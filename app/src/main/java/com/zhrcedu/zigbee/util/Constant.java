package com.zhrcedu.zigbee.util;

/**
 * Created by wugang on 2016/6/23.
 */

public class Constant {

    /**
     * zigbee配置指令
     */
    public static class ZigbeeCommands {
        public static final String READ_PANID = "FC009103A3B3";
        public static final String READ_CHANNEL = "FC00910D342B";
        public static final String READ_NODETYPE = "FC00910BCBEB";
        public static final String READ_SHORTADD = "FC009104C4D4";
        public static final String READ_MACADD = "FC009108A8B8";
        public static final String RESTART = "FC0091876A35";
        public static final String WRITE_PANID = "FC029101XXXX";
        public static final String WRITE_CHANNEL = "FC01910CXX1A";
        public static final String WRITE_NODETYPE_COORDINATOR = "FC009109A9C9";
        public static final String WRITE_NODETYPE_ROUTER = "FC00910ABADA";
        public static final String READ_HARDADD = "getaddress";
    }
}
