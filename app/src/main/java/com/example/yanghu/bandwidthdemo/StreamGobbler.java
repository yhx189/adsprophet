package com.example.yanghu.bandwidthdemo;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by yanghu on 1/5/16.
 */
class StreamGobbler
        extends Thread
{
    private static String LOG_TAG;
    static {
        LOG_TAG = "STREAMGOBBLER";
    }
    private InputStream inputStream;
    private String streamType;
    private Handler handler;

    /**
     * Constructor.
     *
     * @param inputStream the InputStream to be consumed
     * @param streamType the stream type (should be OUTPUT or ERROR)
     */
    StreamGobbler(final InputStream inputStream,
                  final String streamType, Handler handler)
    {
        this.inputStream = inputStream;
        this.streamType = streamType;
        this.handler = handler;
    }

    /**
     * Consumes the output from the input stream and displays the lines consumed if configured to do so.
     */
    @Override
    public void run()
    {
        try
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
                System.out.println(streamType + ">" + line);
                Message msg = new Message();
                msg.what = MainActivity.TRACEROUTE_MSG;
                msg.obj = line;
                handler.sendMessage(msg);
                System.out.println("done sending the msg");
            }
        }
        catch (IOException ex)
        {
            System.err.println("Failed to consume input stream of type " + streamType +
                    "."+ex.toString());
        }
        catch (Exception ex){
            System.err.println("Failed to consume input stream of type "+ streamType +
                    "."+ex.toString());
        }
    }
}
