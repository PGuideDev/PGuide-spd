package org.pguide;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class GetRfidSelf {

    public static void main(String[] args) {
//        sendCommand(CommonUtils.findCards_02("5BD1834D500104E0"));

        sendCommand(CommonUtils.findCards_01());
        //String card="5BD1834D500104E0";
        //String data="00000004";
        //int start_from=2;
        //int len=6+card.length()+data.length();
        //System.out.println(String.format("%02X",len));
        //sendCommand(CommonUtils.WriteSingleBilock_08(card, start_from, data));
    }


    /**
     * =================================================发送指令
     *
     * 配置CommonUtils 使用
     */

    public static void sendCommand(){
        SerialPort serialPort = StartPort();

        String command = CommonUtils.findCards_01();

        SendPortData(serialPort, command.getBytes());
        byte[] data = null;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        data = ReadComData(serialPort);

        String dataOriginal = new String(data);

        System.out.println("reader返回pc原始数据" + dataOriginal);


        String[] split = dataOriginal.split("");

        ArrayList<String> list = new ArrayList<>();
        for (int i = 2; i < split.length-1; i+=2) {
            System.out.print(split[i] + split[i+1]+" | ");
            list.add(split[i] + split[i+1]);
        }
;
        System.out.println("");

        for (int i = 0; i < list.size()-1; i++) {
//            String hexString = Integer.toHexString(Integer.parseInt(list.get(i),16));
            System.out.print(Integer.parseInt(list.get(i),16) + " | ");
//            System.out.println(Integer.decode(list.get(i)));
        }
        System.out.println("");


        ArrayList<String> strings = dealMsg(dataOriginal, "01");

        // TODO 卡号处理
    }

    public static void sendCommand(String command){
        SerialPort serialPort = StartPort();

        SendPortData(serialPort, command.getBytes());
        byte[] data = null;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        data = ReadComData(serialPort);

        String dataOriginal = new String(data);

        System.out.println("reader返回pc原始数据" + dataOriginal);
        ArrayList<String> strings = dealMsg(dataOriginal, command.substring(3,5));



        // TODO 卡号处理
    }

    /**
     * =================================================切换天线
     * @param nums
     */
    public static void checkPath(int nums){
        SerialPort serialPort = StartPort();
        String command = CommonUtils.checkRoad(nums);

        SendPortData(serialPort, command.getBytes());
        byte[] data = null;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        data = ReadComData(serialPort);

        String dataOriginal = new String(data);

        System.out.println("reader返回pc原始数据" + dataOriginal);
        System.out.println("目前已经切换到："+nums);

    }



    private static ArrayList<String> dealMsg(String dataOriginal, String type) {
       if (type.equals("01")){
           return deal01(dataOriginal);
       }else if (type.equals("check")){

       }

       return null;
    }

    public static ArrayList<String> deal01(String dataOriginal){
        String substring = dataOriginal.substring(9);
        String substring1 = substring.substring(0, substring.length() - 6);
        System.out.println("截取后的数据"+substring1);
        System.out.println(substring1.length());
        ArrayList<String> list = new ArrayList<>();
        while(substring1.length()>1){
            String substring2 = substring1.substring(0, 20);
            char[] charArray = substring2.toCharArray();
            int left = 0;
            int right = charArray.length-1;
            while(left<right){
                char temp = charArray[left];
                char temp2 = charArray[left+1];
                charArray[left] = charArray[right-1];
                charArray[left+1] = charArray[right];
                charArray[right] = temp2;
                charArray[right-1] = temp;
                left += 2;
                right -=2;
            }
            list.add(new String(charArray));
            substring1 = substring1.substring(20);
        }
        for (String string : list) {
            System.out.println(string);
        }

        return list;
    }
    public static void deal02(String command){
        System.out.println("目前已切换到：");
    }


    /**
     * 其余公共配置
     */


    public static SerialPort StartPort(){
        GetRfidSelf getRfidSelf = new GetRfidSelf();
        SerialPort serialPort = getRfidSelf.OpenSerial("COM3", 115200);
        return serialPort;
    }


    //1 打开串口函数
    public static SerialPort OpenSerial(String serialname, int baudrate) {
        SerialPort serialPort = null;
        CommPort commPort = null;
        try {
            CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(serialname);
            try {
                commPort = commPortIdentifier.open(serialname, 2000);
                serialPort = (SerialPort) commPort;
                try {
                    serialPort.setSerialPortParams
                            (baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    System.out.println("已打开：" + commPort);
                } catch (UnsupportedCommOperationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (PortInUseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NoSuchPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return serialPort;
    }

    //往端口发送数据函数
    public static void SendPortData(SerialPort serialPort, byte[] data) {
        OutputStream outputStream = null;
        try {
            outputStream = serialPort.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            //此句的目的是在调试的时候观察数据是否发送到串口
            System.out.println("向串口发送数据发送成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public static byte[] ReadComData(SerialPort serialPort) {
        byte[] bytes = null;
        InputStream inputStream = null;
        try {
            inputStream = serialPort.getInputStream();
            int buff = inputStream.available();
            System.out.println("this is buff"+buff);
            Thread.sleep(1000);
            while (buff != 0) {
                bytes = new byte[buff];
                inputStream.read(bytes);
                buff = inputStream.available();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }

}
