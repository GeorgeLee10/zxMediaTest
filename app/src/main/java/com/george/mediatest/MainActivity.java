package com.george.mediatest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    VideoPlayView playView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil checkPermission=new PermissionUtil();
        checkPermission.verifyStoragePermission(this);
        playView=findViewById(R.id.player);
        HashMap<String, MediaCodecInfo.CodecCapabilities> mEncoderInfos = new HashMap<>();
        for(int i = MediaCodecList.getCodecCount() - 1; i >= 0; i--){
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if(codecInfo.isEncoder()){
                for(String t : codecInfo.getSupportedTypes()){
                    try{
                        mEncoderInfos.put(t, codecInfo.getCapabilitiesForType(t));
                    } catch(IllegalArgumentException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        playView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playView.stop();
    }
}