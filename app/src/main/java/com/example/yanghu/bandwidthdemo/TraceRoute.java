package com.example.yanghu.bandwidthdemo;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by yanghu on 1/5/16.
 */
public class TraceRoute {
    private static String LOG_TAG;
    private static String traceroutePath;
    private static String appFileDirectory;
    private static String tracerouteName;
    static {
        LOG_TAG = "TRACEROUTE";
        appFileDirectory = "/data/data/com.example.yanghu.bandwidthdemo/";
        tracerouteName = "traceroute";
        traceroutePath = appFileDirectory+tracerouteName;
    }

    private boolean isInstalled;
    private File tracerouteFile;
    private Context context;
    private int timeout;
    private Handler handler;

    public TraceRoute(Context context_, int timeout, Handler handler){
        context = context_;
        tracerouteFile = new File(traceroutePath);
        if(!tracerouteFile.exists() || !tracerouteFile.canExecute())
            isInstalled = false;
        else
            isInstalled = true;
        this.timeout = timeout;
        this.handler = handler;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void installTraceroute(){
        if(!tracerouteFile.exists()) {
            if(copyAssets(tracerouteName)){
                tracerouteFile = new File(traceroutePath);
                tracerouteFile.setExecutable(true);
                isInstalled = true;
                return ;
            }
        }
        else if(!tracerouteFile.canExecute()){
            tracerouteFile.setExecutable(true);
            isInstalled = true;
            return ;
        }
        Log.e(LOG_TAG, "failed to install traceroute");
        System.out.println("failed to install traceroute");
    }

    public void runTraceroute(String ip){
        if(!isInstalled){
            Log.d(LOG_TAG,"failed to run traceroute: not installed");
            System.out.println("failed to run traceroute: not installed");
            return ;
        }
        String cmd = traceroutePath+" -nI "+" "+ip;
        Process process;
        StreamGobbler errorGobbler, outGobbler;
        try{
            System.out.println("start executing "+cmd);
            process = Runtime.getRuntime().exec(cmd);
            errorGobbler = new StreamGobbler(process.getErrorStream(), "ERR_STREAM", handler);
            errorGobbler.start();
            outGobbler = new StreamGobbler(process.getInputStream(), "OUT_STREAM", handler);
            outGobbler.start();
        }catch(Exception e){
            System.err.println("failed to start traceroute: "+e.toString());
            return;
        }
        System.out.println("start waiting for traceroute to be finished");

        ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process, "traceroute");
        int exitCode = processWithTimeout.waitForProcess(timeout);
        if (exitCode == Integer.MIN_VALUE)
        {
            System.out.println("TIMEOUT");
        }
        else
        {
            System.out.println("Traceroute finished successfully");
        }
        process.destroy();

        //Interrupt output stream threats if required.
        if(!errorGobbler.isInterrupted()){
            System.out.println("error output has not been interrupted. Do it.");
            errorGobbler.interrupt();
        }
        else{
            System.err.println("error output has already been interrupted.");
        }
        if(!outGobbler.isInterrupted()){
            System.out.println("std output has not been interrupted. Do it.");
            outGobbler.interrupt();
        }
        else{
            System.out.println("std output has already been interrupted.");
        }


    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /* Utility Methods */
    private static BufferedReader getProcessOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getProcessError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            System.err.println("Failed to copy inputstream to file: " +
                    file.getName()+ e.toString());
            e.printStackTrace();
        }
    }

    //Copy file from asset folder to appFileDirectory
    private boolean copyAssets(String filename) {
        android.content.res.AssetManager assetManager = context.getAssets();

        java.io.InputStream in = null;
        System.out.println("Attempting to copy this file: " + filename+ " to " + appFileDirectory);

        try {
            in = assetManager.open(filename);
            File outFile = new File(appFileDirectory, filename);
            copyInputStreamToFile(in, outFile);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to copy asset file: " + filename+ e.toString());
            e.printStackTrace();
            return false;
        }
    }
}

