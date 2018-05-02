package com.example.tynchtykbekkaldybaev.wifi_scanning;

            import android.content.BroadcastReceiver;
            import android.content.Context;
            import android.content.Intent;
            import android.content.IntentFilter;
            import android.net.wifi.WifiInfo;
            import android.net.wifi.WifiManager;
            import android.support.v7.app.AppCompatActivity;
            import android.os.Bundle;
            import android.view.View;
            import android.widget.Button;
            import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        setContentView(R.layout.activity_main);

        Button b1 = (Button)findViewById(R.id.button);
        b1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiMan.startScan();
                int linkspeed = wifiMan.getConnectionInfo().getLinkSpeed();
                int newRssi = wifiMan.getConnectionInfo().getRssi();
                double dist = getDistance(newRssi, -59);
                int level = wifiMan.calculateSignalLevel(newRssi, 10);

                int percentage = (int) ((level/10.0)*100);

                String macAdd = wifiMan.getConnectionInfo().getBSSID();

                Toast.makeText(MainActivity.this, "\nRSSI : " + newRssi + "dbm" + "\nMACADDR : " + macAdd + "\nDIST  " + dist + "\nLevel: " + percentage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    double getDistance(int rssi, int txPower) {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */

        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }


private BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
      WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      wifiMan.startScan();
      int linkspeed = wifiMan.getConnectionInfo().getLinkSpeed();
      int newRssi = wifiMan.getConnectionInfo().getRssi();
      int level = wifiMan.calculateSignalLevel(newRssi, 10);
      int percentage = (int) ((level/10.0)*100);
      String macAdd = wifiMan.getConnectionInfo().getBSSID();
      //debugtext.setText("Speed : " + linkspeed + "\nLevel: " + percentage + " / 맥어드레스 : " + macAdd );
  }
};

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(rssiReceiver, rssiFilter);
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(rssiReceiver);
    }


}

