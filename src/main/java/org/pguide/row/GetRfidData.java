package org.pguide.row;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class GetRfidData {

    //1 打开串口函数
    public SerialPort OpenSerial(String serialname, int baudrate) {
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
    public void SendPortData(SerialPort serialPort, byte[] data) {
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

    //从端口读数据函数
    public byte[] ReadComData(SerialPort serialPort) {
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

    //主函数
    public static void main(String[] args) {
//        int code = 0x01;
////        int cp = 0x23E00401504D83D157;
//        test(code,0x07);

        baseTest();
    }
        /**
         * 串口出来的原始字符串:01000A000000835A6881500104E0DED2
         * 根据协议读写器返回给PC机的数据格式为：
         * 起始符   帧编码  状态码  数据长度  卡号包数据  校验码  结束符
         * 起始符  ： 一个字节
         * 帧编码  与寻卡时下发 帧编码一致如此例下发是01 返回时也是01 ,长度一个字节
         * 状态码   读卡正确返回0x00，其他值表示读卡失败，长度一个字节
         * 数据长度  2字节，低字节在前。
         * 卡号包数据  10个字节，第1个字节为标签状态为0x00,第2个字节标签数据存储标识，
         *        后面8个字节为标签UID。高字节在后。
         * 校验码
         * 结束符  长度2个字节，0X0d 0X0a
         *
         * 因此 真正的卡号是返回字符串中从13位到28位，这里需要注意的是起始符0X3A转成字符后是：只
         * 占一个位置。所以是从13位开始
         */
        public static String getCommondSelf(int code,int cp){
            //命令帧字符串   : count 01 02 00 27 crc 0d 0a
//        while(true) {
//           int[] arr = new int[]{1, 0x01, 0x02, 0x00, 0x27};
            int start=0x3a; //起始符
            int number=0x01; //帧编号
//            int code=0x02;//功能码
            int len_low=0x02;//数据长度低位
            int len_high=0x00;// 数据长度高位
//            int cp=0x07;//命令标识
            int[] arr=new int[]{number,code,len_low,len_high,cp};
            Crc16 crc16 = new Crc16();
            /**
             * 接口协议中要求crc（校验码）低字节在前高字节在后，因此需要将获得的校验码高低字节换位
             * substring(start,stop)取字符从start到stop-1位
             */
            String crc = (Integer.toString(crc16.ComputeChecksum(arr), 16).substring(2, 4)
                    + Integer.toString(crc16.ComputeChecksum(arr), 16).substring(0, 2));
            /**
             * 根据接口协议寻卡指令数据格式为：起始符   帧编号   功能码  数据长度 命令标识  校验码  结束符
             *  起始符为       ：长度一个字节
             *  帧编号           0开始长度一个字节循环使用
             *  功能码           01 表示寻卡 长度一个字节
             *  数据长度      数据长度 2个字节02 00（低字节在前）
             *  命令标识       0x06：ASK方式，使用防冲突算法读取多个标签。        0x07：FSK方式，使用防冲突算法读取多个标签。
             *         0x26：ASK方式，不使用防冲突算法，读取单个标签。0x27：FSK方式，不使用防冲突算法，读取单个标签。
             *  校验码          通过CRC16计算出校验码之后，计算结果是2字节，加入报文帧时，低字节在前，高字节在后。
             *  结束符          CR-LF（Hex 0x0D和0x0A） 长度2个字节。
             */


            //注意这里的：0 ：是起始符，0实际是帧编号的高字节，因为帧编号是循环使用，这里为了讲解的简便性将帧编号不循环，直接设为0x01
            String commands="";
            for (int i=0;i<arr.length;i++){
                commands+=String.format("%02X",arr[i]);
            }
            String command = ":" +commands + crc + "\r\n";
            System.out.println("向串口发送数据" + command);
            return command;
        }

        public static void test(int code,int cp) {
            // TODO Auto-generated method stub
            GetRfidData getRfidData = new GetRfidData();
            SerialPort serialPort = getRfidData.OpenSerial("COM4", 115200);
            // 获取命令
            String command = getCommondSelf(code,cp);
            //getbytes()将字符串转成字节
            getRfidData.SendPortData(serialPort, command.getBytes());
            byte[] data = null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            data = getRfidData.ReadComData(serialPort);


            String dataOriginal = new String(data);
            System.out.println("reader返回pc原始数据" + dataOriginal);
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
            try {
                String string = dataOriginal;
                String dataend = "";
                //返回数据是是低字节在前，因此需要将返回的UID高低字节转换。
//               for (int i = 0; i < string.length(); i = i + 2) {
//                   dataend = string.substring(i, i + 2) + dataend;
//               }
//                System.out.println("卡号:" + dataend);
                //:01000A000000835A6881500104E0DED2
            } catch (Exception e) {
                // do sth when error
//               System.out.println("读取失败");
//               System.out.println(dataOriginal.toString());
                e.printStackTrace();
            }
        }


        public static  void baseTest(){
            // TODO Auto-generated method stub
            GetRfidData getRfidData = new GetRfidData();
            SerialPort serialPort = getRfidData.OpenSerial("COM4", 115200);
//命令帧字符串   : count 01 02 00 27 crc 0d 0a
//        while(true) {
//           int[] arr = new int[]{1, 0x01, 0x02, 0x00, 0x27};
            int start=0x3a; //起始符
            int number=0x01; //帧编号b
            int code=0x02;//功能码
            int len_low=0x12;//数据长度低位
            int len_high=0x00;// 数据长度高位
            int cp=0x07;//命令标识
//            int[] arr=new int[]{number,code,len_low,len_high,cp};
            int[] arr=new int[]{number,code,len_low,len_high,0x23,0x5B,0xD1,0x83,0x4D,0x50,0x01,0x04,0xE0};
            Crc16 crc16 = new Crc16();
            /**
             * 接口协议中要求crc（校验码）低字节在前高字节在后，因此需要将获得的校验码高低字节换位
             * substring(start,stop)取字符从start到stop-1位
             */
            String crc = (Integer.toString(crc16.ComputeChecksum(arr), 16).substring(2, 4)
                    + Integer.toString(crc16.ComputeChecksum(arr), 16).substring(0, 2));
            /**
             * 根据接口协议寻卡指令数据格式为：起始符   帧编号   功能码  数据长度 命令标识  校验码  结束符
             *  起始符为       ：长度一个字节
             *  帧编号           0开始长度一个字节循环使用
             *  功能码           01 表示寻卡 长度一个字节
             *  数据长度      数据长度 2个字节02 00（低字节在前）
             *  命令标识       0x06：ASK方式，使用防冲突算法读取多个标签。        0x07：FSK方式，使用防冲突算法读取多个标签。
             *         0x26：ASK方式，不使用防冲突算法，读取单个标签。0x27：FSK方式，不使用防冲突算法，读取单个标签。
             *  校验码          通过CRC16计算出校验码之后，计算结果是2字节，加入报文帧时，低字节在前，高字节在后。
             *  结束符          CR-LF（Hex 0x0D和0x0A） 长度2个字节。
             */


            //注意这里的：0 ：是起始符，0实际是帧编号的高字节，因为帧编号是循环使用，这里为了讲解的简便性将帧编号不循环，直接设为0x01
            String commands="";
            for (int i=0;i<arr.length;i++){
                commands+=String.format("%02X",arr[i]);
            }
            String command = ":" +commands + crc + "\r\n";
            System.out.println("向串口发送数据" + command);
            //getbytes()将字符串转成字节
            getRfidData.SendPortData(serialPort, command.getBytes());
            byte[] data = null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            data = getRfidData.ReadComData(serialPort);

            String dataOriginal = new String(data);
            System.out.println("reader返回pc原始数据" + dataOriginal);
            try {
                String string = dataOriginal;
                String dataend = "";
                //返回数据是是低字节在前，因此需要将返回的UID高低字节转换。
//               for (int i = 0; i < string.length(); i = i + 2) {
//                   dataend = string.substring(i, i + 2) + dataend;
//               }
                System.out.println("卡号:" + dataend);
                //:01000A000000835A6881500104E0DED2
            } catch (Exception e) {
                // do sth when error
//               System.out.println("读取失败");
//               System.out.println(dataOriginal.toString());
                e.printStackTrace();
            }
        }

}


