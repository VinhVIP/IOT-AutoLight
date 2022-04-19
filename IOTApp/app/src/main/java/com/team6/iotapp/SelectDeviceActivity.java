package com.team6.iotapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.team6.iotapp.adapter.DeviceAdapter;
import com.team6.iotapp.databinding.ActivitySelectDeviceBinding;
import com.team6.iotapp.model.DeviceInfoModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {

    private ActivitySelectDeviceBinding binding;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupBluetooth();
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        List<DeviceInfoModel> deviceList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                DeviceInfoModel model = new DeviceInfoModel(deviceName, deviceAddress);
                deviceList.add(model);
            }

            binding.recycler.setLayoutManager(new LinearLayoutManager(this));
            DeviceAdapter adapter = new DeviceAdapter(this, deviceList);
            binding.recycler.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Không có thiết bị nào", Toast.LENGTH_SHORT).show();
        }

    }
}