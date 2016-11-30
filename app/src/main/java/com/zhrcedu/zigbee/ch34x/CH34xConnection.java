package com.zhrcedu.zigbee.ch34x;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

import com.zhrcedu.zigbee.util.DecimalTypeUtils;
import com.zhrcedu.zigbee.util.ToastUtil;

import java.io.IOException;

import static com.zhrcedu.zigbee.util.DecimalTypeUtils.hexStringToBytes;


/**
 * Created by wugang on 2015/11/13.
 * <p>
 * CH34x芯片OTG通信
 */
public class CH34xConnection {

    public static String DEVICE_CH340 = "1a86:7523";
    public static String DEVICE_CH341 = "1a86:5523";
    private UsbManager mUsbmanager;
    private Context mContext;
    private String mString;
    private String mDeviceStr;

    private byte[] readBuffer;
    private byte[] writeBuffer;
    private int actualNumBytes;
    public boolean READ_ENABLE = false;
    private boolean UARTINITFLAG = false;

    private CH34xAndroidDriver uartInterface_ch34;
    private readThread_ch34x handlerThread_ch34x;

    protected final Object ThreadLock = new Object();

    private ICH34xConnection mMessageListener;

    public CH34xConnection(UsbManager manager, Context context, String AppName, String deviceStr) {
        mUsbmanager = manager;
        mContext = context;
        mString = AppName;
        mDeviceStr = deviceStr;

        /* allocate buffer */
        writeBuffer = new byte[1024];
        readBuffer = new byte[1024];

        uartInterface_ch34 = new CH34xAndroidDriver(mUsbmanager, mContext, mString, mDeviceStr);

        if (!READ_ENABLE) {
            READ_ENABLE = true;
            handlerThread_ch34x = new readThread_ch34x(handler);
            handlerThread_ch34x.start();
        }
    }

    public interface ICH34xConnection {
        /**
         * 硬件返回的数据
         *
         * @param message
         */
        void onStrMessage(String message);

        void onBytesToHexStrMessage(String message);
    }

    public void setMessageCH34xListener(ICH34xConnection listener) {
        mMessageListener = listener;
    }

    /**
     * 打开设备
     */
    public void openUsbDevice() {
        if (uartInterface_ch34.isConnected()) {
            UARTINITFLAG = uartInterface_ch34.UartInit();
            ToastUtil.showToast(mContext, "打开成功");
        } else {
            ToastUtil.showToast(mContext, "打开失败，请检查是否连接设备");
        }
    }

    /**
     * 配置通信参数
     *
     * @param baudRate
     * @param dataBit
     * @param stopBit
     * @param parity
     * @param flowControl
     */
    public void setConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        if (UARTINITFLAG) {
            if (uartInterface_ch34.SetConfig(baudRate, dataBit, stopBit, parity, flowControl)) {
                ToastUtil.showToast(mContext, "设置成功");
            } else {
                ToastUtil.showToast(mContext, "设置失败，请检查是否连接设备");
            }
        } else {
            ToastUtil.showToast(mContext, "设置失败，请检查是否连接设备");
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (uartInterface_ch34 != null) {
            if (uartInterface_ch34.isConnected()) {
                uartInterface_ch34.CloseDevice();
            }
            uartInterface_ch34 = null;
        }
    }

    public void stop() {
        if (READ_ENABLE = true)
            READ_ENABLE = false;
    }

    public void resume() {
        if (2 == uartInterface_ch34.ResumeUsbList()) {
            uartInterface_ch34.CloseDevice();
        }
    }

    /**
     * 写入数据,字符串
     *
     * @param message
     */
    public void writeMessage(String message) {
        int numBytes = 0;
        int mLen = 0;
        if (message.length() != 0) {
            numBytes = message.length();
            for (int i = 0; i < numBytes; i++) {
                writeBuffer[i] = (byte) message.charAt(i);
            }
        }
        try {
            mLen = uartInterface_ch34.WriteData(writeBuffer, numBytes);
        } catch (IOException e) {
//            ToastUtil.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
        if (mLen != numBytes) {
//            ToastUtil.showToast(mContext, "写入数据错误");
        }
    }

    /**
     * 写入数据，16进制
     *
     * @param message
     * @param i       为16
     */
    public void writeMessage(String message, int hex) {
        int numBytes = 0;
        int mLen = 0;
        if (hex != 16) {
            return;
        }
        numBytes = message.length();
        try {
            mLen = uartInterface_ch34.WriteData(hexStringToBytes(message), numBytes / 2);
        } catch (IOException e) {
            ToastUtil.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
        if (mLen != numBytes / 2) {
            ToastUtil.showToast(mContext, "写入数据错误");
        }
    }

    /**
     * 写入zigbee参数，需要校验
     *
     * @param message
     * @param hex
     * @param isCheck
     */
    public void writeZigbeeMessage(String message, int hex, boolean isCheck) {
        int numBytes = 0;
        int mLen = 0;
        if (hex != 16) {
            return;
        }
        numBytes = message.length();
        try {
            if (isCheck) {
                mLen = uartInterface_ch34.WriteData(zigbeeCommandCheck(hexStringToBytes(message)), numBytes / 2 + 1);
            } else
                mLen = uartInterface_ch34.WriteData(hexStringToBytes(message), numBytes / 2);
        } catch (IOException e) {
//            ToastUtil.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (actualNumBytes != 0x00) {
                        mMessageListener.onStrMessage(new String(readBuffer, 0, actualNumBytes));
                        mMessageListener.onBytesToHexStrMessage(DecimalTypeUtils.bytesToHexString(readBuffer, actualNumBytes));
                        actualNumBytes = 0;
                    }
                    break;
            }
        }
    };

    /* usb input data handler */
    private class readThread_ch34x extends Thread {
        /* constructor */
        Handler mhandler;

        readThread_ch34x(Handler h) {
            mhandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        public void run() {
            while (READ_ENABLE) {
                Message msg = mhandler.obtainMessage();
                msg.what = 0;
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                }
                synchronized (ThreadLock) {
                    if (uartInterface_ch34 != null) {
                        actualNumBytes = uartInterface_ch34.ReadData(readBuffer, 128);
                        if (actualNumBytes > 0) {
                            mhandler.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理zigbee指令
     * FC 02 91 01 XX XX XY（XY = 前6个字节的和，保留低 8 位，下同）
     *
     * @param olebytes
     * @return
     */
    public static byte[] zigbeeCommandCheck(byte[] olebytes) {
        byte tem = 0;
        byte[] newbytes = new byte[olebytes.length + 1];
        for (int i = 0; i < olebytes.length; i++) {
            tem = (byte) (tem + olebytes[i]);
        }
        for (int j = 0; j < newbytes.length; j++) {
            if (j == olebytes.length)
                newbytes[j] = (byte) (tem & 0xff);
            else
                newbytes[j] = olebytes[j];
        }
        return newbytes;
    }

}
