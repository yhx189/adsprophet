package com.example.yanghu.bandwidthdemo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.view.View;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import java.io.*;
import java.util.regex.*;

public class MainActivity extends ActionBarActivity {
    public String videoURL;

    TelephonyManager telephonyManager;
    myPhoneStateListener pslistener;
    int sStrength = 0;
    MediaPlayer mediaPlayer = new MediaPlayer();
    SurfaceView mPreview;
    SurfaceHolder holder;
    private EditText input;

    class myPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            sStrength = signalStrength.getGsmSignalStrength();
            //sStrength = (2 * sStrength) - 113; // -> dBm
            Context context = getApplicationContext();
            int numberOfLevels=100;

            WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mainWifi.getConnectionInfo();
            int level= WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

            CharSequence text = "Signal Strength:"+ level + " \n Bandwidth: " ;

            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();




        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            pslistener = new myPhoneStateListener();
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(pslistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        catch (Exception e) {

            e.printStackTrace();

        }
        input = (EditText) findViewById(R.id.editText);





        /*
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer.setDisplay(holder);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    public void startVideo(View view){
        videoURL = "http://165.124.182.209:8082/ti5-a.mp4";


        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(videoURL);
        }catch (Exception e) {
            Log.wtf("DO THIS", " WHEN setDataSource() FAILS");
        }
        try{
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        }catch (Exception e) {
            Log.wtf("DO THIS", " WHEN prepare() FAILS");
        }
        Context context = getApplicationContext();
        CharSequence text = "video started!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        mediaPlayer.start();

    }
    public void endVideo(View view){
        mediaPlayer.stop();
        Context context = getApplicationContext();
        CharSequence text = "video stopped!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }
    public String getBandwidth(View view){
        // public native void getBandwidth();
        boolean sudo = false;


        CharSequence text = input.getText().toString();
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        String firstHop = "";
        String traceRoute = "ping -c 1 -t 1 google.com";
        try{
            Process pro;
            //pro = Runtime.getRuntime().exec(new String[]{"su", "-c", traceRoute});
            pro = Runtime.getRuntime().exec(traceRoute);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                //s = "36 bytes from dhcp-10-105-0-1.wireless.northwestern.private (10.105.0.1): Time to live exceeded";
                res += s + "\n";

                String IPADDRESS_PATTERN =
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    System.out.println("find ip address in: " + matcher.group());
                    firstHop = matcher.group();

                    //System.out.println(matcher.group());
                }
                else{
                    //System.out.println(s);
                    //System.out.println("did not find any ip address\n");
                }
            }

            pro.destroy();
        }catch (Exception e) {
            e.printStackTrace();
        }

        String cmd = "ping -c 1 -s 1000 " + firstHop;//google.com";
        try {

            Process p;
            if(!sudo)
                p= Runtime.getRuntime().exec(cmd);
            else{
                p= Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            }
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
            }
            p.destroy();
            int start = res.indexOf("min/avg/max/mdev =");
            if(start != -1){
                float lat = (Float.valueOf(res.substring(start+26, start+30 ))).floatValue();
                float bandwidth = 1000 / lat;

                text = "current latency is " + lat + "ms\n";
                System.out.println("current latency is " + lat + "ms\n");
                text = text + "current bandwidth is " + bandwidth + "KBps\n";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                System.out.println("current bandwidth is " + bandwidth + "KBps\n");
            }else {
                System.out.println(res);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

}
