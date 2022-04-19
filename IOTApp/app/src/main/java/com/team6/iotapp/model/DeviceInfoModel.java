package com.team6.iotapp.model;

public class DeviceInfoModel {
    private String deviceName, deviceAddress;

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public DeviceInfoModel(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }
}
