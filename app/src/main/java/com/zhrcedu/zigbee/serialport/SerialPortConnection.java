package com.zhrcedu.zigbee.serialport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.friendlyarm.AndroidSDK.HardwareControler;

import java.io.UnsupportedEncodingException;

/**
 * 窗口通信类 注意：需先setMessageListener，然后connect Created by wugang on 2016/3/18
 */
public class SerialPortConnection {
    private ISerialPortConnection iSPConnection;
    private int fd;
    private Thread mReadThread;

    byte[] buf = new byte[512];
    public static SerialPortConnection mSPConnection;

    public static SerialPortConnection getInstance() {
        if (mSPConnection == null) {
            mSPConnection = new SerialPortConnection();
        }
        return mSPConnection;
    }

    public void setMessageListener(ISerialPortConnection iSPConnection) {
        this.iSPConnection = iSPConnection;
    }

    public void connect(int fda, String devName, long baud, int dataBits, int stopBits) {
        this.fd = fda;
        if (fd == -1)
            fd = HardwareControler.openSerialPort(devName, baud, dataBits, stopBits);
        if (fd != -1) {
            iSPConnection.onStart("串口启动成功");
        } else {
            iSPConnection.onStart("串口启动失败");
        }
        if (mReadThread == null) {
            final int finalFd = fd;
            mReadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (HardwareControler.select(finalFd, 0, 0) == 1) {
                            int retSize = HardwareControler.read(finalFd, buf, buf.length);
                            if (retSize > 0) {
                                String mStrMessage = new String(buf, 0, retSize);
                                String mHexStrMessage = bytesToHexString(buf, retSize);
//                                Log.d(Thread.currentThread().getId() + "", "mStrMessage: " + mStrMessage + " || mHexStrMessage: " + mHexStrMessage);
                                Message msg = new Message();
                                msg.what = 100;
                                Bundle bundle = new Bundle();
                                bundle.putString("data", mStrMessage);
                                bundle.putString("data_hex", mHexStrMessage);
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            });
            mReadThread.start();
        }

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    if (iSPConnection == null)
                        break;
                    iSPConnection.onMessage(msg.getData().getString("data"));
                    iSPConnection.onHexStrMessage(msg.getData().getString("data_hex"));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 向串口发送数据 wug
     *
     * @param str 字符串
     */
    public void write(String str) {
        try {
            HardwareControler.write(fd, str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向串口发送数据 wug
     *
     * @param str 字符串
     */
    public void writeByte(String str, int delaytime) {
        try {
            Thread.sleep(delaytime);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HardwareControler.write(fd, hexStringToBytes(str));
    }

    /**
     * 写入zigbee参数，需要校验
     *
     * @param message
     * @param hex
     * @param isCheck
     */
    public void writeZigbeeMessage(String message, int hex, boolean isCheck) {
        if (hex != 16) {
            return;
        }
        if (isCheck) {
            HardwareControler.write(fd, zigbeeCommandCheck(hexStringToBytes(message)));
        } else
            HardwareControler.write(fd, hexStringToBytes(message));
    }

    /**
     * 延时向串口发送数据
     *
     * @param str
     * @param delaytime
     */
    public void write(final String str, final int delaytime) {
        try {
            Thread.sleep(delaytime);
            HardwareControler.write(fd, str.getBytes("utf-8"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放串口资源
     */
    public void release() {
        HardwareControler.close(fd);
        fd = -1;
        mReadThread = null;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src, int retSize) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < retSize; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
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
