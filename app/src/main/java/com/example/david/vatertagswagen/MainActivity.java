package com.example.david.vatertagswagen;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.util.Log;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;

import com.example.david.vatertagswagen.Database.DataBaseControll;

public class MainActivity extends AppCompatActivity {
    private Activity main_activity =this;
    private static final String TAG = "MainActivity";
    private static final int MENU_BEENDEN=10;
    private static final int MENU_INFO=11;
    private static final int DIALOG_INFO=12;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RESULT_SETTING_BT = 3;
    public static final String BROADCASTACTION= "com.example.david.vatertagswagen";
    public static final String BROADCASTEXTRA_ACTIVE = "active";
    public static final String BROADCASTEXTRA_BTSTATUS = "status";
    public String mac="";
    public String deviceName;
    public BluetoothAdapter blueAdapter;
    public BluetoothViewerService mBluetoothService;
    public boolean connected;
    public String bluetoothstatus="";
    public String active="";
    private Intent intentbluetoothstatus = new Intent();
    private Intent intentactive = new Intent();

    public DataBaseControll database;

    private TextView textview_BatterieVoltage;
    private TextView textview_EngineCurrent;
    private TextView tv_throttle;
    private ProgressBar pb_Spannung;
    private SeekBar sb_throttle;
    private Button button_bremse;
    private Button button_throttle_active;

    private int Bremse_aktiv=0;
    private int throttle_active=0;
    private int Throttle=0;

    private Drawable defaultDrawable_bremse;
    private Drawable defaultDrawable_throttle;
    private java.text.NumberFormat nf = java.text.NumberFormat.getInstance();


    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothViewerService.MSG_CONNECTED:
                    bluetoothstatus = "connected with "+mac;
                    intentbluetoothstatus.putExtra("extra", bluetoothstatus);
                    connected = true;
                    deviceName = msg.obj.toString();
                    break;
                case BluetoothViewerService.MSG_CONNECTING:
                    bluetoothstatus = "connecting with "+mac;
                    intentbluetoothstatus.putExtra("extra", bluetoothstatus);
                    connected = false;
                    break;
                case BluetoothViewerService.MSG_NOT_CONNECTED:
                    bluetoothstatus = "not connected";
                    intentbluetoothstatus.putExtra("extra", bluetoothstatus);
                    connected = false;
                    break;
                case BluetoothViewerService.MSG_CONNECTION_FAILED:
                    bluetoothstatus = "connection failed with "+mac;
                    intentbluetoothstatus.putExtra("extra", bluetoothstatus);
                    connected = false;
                    break;
                case BluetoothViewerService.MSG_CONNECTION_LOST:
                    bluetoothstatus = "connection lost with "+mac;
                    intentbluetoothstatus.putExtra("extra", bluetoothstatus);
                    connected = false;
                    break;
                case BluetoothViewerService.MSG_BYTES_WRITTEN:
                    //String written = new String((byte[]) msg.obj);
                    break;
                case BluetoothViewerService.MSG_LINE_READ:
                    //message has been received
                    //Bundle receivedData = msg.getData();
                    String receivedDataAsString = msg.obj.toString();
                    if(receivedDataAsString.startsWith("#") && receivedDataAsString.endsWith(";")){
                        receivedDataAsString = receivedDataAsString.substring(1,receivedDataAsString.length()-1);
                        String[] messageParts = receivedDataAsString.split("_");
                        switch (messageParts[0]){
                            case "i":
                                textview_EngineCurrent.setText(messageParts[1]+"A");
                                break;
                            case "u":
                                textview_BatterieVoltage.setText(messageParts[1]+"V");
                                break;
                            default:
                                break;
                        }
                    }
                    break;
            }
           // sendBroadcast(intentbluetoothstatus);
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.optionsmenue, menu);
        getMenuInflater().inflate(R.menu.optionsmenue, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menue_info:
                String title = getString(R.string.app_name) + " build " + getVersion(this);
                Builder builder_INFO = new Builder(this);
                builder_INFO.setTitle(title);
                builder_INFO.setView(View.inflate(this, R.layout.info, null));
                builder_INFO.setIcon(R.drawable.icon_small);
                builder_INFO.setPositiveButton("OK", null);

                Dialog info =builder_INFO.create();
                info.show();
                return true;
            case R.id.menue_bluetooth_setup:
                Intent in = new Intent(MainActivity.this,SettingsActivity.class);
                startActivityForResult(in, RESULT_SETTING_BT);
                return true;

            case R.id.menue_beenden:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Beenden");
                builder.setMessage("Wollen Sie VW wirklich beenden?");
                builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(connected)
                            mBluetoothService.stop();

                        System.exit(0);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NEIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }

                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database=new DataBaseControll(this);

        blueAdapter=BluetoothAdapter.getDefaultAdapter();
        intentbluetoothstatus.setAction(BROADCASTACTION);
        intentbluetoothstatus.putExtra("extra", BROADCASTEXTRA_BTSTATUS);

        intentactive.setAction(BROADCASTACTION);
        intentactive.putExtra("extra", BROADCASTEXTRA_ACTIVE);

        textview_BatterieVoltage = (TextView) findViewById(R.id.voltage);
        textview_EngineCurrent = (TextView) findViewById(R.id.current);
        tv_throttle=(TextView)findViewById(R.id.tv_throttle_value);
        pb_Spannung=(ProgressBar)findViewById(R.id.progressBar_Voltage);
        sb_throttle=(SeekBar)findViewById(R.id.seekBar_Motor);
        button_bremse=(Button)findViewById(R.id.button_Bremse);
        button_throttle_active=(Button)findViewById(R.id.button_throttle_active);
        defaultDrawable_bremse = button_bremse.getBackground();
        defaultDrawable_throttle=button_throttle_active.getBackground();
        nf.setMaximumFractionDigits(2);



            button_bremse.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    if(Bremse_aktiv==1)
                    {
                        Bremse_aktiv=0;
                        //button_bremse.setBackgroundColor(Color.LTGRAY);
                        button_bremse.setBackground(defaultDrawable_bremse);
                    }
                    else if(Bremse_aktiv==0)
                    {
                        Bremse_aktiv=1;

                        button_bremse.setBackgroundColor(Color.BLUE);
                    }
                    sendValueViaBluetooth("b",Bremse_aktiv);
                }
            });
            button_throttle_active.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    if(throttle_active==1)
                    {
                        throttle_active=0;
                        button_throttle_active.setText("Throttle aktivieren");
                        button_throttle_active.setBackground(defaultDrawable_throttle);
                        tv_throttle.setText("0%");
                        sb_throttle.setProgress(0);
                    }
                    else if(throttle_active==0)
                    {
                        throttle_active=1;
                        button_throttle_active.setBackgroundColor(Color.BLUE);
                        button_throttle_active.setText("Throttle deaktivieren");
                    }
                    sendValueViaBluetooth("a",throttle_active);
                }
            });
            sb_throttle.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                    Throttle = progress;
                    if(throttle_active==1)
                    {
                        sendValueViaBluetooth("t", Throttle);
                        tv_throttle.setText(String.valueOf((int)(Throttle/2.55))+"%");
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(main_activity,"seek bar progress:"+Throttle,
//                        Toast.LENGTH_SHORT).show();
                }
            });
        }

    @Override
    protected void onResume(){
        super.onResume();
        if(!blueAdapter.isEnabled()){
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        }
        if(mac=="")
            Toast.makeText(getApplicationContext(),"no bluetooth device is selected",
                    Toast.LENGTH_LONG).show();
        else	//Device vorhanden, Verbindung wird aufgebaut
        {
            if(mBluetoothService==null)
            {
                mBluetoothService = new BluetoothViewerService(mHandler);
            }
            if(!connected)
            {
                Toast.makeText(getApplicationContext(),"trying to connect to Used Device: "+mac,
                        Toast.LENGTH_LONG).show();
                try{
                    BluetoothDevice device = blueAdapter.getRemoteDevice(mac);
                    try{
                        mBluetoothService.connect(device);
                    }
                    catch(Exception e){
                        Log.e("error", "ConnectTread: "+e.getMessage());
                    }
                }
                catch(Exception e)
                {
                    Log.e("error", "Verbindung nicht aufbaubar");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_SETTING_BT)
        {
            mac=data.getExtras().getString("com.example.david.vatertagswagen.mac");
//    		Toast.makeText(getApplicationContext(),"Used Device: "+mac,
//	                 Toast.LENGTH_LONG).show();
        }
//    	else
//    	{
//    		super.onActivityResult(requestCode, resultCode, data);
//    	}
    }

    private void sendValueViaBluetooth(String flag, String value){
        if(connected){
            String h = "#"+flag+"_"+value+";";
            mBluetoothService.write(h.getBytes());
        }else
        {
            Toast.makeText(this,"nicht verbunden -> senden nicht möglich",
                    Toast.LENGTH_LONG).show();
        }
    }
    private void sendValueViaBluetooth(String flag, int value){
        if(connected){
            String h = "#" + flag + "_" + Integer.toString(value) + ";";
            mBluetoothService.write(h.getBytes());
        }else{
            Toast.makeText(this,"nicht verbunden -> senden nicht möglich",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String getVersion(Context context) {
        String version = "1.0";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }

    private int getVersionCode() {
        int code = 1;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            code = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return code;
    }
}

