package com.zhrcedu.zigbee.util;

/**
 * Created by wugang on 2016/7/27.
 */

public class DecimalTypeUtils {
    /**
     * int到byte[](由高位到低位)
     *
     * @param i
     * @return
     */
    public static byte[] intToByteArray_bigToLittle(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * int到byte[](由低位到高位)
     *
     * @param i
     * @return
     */
    public static byte[] iToB_lToB(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * int到byte[](由低位到高位),返回两字节
     *
     * @param i
     * @return
     */
    public static byte[] iToB_2bit_lTob(int i) {
        byte[] result = new byte[2];
        //由高位到低位
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * byte[]转int(由高位到低位)
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt_bigToLittle(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    /**
     * byte[]转int(由低位到高位)
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt_littleToBig(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = i * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    /**
     * byte[]转int(由低位到高位)
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt_2bit_littleToBig(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 2; i++) {
            int shift = i * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    /**
     * TCP232-E45 协议校验
     * 包头（FF）+ 指令长度 + 命令 + Mac地址 + 用户名密码 + 参数 + 校验位
     * 校验位为和校验，从 长度字节（包含长度）开始，加到校验之前（不包含校验）为止，结果为校验值，只保留低字节。
     *
     * @param command
     * @return
     */
    public static String tcp232CommandCheck(String command) {
        byte tem = 0;
        byte[] temp = new byte[1];
        byte[] b = DecimalTypeUtils.hexStringToBytes(command.substring(2, command.length()));
        for (int i = 0; i < b.length; i++) {
            tem += b[i];
        }
        temp[0] = (byte) (tem & 0xff);
        return command + DecimalTypeUtils.bytesToHexString(temp, temp.length);
    }

    /**
     * 十六进制字符串转换成字节数组
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.replace(" ", "");
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
     * 字节数组转换成十六进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src, int actualNumBytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || actualNumBytes <= 0) {
            return null;
        }
        for (int i = 0; i < actualNumBytes; i++) {
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
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 高频RFID读写器校验和。从 FrameLen 开始到 Info 的最后一字节异或取反，C
     * 语言程序描述如下：（SerBfr 为一帧数据缓冲区首址）
     * BCC = 0;
     * for(i=0; i<(SerBfr[0]-2); i++) {
     * BCC ^= SerBfr[i];
     * }
     * SerBfr[SerBfr[0]-2] = ~BCC;
     *
     * @param str
     * @return
     */
    public static String getBCC(String str) {
        byte[] serBfr = DecimalTypeUtils.hexStringToBytes(str);
        byte[] temp = new byte[1];
        byte bcc = 0;
        for (int i = 0; i < serBfr.length; i++) {
            bcc ^= serBfr[i];
        }
        temp[0] = (byte) ~bcc;
        return DecimalTypeUtils.bytesToHexString(temp, 1);
    }

    /**
     * 得到CRC16校验值
     */
    public static String getCrC16(String str) {
        byte[] bytes = DecimalTypeUtils.hexStringToBytes(str.replace(" ", ""));
        int[] table = {
                0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
                0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
                0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
                0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
                0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
                0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
                0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
                0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
                0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
                0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
                0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
                0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
                0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
                0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
                0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
                0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
                0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
                0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
                0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
                0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
                0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
                0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
                0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
                0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
                0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
                0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
                0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
                0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
                0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
                0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
                0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
                0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
        };

        int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        //低位在前，特殊处理，项目需求
        String result = Integer.toHexString(crc);
        if (result.length() != 4)
            result = "0000".substring(0, 4 - result.length()) + result;
        result = result.substring(2, 4) + result.substring(0, 2);

        return result;
    }

    /**
     * 得到CRC值,返回结果 str+crc
     */
    public static String getCRC(String str) {
        long lon = GetModBusCRC(str
                .replace(" ", ""));
        int h1, l0;
        l0 = (int) lon / 256;
        h1 = (int) lon % 256;
        String s = "";
        if (Integer.toHexString(h1).length() < 2) {
            s = "0" + Integer.toHexString(h1);
        } else {
            s = Integer.toHexString(h1);
        }
        if (Integer.toHexString(l0).length() < 2) {
            s = s + "0" + Integer.toHexString(l0);
        } else {
            s = s + Integer.toHexString(l0);
        }
        return str + s;
    }

    /**
     * 描述：获取CRC校验值
     *
     * @param DATA
     * @return
     */
    public static long GetModBusCRC(String DATA) {
        long functionReturnValue = 0;
        long i = 0;
        long J = 0;
        int[] v = null;
        byte[] d = null;
        v = strToToHexByte(DATA);

        long CRC = 0;
        CRC = 0xffffL;
        for (i = 0; i <= (v).length - 1; i++) { // 1.把第一个8位二进制数据（既通讯信息帧的第一个字节）与16位的CRC寄存器的低8位相异或，把结果放于CRC寄存器；
            CRC = (CRC / 256) * 256L + (CRC % 256L) ^ v[(int) i];
            for (J = 0; J <= 7; J++) { // 2.把CRC寄存器的内容右移一位（朝低位）用0填补最高位，并检查最低位；
                // 3.如果最低位为0：重复第3步（再次右移一位）；
                // 如果最低位为1：CRC寄存器与多项式A001（1010 0000 0000 0001）进行异或；
                // 4.重复步骤3和4，直到右移8次，这样整个8位数据全部进行了处理；
                long d0 = 0;
                d0 = CRC & 1L;
                CRC = CRC / 2;
                if (d0 == 1)
                    CRC = CRC ^ 0xa001L;
            } // 5.重复步骤2到步骤5，进行通讯信息帧下一字节的处理；
        } // 6.最后得到的CRC寄存器内容即为：CRC码。
        CRC = CRC % 65536;
        functionReturnValue = CRC;
        return functionReturnValue;
    }

    private static int[] strToToHexByte(String hexString) {
        hexString = hexString.replace(" ", "");
        // 如果长度不是偶数，那么后面添加空格。

        if ((hexString.length() % 2) != 0) {
            hexString += " ";
        }

        // 定义数组，长度为待转换字符串长度的一半。
        int[] returnBytes = new int[hexString.length() / 2];

        for (int i = 0; i < returnBytes.length; i++)
            returnBytes[i] = (0xff & Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16));
        return returnBytes;
    }
}
