package com.team6.iotapp.thread;

import static com.team6.iotapp.Configs.MESSAGE_READ;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.team6.iotapp.Configs;
import com.team6.iotapp.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ConnectedThread(BluetoothSocket socket) {
        this.socket = socket;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            inputStream = null;
            outputStream = null;
        }

    }

    @Override
    public void run() {

        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];

        int cmd;
        while (true) {
            try {
                if ((cmd = inputStream.read()) != -1) {
                    int value;
                    Log.d("arduino", cmd + "");

                    switch (cmd) {
                        case Configs.RECEIVE_TEMP:
                            value = inputStream.read();
                            MainActivity.handler.obtainMessage(MESSAGE_READ, "T" + value).sendToTarget();
                            break;
                        case Configs.RECEIVE_HUM:
                            value = inputStream.read();
                            MainActivity.handler.obtainMessage(MESSAGE_READ, "H" + value).sendToTarget();
                            break;
                        case Configs.RECEIVE_LIGHT:
                            int length = inputStream.read();
                            String s = "";
                            while (length-- > 0) {
                                value = inputStream.read();
                                s = value + s;
                            }
                            MainActivity.handler.obtainMessage(MESSAGE_READ, "I" + s).sendToTarget();
                            break;
                        case Configs.LED_TURN_ON:
                            MainActivity.handler.obtainMessage(MESSAGE_READ, "LED_ON").sendToTarget();
                            break;
                        case Configs.LED_TURN_OFF:
                            MainActivity.handler.obtainMessage(MESSAGE_READ, "LED_OFF").sendToTarget();
                            break;
                        default:
                            break;
                    }
                }


//                    buffer[bytes] = (byte) mmInStream.read();
//                    String readMessage;
//                    if (buffer[bytes] == '\n') {
//                        readMessage = new String(buffer, 0, bytes);
//                        Log.d("Arduino Message", readMessage);
//                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
//                        bytes = 0;
//                    } else {
//                        bytes++;
//                    }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void writeString(String input) {
        byte[] bytes = input.getBytes();
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            Log.e("Send Error", "Unable to send message", e);
        }
        Log.d("Arduino", "Send ok");
    }

    public void writeInt(int input) {
        try {
            outputStream.write(input);
        } catch (IOException e) {
            Log.e("Send Error", "Unable to send message", e);
        }
        Log.d("Arduino", "Send input ok");
    }


    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }
}
