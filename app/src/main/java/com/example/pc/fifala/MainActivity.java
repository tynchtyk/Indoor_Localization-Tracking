package com.example.pc.fifala;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import com.example.pc.fifala.Data;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.ulp;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final String CoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    final String AccessWifi = Manifest.permission.ACCESS_WIFI_STATE;
    final String ChangeWifi = Manifest.permission.CHANGE_WIFI_STATE;
    private TextView degreeView;
  //  private TextView distView;
    private double V1 = 0;
    private double V2 = 0;
    private double V3 = 0;
    private double prevV1 = 0;
    private double prevV2 = 0;
    private double prevV3 = 0;
    private double A1 = 0;
    private double A2 = 0;
    private double A3 = 0;
    private double Time = 0;
    private double currentDegree = 0f;
    private int AbsoluteX = 0;
    private int AbsoluteY = 0;
    private double X = 0;
    private double Y = 0;
    private double Z = 0;
    private boolean FF = true;
    private boolean started = false;
    private static SensorManager sensorManager;
    private Sensor sensor;
    private Sensor sensor1;
    private Line customCanvas;
    private float currentAzimuth = 0f;
    //    @Override
//    public void onAccuracyChange(Sensor sensor, int i){
//
//    }
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        MotionEvent.PointerCoords[]  cord = new MotionEvent.PointerCoords[event.getPointerCount()];
        int x = (int)event.getX() - 58;
        int y = (int)event.getY() - 410;
        customCanvas.startTouch(x, y);
        customCanvas.invalidate();

        AbsoluteX = x;
        AbsoluteY = y;
        putImage(x,y);
        // Log.v("MainActivity",  "X : " + String.valueOf(x) + " Y : " + String.valueOf(y));


        return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (checkSelfPermission(CoarseLocation) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        if (checkSelfPermission(AccessWifi) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 123);

        if (checkSelfPermission(ChangeWifi) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 123);

        if (checkSelfPermission(ChangeWifi) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);

        if (checkSelfPermission(ChangeWifi) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 123);

        LocationManager lman = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = false;
        try {
            network_enabled = lman.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        if (!network_enabled)
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);


        super.onCreate(savedInstanceState);
        //registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        setContentView(R.layout.activity_main);

        customCanvas = (Line) findViewById(R.id.signature_canvas);

        degreeView = (TextView) findViewById(R.id.Degree);
        //distView = (TextView) findViewById(R.id.distance);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        Button b2 = (Button) findViewById(R.id.start);
        b2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = true;
            }
        });

        Button b1 = (Button)findViewById(R.id.button);
        b1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                try {
                    // Toast.makeText(MainActivity.this, "path: " + name, Toast.LENGTH_SHORT).show();


                    int AGAIN = 1;int MINERROR = 999999999, LOCAT = 99999999;
                    while(AGAIN < 2) {
                        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifiMan.startScan();

                        List <ScanResult> result = wifiMan.getScanResults();


//               Log.v("MainActivity", String.valueOf(result.size()));
                        //UPLOAD

                        for(int files = 1; files <= 9 ; files++) {
                            //String name = inputText.getText().toString();
                            String name =  String.valueOf(files) + ".txt";
                            File MYFILE = new File(getExternalFilesDir("PIDARAZ"), name);
                            MYFILE.createNewFile();
                            FileInputStream is = new FileInputStream(MYFILE);
                            DataInputStream in = new DataInputStream(is);
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));
                            String strLine;
                            ArrayList<Data> datas = new ArrayList<>();
                            String mydata = "";
                            while ((strLine = br.readLine()) != null) {
                                Data temp = parse(strLine);
                                //String mmac = temp.mac;
                                //int rrssi = temp.rssi;
                                // Log.v("MainActivity", strLine);
                                //   Log.v("MainActivity", mmac + "  " + String.valueOf(rrssi));
                                //                            Toast.makeText(MainActivity.this, strLine, Toast.LENGTH_SHORT).show();
                                //                        my
                                boolean ok = true;
                                int index = 0;
                                //for(Data i: datas) {
                                //if(temp.mac == i.mac) //{datas.set(index, new Data(i.mac, i.rssi + temp.rssi, i.count + temp.count)); ok = false; break;}

                                // index++;
                                //                        }
                                //                      if(ok)

                                datas.add(temp);
                            }

                            in.close();
                            // File fifla = MYFILE;


                            FileOutputStream os = new FileOutputStream(MYFILE);
                            //MYFILE.write

                            // os = new FileOutputStream(name,true);
                            // os.flush();
                            //       os.close();

//                    os = new FileOutputStream(MYFILE, false);
                            int sum = 0;
                            ArrayList<Data> res = new ArrayList<>();
                            int error = 0, KOL = 0;
                            for (ScanResult i : result) {
                                boolean flag = true;
                           //     Log.v("MainActivity", i.level);
                          // Toast.makeText(MainActivity.this, "path: " + String.valueOf(MYFILE), Toast.LENGTH_SHORT).show();

                                sum = 0;
                                int kol = 0;
                                for (Data j : datas) {

                                    //Log.v("MainActivity", "HELLLO");
                                    if (j.mac.equals(i.BSSID)) {
                                        sum = sum + j.rssi;
                                        kol = kol + 1;
                                        flag = false;
                                    }
                                    //           int cur = i.level;
                                    //res.add(new Data(i.BSSID, (j.rssi / j.count) - i.level, j.count + 1));
                                    //            datas.set(jj, new Data(i.BSSID, (i.level + j.rssi)/2));

                                    //Log.v("MainActivity", i.BSSID   +  "    "   +  String.valueOf((j.rssi / j.count) - i.level));
                                    //            flag = false;
                                    //             break;
                                    //      }

                                    //        if(j.mac == i.BSSID) {
                                    //           int cur = i.level;
                                    //res.add(new Data(i.BSSID, (j.rssi / j.count) - i.level, j.count + 1));
                                    //            datas.set(jj, new Data(i.BSSID, (i.level + j.rssi)/2));

                                    //Log.v("MainActivity", i.BSSID   +  "    "   +  String.valueOf((j.rssi / j.count) - i.level));
                                    //            flag = false;
                                    //             break;
                                    //      }
                                    //     jj++;
                                    //Log.v("MainActivity", i.BSSID   +  "    "   +  i.level);
                                }
                                if (!flag) {
                                    //           Log.v("MainActivity", i.BSSID + "   " + String.valueOf(sum / kol));
                                    if(kol != 0)
                                        error = error + abs(i.level - (sum / kol));
                                    KOL = KOL + 1;
                                }
//                        else
                                //        Log.v("MainActivity", " NEW SCAN " + i.level);

                                //if(flag) res.add(new Data(i.BSSID, i.level, 1));*/
                                //  if(flag)
                                //  datas.add(new Data(i.BSSID, i.level));
                                //                            generateNoteOnSD(MainActivity.this, "DATA", i.BSSID, i.level);
                                // Toast.makeText(MainActivity.this, "FILE CREATED " + String.valueOf(directory), Toast.LENGTH_SHORT).show();
                            }
                            for (Data i : datas) {
                                os.write((i.mac + "    " + String.valueOf(i.rssi) + "\n").getBytes());
                            }
                            if(KOL != 0)
                                if(MINERROR > (error / KOL) ) {MINERROR = (error / KOL); LOCAT = files;}
                        }

                        AGAIN = AGAIN + 1;
                    }
                    if(FF) { FF = false; Toast.makeText(MainActivity.this, "Press again", Toast.LENGTH_SHORT).show(); }
                    else {
                        FF = true;
                        int x = 0;
                        int y = 0;
                        if (String.valueOf(LOCAT) == "1") {
                            x = 200;
                            y = 840;
                        } else if (String.valueOf(LOCAT) == "2") {
                            x = 450;
                            y = 860;
                        } else if (String.valueOf(LOCAT) == "3") {
                            x = 720;
                            y = 930;
                        } else if (String.valueOf(LOCAT) == "4") {
                            x = 770;
                            y = 730;
                        } else if (String.valueOf(LOCAT) == "5") {
                            x = 1040;
                            y = 810;
                        } else if (String.valueOf(LOCAT) == "6") {
                            x = 970;
                            y = 1010;
                        } else if (String.valueOf(LOCAT) == "7") {
                            x = 860;
                            y = 1380;
                        } else if (String.valueOf(LOCAT) == "8") {
                            x = 530;
                            y = 1554;
                        } else if (String.valueOf(LOCAT) == "9") {
                            x = 608;
                            y = 1270;
                        }
                        putImage(x, y);
                    }
                    //  Toast.makeText(MainActivity.this, "LOC" + String.valueOf(LOCAT), Toast.LENGTH_SHORT).show();
//                    os.write(("---------------    000000000").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }



                //    Log.v("MainActivity",    i.BSSID  + " -- "+ i.SSID + " -- " + i.level);
                //   Toast.makeText(MainActivity.this, "\nRSSI : " + newRssi + "dbm" + "\nMACADDR : " + macAdd + "\nLevel: " + percentage, Toast.LENGTH_SHORT).show();
            }
        });


        if(!isExternalStorageAvailable()) {
            Toast.makeText(MainActivity.this, "NOT AVAILEBALE", Toast.LENGTH_SHORT).show();
        }
    }



    public Data parse (String str) {
        String s = "";
        int index = 0;
        String MAC = "";
        int RSSI = 0;
        str = str + " ";
        for(int i=0; i<str.length(); i++) {
            if(str.charAt(i) != ' ') {
                s = s + str.charAt(i);
            }
            else {
                if(s.equals("")) continue;
                if(index == 0) MAC = s;
                else  RSSI = Integer.parseInt(s);
                index = index + 1;
                s = "";
            }
        }
        //  Log.v("MainActivity", "MAC : " + MAC);
        //  Log.v("MainActivity", "RSSI : " + String.valueOf(RSSI));
        Data c = new Data(MAC,RSSI);
        return c;
    }
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    public void putImage(int x, int y) {
        ImageView m = (ImageView) findViewById(R.id.locc);
//                    int x = 0,y = 0;
        if(AbsoluteX == 0 && AbsoluteY == 0) {
            customCanvas.startTouch(x, y);
            customCanvas.invalidate();
        }
        else {
            customCanvas.moveTouch(x, y);
            customCanvas.invalidate();
        }
        AbsoluteX = x;
        AbsoluteY = y;
        m.setX(x);
        m.setY(y);
        m.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensor1,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensor1,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }
    public static final float NS = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private int count = 0;
    private int count1 = 0;
    private int count2 = 0;
    private int count3 = 0;
    private double AccX = 0;
    private double AccY = 0;
    private double AccZ = 0;


    @Override
    public void onSensorChanged(SensorEvent event) {
        double Xdeg, Ydeg, Zdeg ;
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            Xdeg = event.values[1];
            Ydeg = event.values[2];
            Zdeg = event.values[0];
            degreeView.setText(Integer.toString((int) Math.round(Zdeg)));
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if(started) {
//                degreeView.setText(Integer.toString((int) Math.round(Zdeg)));
                Zdeg = Double.valueOf(degreeView.getText().toString());
                Log.v("MainActivity", Double.toString(Zdeg));
                double curTime = System.currentTimeMillis();
                double dt = curTime - Time;
                // / if ((curTime - Time) > 100) {
                double z = event.values[2];
                double x = event.values[0];
                double y = event.values[1];
                if ((x <= 0.18) && (x >= -0.18)) x = 0.0;
                if ((y <= 0.18) && (y >= -0.18)) y = 0.0;
                if ((z <= 0.18) && (z >= -0.18)) z = 0.0;
                if ((x <= -2.0) || (x >= 2.0)) x = 0.0;
                if ((y <= -2.0) || (y >= 2.0)) y = 0.0;
                if ((z <= -2.0) || (z >= 2.0)) z = 0.0;

                // Log.v("MainActivity", Double.toString(x) + "   --   " + Double.toString(y) + "    "  + Double.toString(z));
                if(x == 0.0) count1++;
                else count1 = 0;
                if(y == 0.0) count2++;
                else count2 = 0;
                if(z == 0.0) count3++;
                else count3 = 0;

                if(count1 == 25) {prevV1 = 0; V1 = 0;}
                if(count2 == 25) {prevV2 = 0; V2 = 0;}
                if(count3 == 25) {prevV3 = 0; V3 = 0;}

                if (dt < 500) {
                    AccX = AccX + x;
                    AccY = AccY + y;
                    AccZ = AccZ + z;
                    count = count + 1;
                    //      Log.v("MainActivity", Double.toString(x));
                } else {
                    x = (AccX + x) / (count * 1.0 + 1.0);
                    y = (AccY + y) / (count * 1.0 + 1.0);
                    z = (AccZ + z) / (count * 1.0 + 1.0);
                    if ((x <= 0.18) && (x >= -0.18)) x = 0.0;
                    if ((y <= 0.18) && (y >= -0.18)) y = 0.0;
                    if ((z <= 0.18) && (z >= -0.18)) z = 0.0;

                    if ((x <= -2.0) || (x >= 2.0)) x = 0.0;
                    if ((y <= -2.0) || (y >= 2.0)) y = 0.0;
                    if ((z <= -2.0) || (z >= 2.0)) z = 0.0;

                    ///   Log.v("MainActivity", Double.toString(x) + "  " +  Double.toString(x) + "  " +  Double.toString(x));

                    //  Log.v("MainActivity", Double.toString(x) + " --- " + Double.toString(y) + " --- " + Double.toString(z));
                    //      Log.v("MainActivity", Double.toString(x));


                    //    Log.v("MainActivity", Double.toString(x));
                    AccX = 0; AccY = 0; AccZ = 0; count = 0;
                    // degreeView.setText(Integer.toString(x) + " : " + Integer.toString(y)+  " : " +Integer.toString(z));
                    //    distView.setText(Integer.toString(x) + " : " + Integer.toString(y)+  " : " +Integer.toString(z));
                    //double diffTime = (curTime - Time) / 1000;
                    dt = dt / 1000.0;

                    if(A1 + ((x - A1) / 2.0) * dt < 0.01 && A1 + ((x - A1) / 2.0) * dt > -0.01) V1  = prevV1;
                    else V1 = prevV1 + (A1 + (x - A1) / 2.0) * dt;

                    if(A2 + ((y - A2) / 2.0) * dt < 0.01 && A2 + ((y - A2) / 2.0) * dt > -0.01) V2  = prevV2;
                    else V2 = prevV2 + (A2 + (y - A2) / 2.0) * dt;

                    if(A3 + ((z - A3) / 2.0) * dt < 0.01 && A3 + ((x - A3) / 2.0) * dt > -0.01) V3  = prevV3;
                    else V3 = prevV3+ (A3 + (z - A3) / 2.0) * dt;
                    //if(count1 >= 25) {V1 = 0; prevV1 = 0;}
                    //if(count2 >= 25) {V2 = 0; prevV2 = 0;}
                    // if(count3 >= 25) {V3 = 0; prevV3 = 0;}
                    //                   V2 = y * diffTime;


                    //if ((V1 <= 0.18) && (V1 >= -0.18)) {V1 = 0.0;prevV1 = 0;}
                    // if ((V2 <= 0.18) && (V2 >= -0.18)) {V2 = 0.0; prevV2 = 0;}
                    //  if ((V2 <= 0.18) && (V2 >= -0.18)) {V3 = 0.0; prevV3 = 0;}

                    // if ((V1 <= -3.0) || (V1 >= 3.0)) {V1 = 0.0; prevV1 = 0.0;}
                    //  if ((V2 <= -3.0) || (V2 >=  3.0)) {V2 = 0.0; prevV2 = 0.0;}
                    //  if ((V3 <= -3.0) || (V3 >= 3.0)) {V3 = 0.0; prevV3 = 0.0;}

                    // degreeView.setText(xd + " " + yd + " " + zd);

                    double dispX = (prevV1 + ((V1 - prevV1) / 2.0)) * dt;
                    double dispY = (prevV2 + ((V2 - prevV2) / 2.0)) * dt;
                    double dispZ = (prevV3 + ((V3 - prevV3) / 2.0)) * dt;

                    if( dispX * dt < 0.03 &&  dispX > -0.03) X += 0.0;
                    else {
                        // if(xd == "RIGHT")
                        //    X += (prevV1 + ((V1 - prevV1) / 2.0)) * dt;
                        //else if(xd == "LEFT")
                        X += dispX * dt;
                    }

                    if(dispY < 0.03 && dispY > -0.03) Y += 0.00;
                    else {
                        // if(yd == "RIGHT")
                        //    Y += (prevV2 + ((V2 - prevV2) / 2.0)) * dt;
                        // else if(yd == "LEFT")
                        Y += dispY * dt;
                    }

                    if(dispZ < 0.03 && dispZ > -0.03) Z += 0.0;
                    else {
                        //if(zd == "RIGHT")
                        //     Z += (prevV3 + ((V3 - prevV3) / 2.0)) * dt;
                        // else if(zd == "LEFT")
                        Z += dispZ * dt;
                    }

                    //                  Y += (int) Math.round(prevV2 + V2 * diffTime);
//                    Z += (int) Math.round(prevV3 + V3 * diffTime);
//                Log.v("MainActivity", Double.toString((prevV2 + ((V2 - prevV2) / 2.0)) * dt) + " -- " + Double.toString(V2) + "  --  " + Double.toString(y));
                    Time = curTime;
                    A1 = x;
                    A2 = y;
                    A3 = z;
                    prevV1 = V1;
                    prevV2 = V2;
                    prevV3 = V3;
                    //              double a[3] = {dispX, dispY, dispZ};
//                for(int i=0; i<3; i++) for (int j=0; j<3; j++) if(a[i] > a[j])

                    double dispMax = Math.max(dispX , Math.max(dispY, dispZ));
                    double dispMin = Math.min(dispX , Math.min(dispY, dispZ));
                    double sin = Math.sin(Zdeg);
                    double cos = Math.cos(Zdeg);

                 //   distView.setText(Integer.toString(AbsoluteX) + "  " + Integer.toString(AbsoluteY));
                    double DIST = Math.sqrt(dispX * dispX + dispY * dispY );
                    int rand = (int) Math.random() % 6 + 6;
                    int sideAdd = (int) Math.round(170 / 25.0 * dt);
                    int directAdd = (int) Math.round(770.0 / 25.0 * dt);
                    if(DIST > 0.11 || DIST < -0.11) {
                        //RIGHT
                        if(Zdeg >= 205 && Zdeg <= 295) {

                            if(AbsoluteX <= 680) {
                               if(AbsoluteY + sideAdd  >  (AbsoluteX + directAdd) * 1000 / 3312 + 758  && AbsoluteX >= 450)
                                    putImage(AbsoluteX + directAdd, AbsoluteY );
                                else
                                    putImage(AbsoluteX +directAdd, AbsoluteY + sideAdd);
                            }
                            else {
                                  if(AbsoluteY < 1200) {
                                      if(AbsoluteY + sideAdd  >  (AbsoluteX + directAdd) * 1000 / 3312 + 758  && AbsoluteX >= 760 && AbsoluteX <= 940)
                                          putImage(AbsoluteX + directAdd, AbsoluteY );
                                      else
                                          putImage(AbsoluteX +directAdd, AbsoluteY + sideAdd + 2);
                                  }
                                  else {
                                      if(AbsoluteY + sideAdd >  (AbsoluteX + directAdd) * 2613 /1000  - 752  && AbsoluteX >= 645 && AbsoluteX < 820)
                                          putImage(AbsoluteX + directAdd, AbsoluteY );
                                      else
                                          putImage(AbsoluteX +directAdd, AbsoluteY + sideAdd );

                                  }
                            }


                        }
                        ////DOWN
                        else if(Zdeg >= 296 || Zdeg <= 45 ) {
                            if(AbsoluteX >= 800) {
                                 if(AbsoluteY <= 955) {
                                     if (AbsoluteX - sideAdd  < (AbsoluteY + directAdd) * (-3155) / 10000 + 1251)
                                         putImage(AbsoluteX , AbsoluteY + directAdd);
                                     else
                                         putImage(AbsoluteX - sideAdd - 2, AbsoluteY + directAdd);
                                 }
                                 else {
                                         int down1y  = AbsoluteY + directAdd;
                                         if(down1y > 1511) down1y = 1511;
                                        if(AbsoluteY >= 1050 && AbsoluteY <= 1370) {
                                            if (AbsoluteX - sideAdd  < (AbsoluteY + directAdd) * (-3155) / 10000 + 1251)
                                                putImage(AbsoluteX , down1y);
                                            else
                                                putImage(AbsoluteX - sideAdd - 2, down1y);
                                        }
                                        else
                                            putImage(AbsoluteX - sideAdd - 2, down1y);
                                 }
                            }
                            else {
                                int down1y  = AbsoluteY + directAdd;
                                if(down1y > 1595) down1y = 1595;
                                if(AbsoluteY <= 885) {
                                    if (AbsoluteX - sideAdd  < (AbsoluteY + directAdd) * (-3029) / 10000 + 950)
                                        putImage(AbsoluteX , AbsoluteY + directAdd);
                                    else
                                        putImage(AbsoluteX - sideAdd - 2, AbsoluteY + directAdd);
                                }
                                else if(AbsoluteY >= 975) {

                                    if (AbsoluteX - sideAdd < (AbsoluteY + directAdd) * (-3029) / 10000 + 950)
                                        putImage(AbsoluteX , down1y);
                                    else
                                        putImage(AbsoluteX - sideAdd - 2, down1y);
                                }
                                else
                                    putImage(AbsoluteX - sideAdd  - 2, down1y);
                            }
                        }
                        //LEFT
                        else if(Zdeg >= 25 && Zdeg <= 115) {
                            if(AbsoluteX <= 680) {
                                if(AbsoluteY - sideAdd <  (AbsoluteX - directAdd) * 261 / 1000 + 690.6)
                                    putImage(AbsoluteX - directAdd, AbsoluteY - sideAdd / 10);
                                else
                                    putImage(AbsoluteX - directAdd, AbsoluteY - sideAdd -2 );
                            }
                            else {
                                if(AbsoluteY < 1200) {
                                    if(AbsoluteY - sideAdd  <  (AbsoluteX - directAdd) *  3128 / 10000 + 662 && AbsoluteX >= 760 && AbsoluteX <= 940)
                                        putImage(AbsoluteX - directAdd, AbsoluteY  );
                                    else
                                        putImage(AbsoluteX - directAdd, AbsoluteY - sideAdd -2);
                                }
                                else {
                                  if(AbsoluteY - sideAdd  <  (AbsoluteX - directAdd) * 3345 / 10000 + 1093 && AbsoluteX >= 645 && AbsoluteX < 820)
                                        putImage(AbsoluteX - directAdd, AbsoluteY  );
                                  else
                                        putImage(AbsoluteX - directAdd, AbsoluteY - sideAdd - 2);
                                }
                            }
                        }
                        //UP
                        else if(Zdeg >= 115 && Zdeg <= 205) {
                            int down1y  = AbsoluteY - directAdd;

                            if(AbsoluteX <= 790) {
                                if(down1y < 690) down1y = 690;
                                if(AbsoluteY >= 990) {
                                    if (AbsoluteX + sideAdd  > (AbsoluteY - directAdd) * (-2964) / 10000 + 1019)
                                        putImage(AbsoluteX , down1y);
                                    else
                                        putImage(AbsoluteX + sideAdd +2 ,  down1y);
                                }
                                else if(AbsoluteY <= 890) {
                                    if (AbsoluteX + sideAdd > (AbsoluteY - directAdd) * (-2983) / 10000 + 1022)
                                        putImage(AbsoluteX,  down1y);
                                    else
                                        putImage(AbsoluteX + sideAdd + 2,  down1y);
                                }
                                else
                                    putImage(AbsoluteX + sideAdd + 2 ,  down1y);

                            }

                            else if(AbsoluteX >= 795) {
                                if(down1y < 750)
                                    down1y = 750;

                                  if (AbsoluteX + sideAdd  > (AbsoluteY) * (-3) / 1000 + 1324)
                                       putImage(AbsoluteX ,  down1y);
                                   else
                                        putImage(AbsoluteX + sideAdd + 2,  down1y);
                            }

                        }


                    }
                }
            }
        }
        //            Log.v("MainActivity", "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}