package com.team6.iotapp;

import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static com.team6.iotapp.Configs.CONNECTING_STATUS;
import static com.team6.iotapp.Configs.MESSAGE_READ;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.team6.iotapp.databinding.ActivityMainBinding;
import com.team6.iotapp.thread.ConnectedThread;
import com.team6.iotapp.thread.CreateConnectThread;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View thumbView;

    private String deviceName, deviceAddress;

    public static Handler handler;
    public static CreateConnectThread createConnectThread;
    public static ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH, BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE}, 999);
            }
        }

        setSupportActionBar(binding.toolbar);

        binding.swTurnLed.setEnabled(false);
        binding.swAutoLight.setEnabled(false);

        getData();
        handler();
        setupControls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 999) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("vinh", "ok permission");
//            } else {
//                Toast.makeText(this, "Chưa cấp quyền sử dụng bluetooth", Toast.LENGTH_LONG).show();
//            }
//        }
    }

    private void getData() {
        Intent data = getIntent();
        deviceName = data.getStringExtra("deviceName");

        if (deviceName != null) {
            deviceAddress = data.getStringExtra("deviceAddress");
            Toast.makeText(this, "Đang kết nối tới " + deviceName + " : " + deviceAddress, Toast.LENGTH_SHORT).show();

            binding.toolbar.setSubtitle("Đang kết nối tới " + deviceName + "...");
            binding.buttonConnect.setEnabled(false);

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, this);
            createConnectThread.start();
        }

    }

    private void setupControls() {
        binding.buttonConnect.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
        });

        binding.swTurnLed.setOnClickListener(v -> {
            Log.d("arduino", "sw on click");

            if (binding.swTurnLed.isChecked()) {
                connectedThread.writeInt(Configs.LED_TURN_ON);
            } else {
                connectedThread.writeInt(Configs.LED_TURN_OFF);
            }
        });

        binding.swAutoLight.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                connectedThread.writeInt(Configs.LED_AUTO_LIGHT_ON);
                binding.seekBarLight.setEnabled(false);
            } else {
                connectedThread.writeInt(Configs.LED_AUTO_LIGHT_OFF);
                connectedThread.writeInt(binding.seekBarLight.getProgress() * 10);
                binding.seekBarLight.setEnabled(true);
            }
        });

        thumbView = LayoutInflater.from(this).inflate(R.layout.thumb, null, false);
        binding.seekBarLight.setThumb(getThumb(5));

        binding.seekBarLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.seekBarLight.setThumb(getThumb(i));
                if (connectedThread == null) return;

                connectedThread.writeInt(Configs.LED_AUTO_LIGHT_OFF);
                connectedThread.writeInt(i * 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void handler() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                binding.toolbar.setSubtitle("Đã kết nối tới " + deviceName);
                                binding.buttonConnect.setEnabled(true);
                                binding.swTurnLed.setEnabled(true);
                                binding.swAutoLight.setEnabled(true);
                                // init data
                                connectedThread.writeInt(Configs.INIT);
                                break;
                            case -1:
                                binding.toolbar.setSubtitle("Kết nối thất bại");
                                binding.buttonConnect.setEnabled(true);
                                binding.swTurnLed.setEnabled(false);
                                binding.swAutoLight.setEnabled(false);
                                break;
                        }
                        break;
                    case MESSAGE_READ:
                        String mess = msg.obj.toString();
                        Log.d("Arduino", mess);

                        if (mess.startsWith("T")) {
                            binding.tvTemperature.setText(mess.substring(1) + "°C");
                        } else if (mess.startsWith("H")) {
                            binding.tvHumidity.setText(mess.substring(1) + "%");
                        } else if (mess.startsWith("I")) {
                            binding.tvLightIntensity.setText(mess.substring(1) + " lux");
                        } else if (mess.equalsIgnoreCase("LED_ON")) {
                            binding.swTurnLed.setChecked(true);
                        } else if (mess.equalsIgnoreCase("LED_OFF")) {
                            binding.swTurnLed.setChecked(false);
                        } else if (mess.equalsIgnoreCase("LED_AUTO_LIGHT_ON")) {
                            binding.swAutoLight.setChecked(true);
                        } else if (mess.equalsIgnoreCase("LED_AUTO_LIGHT_OFF")) {
                            binding.swAutoLight.setChecked(false);
                        }
                        break;
                }
            }
        };
    }

    private Drawable getThumb(int progress) {
        TextView tv = thumbView.findViewById(R.id.tvProgress);
        tv.setText(progress * 10 + "%");
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    public void onBackPressed() {
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}