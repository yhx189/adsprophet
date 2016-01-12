package com.example.yanghu.bandwidthdemo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.content.Context;
import android.app.Activity;
/**
 * Created by yanghu on 1/7/16.
 */
public class misc extends Activity {
    public void calcMean(Context context){

        try
        {
            InputStream instream = context.openFileInput("/sdcard/output.txt");
            System.out.println("this is reading");
            if (instream != null)
            {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "", line1 = "";
                try
                {
                    while ((line = buffreader.readLine()) != null) {
                        line1 += line;
                        System.out.println(line);
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            String error="";
            error=e.getMessage();

        }

    }
    public void sendProbe(){

    }
}
