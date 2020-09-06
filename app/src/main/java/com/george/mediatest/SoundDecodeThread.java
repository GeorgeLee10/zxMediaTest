package com.george.mediatest;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SoundDecodeThread extends Thread {
    private  final static String TAG="SoundDecodeThread";
    private MediaCodec mediaCodec;
    private AudioPlayer mPlayer;
    private String path;

    public SoundDecodeThread(String path){
        this.path=path;
    }

    @Override
    public void run() {
        MediaExtractor mediaExtractor=new MediaExtractor();
        try {
            mediaExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i=0;i<mediaExtractor.getTrackCount();i++)
        {
            MediaFormat format=mediaExtractor.getTrackFormat(i);
            String mimeType=format.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("audio/")){
                mediaExtractor.selectTrack(i);
                try {
                    mediaCodec=MediaCodec.createDecoderByType(mimeType);
                }catch (IOException e){
                    e.printStackTrace();
                }
                mediaCodec.configure(format,null,null,0);
                mPlayer =new AudioPlayer(format.getInteger(MediaFormat.KEY_SAMPLE_RATE), AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);
                mPlayer.init();
                break;
            }
        }
        if(mediaCodec==null){
            Log.e(TAG, "run: Can't find video info" );
            return;
        }
        mediaCodec.start();
        ByteBuffer[] inputBuffers=mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers=mediaCodec.getOutputBuffers();
        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
        boolean isEOS=false;
        long startMs=System.currentTimeMillis();
        while (!isInterrupted()){
            if (!isEOS){
                int inIndex=mediaCodec.dequeueInputBuffer(0);
                if(inIndex>=0){
                    ByteBuffer buffer=inputBuffers[inIndex];
                    int nSampleSize =mediaExtractor.readSampleData(buffer,0);
                    if (nSampleSize<0){
                        Log.d(TAG, "run: InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    }else {
                        // 填数据
                        mediaCodec.queueInputBuffer(inIndex, 0, nSampleSize, mediaExtractor.getSampleTime(), 0); // 通知MediaDecode解码刚刚传入的数据
                        mediaExtractor.advance(); // 继续下一取样
                    }
                }
            }
            int outIndex = mediaCodec.dequeueOutputBuffer(info, 0);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = mediaCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "New format " + mediaCodec.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(TAG, "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    //用来保存解码后的数据
                    byte[] outData = new byte[info.size];
                    buffer.get(outData);
                    //清空缓存
                    buffer.clear();
                    //播放解码后的数据
                    mPlayer.play(outData, 0, info.size);
                    mediaCodec.releaseOutputBuffer(outIndex, true);
                    break;
            }

            // All decoded frames have been rendered, we can stop playing
            // now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }
        mediaCodec.stop();
        mediaCodec.release();
        mediaExtractor.release();
    }
}
