package com.decibility.decibilityapp;

import static java.lang.System.out;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import com.decibility.audioprocessing.AudioThread;
import com.decibility.ledmanager.DecibilityLEDs;

public class ConnectDevicePage extends AppCompatActivity {

    private final String TAG = ConnectDevicePage.class.getSimpleName();

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    // GUI Components - get references to listen to their onClick events and update their text values
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private ListView mDevicesListView;
    private CheckBox mLED1;
    private Button connect_device_back_btn;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private AudioThread mAudioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_device_page);

        mBluetoothStatus = (TextView)findViewById(R.id.bluetooth_status);
        mReadBuffer = (TextView) findViewById(R.id.read_buffer);
        mScanBtn = (Button)findViewById(R.id.scan);
        mOffBtn = (Button)findViewById(R.id.off);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.paired_btn);
        mLED1 = (CheckBox)findViewById(R.id.checkbox_led_1);
        connect_device_back_btn = findViewById(R.id.connect_device_back_btn);

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        // creates a list view to contain our bluetooth devices
        mDevicesListView = (ListView)findViewById(R.id.devices_list_view);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        // This function is called from our bluetooth "ConnectedThread" thread and another
        // thread that handles the discovery stage
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    char[] sConnected;
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText(getString(R.string.BTConnected) + msg.obj);
                    else
                        mBluetoothStatus.setText(getString(R.string.BTconnFail));
                }
            }
        };

        // Ensure bluetooth capability on phone. If so, assign actions to each buttons click event.
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText(getString(R.string.sBTstaNF));
            Toast.makeText(getApplicationContext(),getString(R.string.sBTdevNF),Toast.LENGTH_SHORT).show();
        }
        else {

            connect_device_back_btn.setOnClickListener(view -> {

                Intent intent = new Intent(this, HomePage.class);
                Intent this_intent = getIntent();

                // send user range configuration to device
                if (getIntent().hasExtra("min_volume_input")) {
                    Log.d("CONFIG", (getIntent().getStringExtra("min_volume_input")));
                }
                if (getIntent().hasExtra("max_volume_input")) {
                    Log.d("CONFIG", (getIntent().getStringExtra("max_volume_input")));
                }
                if (getIntent().hasExtra("min_frequency_input")) {
                    Log.d("CONFIG", (getIntent().getStringExtra("min_frequency_input")));
                }
                if (getIntent().hasExtra("max_frequency_input")) {
                    Log.d("CONFIG", (getIntent().getStringExtra("max_frequency_input")));
                }

                // keep data persistent through page navigation
                if (this_intent.hasExtra("min_volume_input")) {
                    intent.putExtra("min_volume_input", this_intent.getStringExtra("min_volume_input"));
                }
                if (this_intent.hasExtra("max_volume_input")) {
                    intent.putExtra("max_volume_input", this_intent.getStringExtra("max_volume_input"));
                }
                if (this_intent.hasExtra("min_frequency_input")) {
                    intent.putExtra("min_frequency_input", this_intent.getStringExtra("min_frequency_input"));
                }
                if (this_intent.hasExtra("max_frequency_input")) {
                    intent.putExtra("max_frequency_input", this_intent.getStringExtra("max_frequency_input"));
                }

                startActivity(intent);
            });

            mLED1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mAudioThread != null) //First check to make sure thread created
                    {
                        mAudioThread.getmLEDs().setAll(20, 20, 20);
                        mAudioThread.getmLEDs().updateAll();
                    }
                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn();
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff();
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices();
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover();
                }
            });
        }
    }

    // Make toasts signaling bluetooth turned on or off
    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText(getString(R.string.BTEnable));
            Toast.makeText(getApplicationContext(),getString(R.string.sBTturON),Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),getString(R.string.BTisON), Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText(getString(R.string.sEnabled));
            }
            else
                mBluetoothStatus.setText(getString(R.string.sDisabled));
        }
    }

    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText(getString(R.string.sBTdisabl));
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    // During discovery, register a "listener" class that will update our listView by adding items
    // to its model (the mBTArrayAdapter) once a notification is made that a bluetooth device was found
    private void discover(){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),getString(R.string.DisStop),Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), getString(R.string.DisStart), Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), getString(R.string.BTnotOn), Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Checks if device is a Decibility device, if so it is shown in the list
                if(device.getName() != null && device.getName().matches("(?i).*decibility.*")) {
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mBTArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Get the already paired devices
    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), getString(R.string.show_paired_devices), Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), getString(R.string.BTnotOn), Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        final AppCompatActivity activity = ConnectDevicePage.this;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), getString(R.string.BTnotOn), Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText(getString(R.string.cConnet));
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        mAudioThread = new AudioThread(mBTSocket, activity);
                        mAudioThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}
