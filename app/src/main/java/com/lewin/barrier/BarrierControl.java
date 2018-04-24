package com.lewin.barrier;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BarrierControl extends AppCompatActivity {

    Button btnOpen, btnDis;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        new ConnectBT().execute();
        setContentView(R.layout.activity_main);

        btnOpen = (Button) findViewById(R.id.open_button);
        btnDis = (Button) findViewById(R.id.dis_button);

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBarrier();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeBarrier();
                        //Do something after 100ms
                    }
                }, 2000);
            }
        });


        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
                finish();
            }
        });
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSucces = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BarrierControl.this, "Connecting...", "Please wait!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSucces = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSucces) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void closeBarrier() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("TF".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openBarrier() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("TO".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
