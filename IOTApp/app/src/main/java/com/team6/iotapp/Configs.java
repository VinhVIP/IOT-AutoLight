package com.team6.iotapp;

public class Configs {
    public static final int INIT = 1;

    public static final int LED_TURN_OFF = 20;
    public static final int LED_TURN_ON = 21;
    public static final int LED_AUTO_LIGHT_ON = 22;
    public static final int LED_AUTO_LIGHT_OFF = 23;

    public static final int RECEIVE_TEMP = 10;
    public static final int RECEIVE_HUM = 11;
    public static final int RECEIVE_LIGHT = 12;

    public final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

}
