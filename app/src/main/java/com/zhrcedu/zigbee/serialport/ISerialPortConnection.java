package com.zhrcedu.zigbee.serialport;

/**
 * Created by wugang on 2015/7/23.
 */
public interface ISerialPortConnection {
    void onStart(String startResult);
    void onMessage(String message);
    void onHexStrMessage(String hexMessage);
}
