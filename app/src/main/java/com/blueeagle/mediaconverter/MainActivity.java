package com.blueeagle.mediaconverter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start_command).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCommand();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    111);
        }
    }

    private void startCommand() {
        String ffmpegPath = getDir("files", Context.MODE_PRIVATE).getAbsolutePath() + "/ffmpeg";
        if (!new File(ffmpegPath).exists())
            copyAsset("ffmpeg", ffmpegPath);

        String demoVideoPath = getExternalFilesDir(null) + "/demo.mp4";
        if (!new File(demoVideoPath).exists())
            copyAsset("demo.mp4", demoVideoPath);

        String outVideoPath = getExternalFilesDir(null) + "/demo_out.mp4";

        File ffmpegFile = new File(ffmpegPath);
        boolean isExecutable = ffmpegFile.setExecutable(true);
        Log.d(TAG, ffmpegPath + " is executable: " + isExecutable);

        try {
            String command = ffmpegPath + " -i '" + demoVideoPath + "' '" + outVideoPath + "'";
            command = "-f concat -safe 0 -i " + demoVideoPath + " -c copy " + demoVideoPath;
            Log.d(TAG, "Start with command: " + command);
            Process process = new ProcessBuilder("sh", "-c", command).start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                Log.d(TAG, line);
            }

            process.waitFor();
            Log.d(TAG, "Execute DONE");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void copyAsset(String assetFileName, String destFilePath) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetFileName);
            File outFile = new File(destFilePath);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            //
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + assetFileName, e);
            //
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }
}
