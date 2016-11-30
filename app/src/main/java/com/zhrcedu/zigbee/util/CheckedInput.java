package com.zhrcedu.zigbee.util;

/**
 * Created by wugang on 2016/10/21.
 */

public class CheckedInput {
    /**
     * 检查输入的硬件地址是否正确(000~999)
     *
     * @param hardAddr
     * @return
     */
    public static String checkHardAddr(String hardAddr) {
        if (hardAddr.length() != 3)
            return "地址长度错误，请输入(000~999)";
        char[] ch = hardAddr.toCharArray();
        for (int i = 1; i < ch.length; i++) {
            if (ch[i] < '0' || ch[i] > '9')
                return "地址格式错误，请输入(000~999)";
        }
        return "OK";
    }

    /**
     * 检查输入的值是否正确(000~999)
     *
     * @param val
     * @return
     */
    public static String checkVal(String val) {
        if (val.length() != 3)
            return "值长度错误，请输入(000~999)";
        char[] ch = val.toCharArray();
        for (int i = 1; i < ch.length; i++) {
            if (ch[i] < '0' || ch[i] > '9')
                return "值格式错误，请输入(000~999)";
        }
        return "OK";
    }

    /**
     * 检查输入的panid是否正确
     *
     * @param panid
     * @return
     */
    public static String checkZigbeePanid(String panid) {
        if (panid.length() != 4)
            return "PANID长度错误";
        char[] ch = panid.toCharArray();
        for (int i = 1; i < ch.length; i++) {
            if (ch[i] >= '0' && ch[i] <= '9')
                continue;
            if (!((ch[i] >= 'a' && ch[i] <= 'f') || (ch[i] >= 'A' && ch[i] <= 'F')))
                return "PANID错误，请输入四位十六进制数";
        }
        if (panid.equals("ffff") || panid.equals("fffe") || panid.equals("FFFF") || panid.equals("FFFE"))
            return "PANID不可为FFFF或FFFE";
        return "OK";
    }
}
