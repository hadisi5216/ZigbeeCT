package com.zhrcedu.zigbee;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.zhrcedu.zigbee.ch34x.CH34xConnection;
import com.zhrcedu.zigbee.util.CheckedInput;
import com.zhrcedu.zigbee.util.Constant;
import com.zhrcedu.zigbee.util.DecimalTypeUtils;
import com.zhrcedu.zigbee.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static String ACTION_USB_PERMISSION = "com.zhrcedu.sensinglayercontroller.USB_PERMISSION";

    @BindView(R.id.baud_rate_spinner)
    Spinner baudRateSpinner;
    @BindView(R.id.stop_bits_spinner)
    Spinner stopBitsSpinner;
    @BindView(R.id.data_bits_spinner)
    Spinner dataBitsSpinner;
    @BindView(R.id.parity_spinner)
    Spinner paritySpinner;
    @BindView(R.id.flow_spinner)
    Spinner flowSpinner;
    @BindView(R.id.read_panid_tv)
    TextView readPanidTv;
    @BindView(R.id.read_channel_tv)
    TextView readChannelTv;
    @BindView(R.id.read_nodetype_tv)
    TextView readNodetypeTv;
    @BindView(R.id.read_shortadd_tv)
    TextView readShortaddTv;
    @BindView(R.id.read_macadd_tv)
    TextView readMacaddTv;
    @BindView(R.id.read_hardadd_tv)
    TextView readHardaddTv;
    @BindView(R.id.write_panid_et)
    EditText writePanidEt;
    @BindView(R.id.write_nodechannel_spinner)
    Spinner writeNodechannelSpinner;
    @BindView(R.id.write_nodetype_spinner)
    Spinner writeNodetypeSpinner;
    @BindView(R.id.write_item_rg)
    RadioGroup writeItemRg;
    @BindView(R.id.write_tip)
    TextView writeTip;
    @BindView(R.id.write_panid_rbtn)
    RadioButton writePanidRbtn;
    @BindView(R.id.write_channel_rbtn)
    RadioButton writeChannelRbtn;
    @BindView(R.id.write_nodetype_rbtn)
    RadioButton writeNodetypeRbtn;
    @BindView(R.id.write_param_btn)
    Button writeParamBtn;

    /* local variables */
    protected int baudRate; /* baud rate */
    protected byte stopBit; /* 1:1stop bits, 2:2 stop bits */
    protected byte dataBit; /* 8:8bit, 7: 7bit */
    protected byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    protected byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
    protected int portNumber; /* port number */

    //消息类型
    private enum MessageFlagEnum {
        READ_PANID, READ_CHANNEL, READ_NODETYPE, READ_SHORTADD, READ_MACADD, WRITE_PANID, WRITE_CHANNEL, WRITE_NODETYPE, DEFAULT
    }

    private MessageFlagEnum flagEnum = MessageFlagEnum.DEFAULT;
    private String writeChannel;
    private String writeNodeType;

    private CH34xConnection mCH34xConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        doSpinnerItemSelecte();
        connectionCh34x();
        writeItemRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == writePanidRbtn.getId())
                    flagEnum = MessageFlagEnum.WRITE_PANID;
                else if (checkedId == writeChannelRbtn.getId())
                    flagEnum = MessageFlagEnum.WRITE_CHANNEL;
                else if (checkedId == writeNodetypeRbtn.getId()) {
                    flagEnum = MessageFlagEnum.WRITE_NODETYPE;
                }
            }
        });
    }

    @OnClick({R.id.open_btn, R.id.config_btn, R.id.read_param_btn, R.id.write_param_btn, R.id.restart_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_btn:
                mCH34xConnection.openUsbDevice();
                break;
            case R.id.config_btn:
                mCH34xConnection.setConfig(baudRate, dataBit, stopBit, parity, flowControl);
                break;
            case R.id.read_param_btn:
                readParam();
                break;
            case R.id.write_param_btn:
                writeParam();
                break;
            case R.id.restart_btn:
                clearShow();
                mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.RESTART, 16, true);
                break;
        }
    }

    /**
     * 写入参数（PAN_ID，频道，节点类型）
     */
    private void writeParam() {
        switch (flagEnum) {
            case WRITE_PANID:
                String checkZigbeePanidResult = CheckedInput.checkZigbeePanid(writePanidEt.getText().toString().trim());
                if (checkZigbeePanidResult.equals("OK"))
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.WRITE_PANID.replace("XXXX", writePanidEt.getText().toString().trim()), 16, true);
                else
                    ToastUtil.showToast(this, checkZigbeePanidResult);
                break;
            case WRITE_CHANNEL:
                byte[] bt1 = new byte[1];
                bt1[0] = (byte) (Integer.parseInt(writeChannel) & 0xff);
                mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.WRITE_CHANNEL.replace("XX", DecimalTypeUtils.bytesToHexString(bt1, bt1.length)), 16, true);
                break;
            case WRITE_NODETYPE:
                if (writeNodeType.equals("Router"))
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.WRITE_NODETYPE_ROUTER, 16, true);
                else if (writeNodeType.equals("Coordinator"))
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.WRITE_NODETYPE_COORDINATOR, 16, true);
                break;
        }
    }

    /**
     * 读取参数（PAN_ID，频道，节点类型，短地址，MAC地址）
     */
    private void readParam() {
        clearShow();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.READ_PANID, 16, true);
                    flagEnum = MessageFlagEnum.READ_PANID;
                    Thread.sleep(500);
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.READ_CHANNEL, 16, true);
                    flagEnum = MessageFlagEnum.READ_CHANNEL;
                    Thread.sleep(500);
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.READ_NODETYPE, 16, true);
                    flagEnum = MessageFlagEnum.READ_NODETYPE;
                    Thread.sleep(500);
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.READ_SHORTADD, 16, true);
                    flagEnum = MessageFlagEnum.READ_SHORTADD;
                    Thread.sleep(500);
                    mCH34xConnection.writeZigbeeMessage(Constant.ZigbeeCommands.READ_MACADD, 16, true);
                    flagEnum = MessageFlagEnum.READ_MACADD;
                    Thread.sleep(500);
                    mCH34xConnection.writeMessage(Constant.ZigbeeCommands.READ_HARDADD);
                    //默认消息
                    flagEnum = MessageFlagEnum.DEFAULT;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void clearShow() {
        readPanidTv.setText("");
        readChannelTv.setText("");
        readNodetypeTv.setText("");
        readShortaddTv.setText("");
        readMacaddTv.setText("");
        readHardaddTv.setText("");
        writePanidRbtn.setChecked(false);
        writeChannelRbtn.setChecked(false);
        writeNodetypeRbtn.setChecked(false);
        writePanidEt.setText("");
        writeTip.setText("");
    }

    /**
     * 连接CH34x设备
     */
    private void connectionCh34x() {
        mCH34xConnection = new CH34xConnection((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), ACTION_USB_PERMISSION, CH34xConnection.DEVICE_CH340);
        mCH34xConnection.setMessageCH34xListener(new CH34xConnection.ICH34xConnection() {
            @Override
            public void onStrMessage(String message) {
//                Log.d("onStrMessage::::::::::::::::", message);
                if (message.startsWith("rptaddress")) {
                    readHardaddTv.setText(message.split("=")[1]);
                }
            }

            @Override
            public void onBytesToHexStrMessage(String message) {
//                Log.d("onBytesToHexStrMessage::::::::::::::::", message);
                switch (flagEnum) {
                    case READ_PANID:
                        readPanidTv.setText(message);
                        break;
                    case READ_CHANNEL:
                        if (message.length() != 12)
                            break;
                        //最后两位十六进制转换为十进制是channel号
                        byte[] cha = DecimalTypeUtils.hexStringToBytes(message.substring(message.length() - 2, message.length()));
                        readChannelTv.setText("" + (cha[0] & 0xff));
                        break;
                    case READ_NODETYPE:
                        //如果是 Coordinator，返回：43 6F 6F 72 64 69如果是 Router，返回：52 6F 75 74 65 72
                        if (message.equals("436f6f726469"))
                            readNodetypeTv.setText("Coordinator");
                        if (message.equals("526f75746572"))
                            readNodetypeTv.setText("Router");
                        break;
                    case READ_SHORTADD:
                        readShortaddTv.setText(message);
                        break;
                    case READ_MACADD:
                        readMacaddTv.setText(message);
                        break;
                    case WRITE_PANID:
                        if (message.equals("fa161718191a72"))
                            writeTip.setText("禁止重设置Coordinate的PAN ID为相同的值");
                        else
                            writeTip.setText("设置新PAN ID成功，重启生效");
                        break;
                    case WRITE_CHANNEL:
                        if (message.length() != 10)
                            break;
                        //最后两位十六进制转换为十进制是channel号
                        byte[] cha1 = DecimalTypeUtils.hexStringToBytes(message.substring(message.length() - 2, message.length()));
                        writeTip.setText("设置新频道" + (cha1[0] & 0xff) + "成功，重启生效");
                        break;
                    case WRITE_NODETYPE:
                        if (writeNodeType.equals("Router"))
                            writeTip.setText("设置为Router成功，重启生效");
                        else if (writeNodeType.equals("Coordinator"))
                            writeTip.setText("设置为Coordinator成功，重启生效");
                        break;
                    case DEFAULT:
                        //默认情况
                        break;
                }
            }

        });
    }

    private void doSpinnerItemSelecte() {
        baudRateSpinner.setSelection(4);
        baudRate = 9600;
        baudRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baudRate = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stopBit = 1;
        stopBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stopBit = (byte) Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dataBitsSpinner.setSelection(1);
        dataBit = 8;
        dataBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataBit = (byte) Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        parity = 0;
        paritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String parityString = new String(parent.getItemAtPosition(position).toString());
                if (parityString.compareTo("none") == 0) {
                    parity = 0;
                } else if (parityString.compareTo("odd") == 0) {
                    parity = 1;
                } else if (parityString.compareTo("even") == 0) {
                    parity = 2;
                } else if (parityString.compareTo("mark") == 0) {
                    parity = 3;
                } else if (parityString.compareTo("space") == 0) {
                    parity = 4;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        flowControl = 0;
        flowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String flowString = new String(parent.getItemAtPosition(position).toString());
                if (flowString.compareTo("none") == 0) {
                    flowControl = 0;
                } else if (flowString.compareTo("CTS/RTS") == 0) {
                    flowControl = 1;
                } else if (flowString.compareTo("DTR/DSR") == 0) {
                    flowControl = 2;
                } else if (flowString.compareTo("XOFF/XON") == 0) {
                    flowControl = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        writeChannel = "11";
        writeNodechannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                writeChannel = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        writeNodeType = "Router";
        writeNodetypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    writeNodeType = "Router";
                else if (position == 1)
                    writeNodeType = "Coordinator";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCH34xConnection != null)
            mCH34xConnection.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCH34xConnection != null)
            mCH34xConnection.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCH34xConnection != null)
            mCH34xConnection.release();
    }
}
