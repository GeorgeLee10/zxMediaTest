package com.george.mediatest;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class VideoPlayView extends SurfaceView implements SurfaceHolder.Callback {


    private static final String strVideo= Environment.getExternalStorageDirectory().getPath()+"/testfile.mp4";
    private static final String TAG="VideoPlayView";
    private VideoDecodeThread thread;
    private SoundDecodeThread soundDecodeThread;
    public static boolean isCreate=false;
    public VideoPlayView(Context context){
        super(context);
        getHolder().addCallback(this);
    }

    public VideoPlayView(Context context, AttributeSet attrs) {
        super(context,attrs);
        getHolder().addCallback(this);
    }
    public VideoPlayView(Context context,AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.e(TAG, "surfaceCreated ");
        isCreate=true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int i, int i1, int i2) {
        Log.e(TAG, "surfaceChanged:" );
        if(thread==null)
        {
            thread=new VideoDecodeThread(holder.getSurface(),strVideo);
            thread.start();
        }
        if (soundDecodeThread==null){
            soundDecodeThread=new SoundDecodeThread(strVideo);
            soundDecodeThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.e(TAG, "surfaceDestroyed:" );
        if(thread != null){
            thread.interrupt();
        }
        if (soundDecodeThread != null){
            soundDecodeThread.interrupt();
        }
    }

    public void start(){
        Log.e("VideoPlayView", "start");
        thread = new VideoDecodeThread(getHolder().getSurface(), strVideo);
        soundDecodeThread = new SoundDecodeThread(strVideo);
        soundDecodeThread.start();
        thread.start();
    }

    public void stop(){
        thread.interrupt();
        soundDecodeThread.interrupt();
    }
}
