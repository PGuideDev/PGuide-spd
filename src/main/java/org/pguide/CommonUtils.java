package org.pguide;

import org.pguide.row.Crc16;

import java.util.ArrayList;

public class CommonUtils {

    public static void main(String[] args) {
////        findCards_02("E00401504D83D1590000");
//        String string = checkRoad(2);
//        System.out.println(string);
//        String card="5BD1834D500104E0";
//        String data="00000004";
//        int start_from=2;
//
//        WriteSingleBilock_08(card, start_from, data);
    }


    /**
     * 读卡
     * @return
     */
    public static String findCards_01(){
        int start=0x3a; //起始符
        int number=0x01; //帧编号
        int code=0x01;//功能码
        int len_low=0x02;//数据长度低位
        int len_high=0x00;// 数据长度高位
        int cp=0x07;//命令标识
        int[] arr=new int[]{number,code,len_low,len_high,cp};
        Crc16 crc16 = new Crc16();
        String crc = (Integer.toString(crc16.ComputeChecksum(arr), 16).substring(2, 4)
                + Integer.toString(crc16.ComputeChecksum(arr), 16).substring(0, 2));

        String commands="";
        for (int i=0;i<arr.length;i++){
            commands+=String.format("%02X",arr[i]);
        }
        String command = ":" +commands + crc + "\r\n";
        System.out.println("向串口发送数据" + command);
        return command;
    }

    //E00401504D83D1590000
    public static String findCards_02(String card){
        int start=0x3a; //起始符
        int number=0x01; //帧编号
        int code=0x02;//功能码
        int len_low=0x02;//数据长度低位
        int len_high=0x00;// 数据长度高位
        //deal cp
        int[] cp=Tools.changeToHex(card);//命令标识
        cp[0] = 0x23;

        int[] arr=new int[]{number,code,len_low,len_high};
        int[] Arr = new int[arr.length + cp.length];
        for (int i = 0; i < arr.length; i++) {
            Arr[i] = arr[i];
        }
//        for (int i : Arr) {
//            Arr[i] = arr[i];
//        }
        for (int i = 0; i < cp.length; i++) {
            Arr[i+arr.length] = cp[i];
        }
        Crc16 crc16 = new Crc16();
        String crc = (Integer.toString(crc16.ComputeChecksum(Arr), 16).substring(2, 4)
                + Integer.toString(crc16.ComputeChecksum(Arr), 16).substring(0, 2));

        String commands="";
        for (int i=0;i<Arr.length;i++){
            commands+=String.format("%02X",Arr[i]);
        }
        String command = ":" +commands + crc + "\r\n";
        System.out.println("向串口发送数据" + command);
        return command;
    }
    public static String WriteSingleBilock_08(String card,int start_from,String data){
        int start=0x3a; //起始符
        int number=0x35; //帧编号
        int code=0x08;//功能码
        int len_low=6+card.length()+data.length();//数据长度低位
        int len_high=0x00;// 数据长度高位
        int Block_Length=0x04;
        int Command_Flag=0x23;
        int[] commandhead = {number, code, len_low, len_high, Block_Length, Command_Flag,start_from};
        int[] dataInt=Tools.changeToHex(data);
        int[] UID=Tools.changeToHex(card);
        int totalLength = commandhead.length + dataInt.length + UID.length;

        // 创建新数组
        int[] combinedArray = new int[totalLength];

        // 使用 System.arraycopy 依次复制三个数组到新数组中
        System.arraycopy(commandhead, 0, combinedArray, 0, commandhead.length);
        System.arraycopy(dataInt, 0, combinedArray, commandhead.length, dataInt.length);
        System.arraycopy(UID, 0, combinedArray, commandhead.length + dataInt.length, UID.length);
        Crc16 crc16=new Crc16();
        String crc = (Integer.toString(crc16.ComputeChecksum(combinedArray), 16).substring(2, 4)
                + Integer.toString(crc16.ComputeChecksum(combinedArray), 16).substring(0, 2));
        String commands="";
        for (int i=0;i<combinedArray.length;i++){
            commands+=String.format("%02X",combinedArray[i]);
        }
        String command = ":" +commands + crc + "\r\n";
        System.out.println("向串口发送数据" + command);
        return command;

    }

    /**
     * 切换天线
     */
    public static String checkRoad(int num){
//        int start=0x3a; //起始符
//        int number=0x01; //帧编号
//        int code=0x02;//功能码
//        int len_low=0x02;//数据长度低位
//        int len_high=0x00;// 数据长度高位
//        //deal cp
//        int[] cp=Tools.changeToHex(card);//命令标识
//        cp[0] = 0x23;
//
//        int[] arr=new int[]{number,code,len_low,len_high};
//        int[] Arr = new int[arr.length + cp.length];
//        for (int i = 0; i < arr.length; i++) {
//            Arr[i] = arr[i];
//        }
////        for (int i : Arr) {
////            Arr[i] = arr[i];
////        }
//        for (int i = 0; i < cp.length; i++) {
//            Arr[i+arr.length] = cp[i];
//        }
        int[] Arr =  {0xB3,0xF4,0x02,0x00, 0x00+num};

        Crc16 crc16 = new Crc16();
        String crc = (Integer.toString(crc16.ComputeChecksum(Arr), 16).substring(2, 4)
                + Integer.toString(crc16.ComputeChecksum(Arr), 16).substring(0, 2));

        String commands="";
        for (int i=0;i<Arr.length;i++){
            commands+=String.format("%02X",Arr[i]);
        }
        String command = ":" +commands + crc + "\r\n";
        System.out.println("向串口发送数据" + command);
        return command;
    }



}
