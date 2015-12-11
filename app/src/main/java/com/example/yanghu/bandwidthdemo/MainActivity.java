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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import 	java.util.Arrays;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpStatus;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.StatusLine;
public class MainActivity extends ActionBarActivity {
    public String videoURL;

    TelephonyManager telephonyManager;
    myPhoneStateListener pslistener;
    int sStrength = 0;
    MediaPlayer mediaPlayer = new MediaPlayer();
    SurfaceView mPreview;
    SurfaceHolder holder;
    private EditText input;
    private TextView firstBd;
    private TextView totalBd;
    private TextView packetPair;
    float selectedHop = 0;
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
        firstBd = (TextView) findViewById(R.id.firstBD);
        totalBd = (TextView) findViewById(R.id.totalBd);
        packetPair = (TextView) findViewById(R.id.packetPair);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                Object item = parent.getItemAtPosition(pos);
                selectedHop = Float.valueOf(item.toString().substring(4));
                System.out.println("select hop...   " + selectedHop);

            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }

        });
        List<String> SpinnerArray = new ArrayList<String>();
        SpinnerArray.add("Hop 1");
        SpinnerArray.add("Hop 2");
        SpinnerArray.add("Hop 3");
        SpinnerArray.add("Hop 4");
        SpinnerArray.add("Hop 5");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SpinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

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


    public float getOne(String firstHop){
        boolean sudo = false;
        CharSequence text = input.getText().toString();
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
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
            float bandwidth = 0;
            float lat = 0;
            if(start != -1){
                System.out.println(res);
                lat = (Float.valueOf(res.substring(start+26, start+30 ))).floatValue();
                bandwidth = 1000 / lat;

                text = "current latency is " + lat + "ms\n";
                //System.out.println("current latency is " + lat + "ms\n");
                text = text + "current bandwidth is " + bandwidth + "KBps\n";
                //Toast toast = Toast.makeText(context, text, duration);
                //toast.show();
                //System.out.println("current bandwidth is " + bandwidth + "KBps\n");
                //firstBd.setText("first Mile bandwidth is " + bandwidth + "KBps\n");
                input.setEnabled(true);
                return bandwidth;

            }else {
                System.out.println(res);
            }
            return bandwidth;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public float getTtl(String firstHop, int ttl){
        boolean sudo = false;
        CharSequence text = input.getText().toString();
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String cmd = "ping -c 1 -m " + (64 - ttl) +" " + firstHop;//google.com";
        System.out.println(cmd);
        String dst = "";
        int cnt = 0;
        try {
            Process p;
            p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
                System.out.print("read one line..." + s);
                String IPADDRESS_PATTERN =
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {

                    cnt = cnt + 1;
                    System.out.println("cnt ... " + cnt);
                    if (cnt == 2) {
                        System.out.println("find ip address in: " + matcher.group());
                        dst = matcher.group();
                        System.out.println("dst is ...  " + dst);
                        firstBd.setText("You selected hop" + (int) selectedHop + ", " + matcher.group());

                    }
                }

            }
            p.destroy();


            return getOne(dst);
        }catch (Exception e) {
            e.printStackTrace();
        }



        return 0;
    }
    public String queryKing(View view) {
        String ret = "";

        float bandwidth = 0;

        TextView mTxtDisplay;
        ImageView mImageView;

        String url = "http://165.124.182.209:5000/todo/api/v1.0/tasks/2";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String res = response.getString("task");
                            int start = res.indexOf("bandwidth");
                            packetPair.setText("Second Segment banwdith: " +  res.substring(start + 12, start + 15) + "KB/s");
                        }catch(Exception e){
                            System.out.println(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

// Access the RequestQueue through your singleton class.
        RequestQueue mRequestQueue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

// Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
        mRequestQueue.add(jsObjRequest);

        //packetPair.setText("Second segment bandwidth from 162.124.182.209 to " +  " is "+ String.format("%.2f", bandwidth)  + "KB/s");

        return ret;
    }
    public String getBandwidth(View view){
        // public native void getBandwidth();
        boolean sudo = false;
        input.setEnabled(false);
        String dst = "google.com";
        //String response = HttpRequest.get("http://google.com").body();
        //System.out.println("Response was: " + response);
        //

        //String URL = "http://google.com";

        //HttpClient httpclient = new DefaultHttpClient();

        //

        //try {

        //HttpResponse response =  httpclient.execute(new HttpGet(URL));

        //StatusLine statusLine = response.getStatusLine();

        //if(statusLine.getStatusCode() == HttpStatus.SC_OK){

        //

        //ByteArrayOutputStream out = new ByteArrayOutputStream();

        //response.getEntity().writeTo(out);

        //String responseString = out.toString();

        //out.close();

        //byte[] byteArray = out.toByteArray();

        //for (byte b : byteArray)

        //System.out.print(b+" ");

        //

        //

        //} else{

        ////Closes the connection.

        //response.getEntity().getContent().close();

        //throw new IOException(statusLine.getReasonPhrase());

        //}

        //

        //}catch(IOException ie){

        //ie.printStackTrace();

        //}

        //

        CharSequence text = input.getText().toString();
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
/*
        String firstHop = "";
        String traceRoute = "ping -c 1 -t 1 " + "www.google.com";
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

                    System.out.println(matcher.group());
                }
                else{
                    System.out.println(s);
                    //System.out.println("did not find any ip address\n");
                }
            }

            pro.destroy();
        }catch (Exception e) {
            e.printStackTrace();
        }
*/
        /*
        float[] bds = new float[10];
        float mean = 0;
        for(int i=0;i<10;i++){
            bds[i] = getTtl("google.com", 1);
            mean = mean + bds[i];
            System.out.println(bds[i]);
        }
        Arrays.sort(bds);

        double median;
        if (bds.length % 2 == 0)
            median = ((double)bds[bds.length/2] + (double)bds[bds.length/2 - 1])/2;
        else
            median = (double) bds[bds.length/2];
        //firstBd.setText("TOPP bandwidth is " + String.format("%.2f", median)  + "KB/s");
        firstBd.setText("TOPP bandwidth is " + bds[bds.length -1] + "KB/s");



        float bandwidthfirst = 0;
        for (int i = 1; i < 5; i++){
            float tmp = getTtl("google.com", i);

            if(i == 1)
                bandwidthfirst = tmp;
            System.out.println("bandwidth during traceroute is " + tmp);
        }
        */


        float packet = getTtl(dst, (int) selectedHop);//getOne("google.com");


        packetPair.setText("Packet pair bandwidth is " + String.format("%.2f", packet)  + "KB/s");


        String cmd = "ping -c 1 -s 10 " + dst;
        float bandwidth = 0;
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
                bandwidth = 5000 / lat;

                //text = "current latency is " + lat + "ms\n";
                //System.out.println("current latency is " + lat + "ms\n");
                //text = text + "current bandwidth is " + bandwidth + "KBps\n";
                //Toast toast = Toast.makeText(context, text, duration);
                //toast.show();
                //System.out.println("current bandwidth is " + bandwidth + "KBps\n");
                //firstBd.setText("first Mile bandwidth is " + bandwidth + "KBps\n");
                input.setEnabled(true);

            }else {
                System.out.println(res);
            }
            //return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //float measuredBd = 0;
        //measuredBd = getOne("www.google.com");
        totalBd.setText("total bandwidth is " + String.format("%.2f", bandwidth)  + "KB/s");
        return "";


    }

}
