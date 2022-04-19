package com.team6.iotapp.thread;

import static com.team6.iotapp.Configs.CONNECTING_STATUS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.team6.iotapp.MainActivity;

import java.io.IOException;
import java.util.UUID;

public class CreateConnectThread extends Thread {

    public BluetoothSocket socket;

    public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Context context) {

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Arduino", "Chưa cấp quyền");
        }

        try {
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
            socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();

        try {
            socket.connect();
            Log.d("arduino", "socket connected");

            MainActivity.handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();

            try {
                socket.close();
                MainActivity.handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        MainActivity.connectedThread = new ConnectedThread(socket);
        MainActivity.connectedThread.start();
        Log.d("Arduino", "run connected thread");
    }

    public void cancel() {
        try {
            MainActivity.connectedThread.cancel();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
