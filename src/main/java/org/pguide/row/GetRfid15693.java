package org.pguide.row;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
public class GetRfid15693 {	
	//1 打开串口函数
	public SerialPort OpenSerial(String serialname,int baudrate){
		SerialPort serialPort=null;
		CommPort commPort=null;
		try {
			CommPortIdentifier commPortIdentifier=CommPortIdentifier.getPortIdentifier(serialname);
		    try {
				commPort=commPortIdentifier.open(serialname, 2000);
				serialPort=(SerialPort)commPort;
				try {
					serialPort.setSerialPortParams
					(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					System.out.println("已打开："+commPort);
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
	public void SendPortData(SerialPort serialPort,byte[] data){
		OutputStream outputStream=null;
		try {
			outputStream=serialPort.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
			//此句的目的是在调试的时候观察数据是否发送到串口
			System.out.println("向串口发送数据发送成功");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {if(outputStream !=null)
				outputStream.close();
			    outputStream=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//从端口读数据函数
	public byte[] ReadComData(SerialPort serialPort){
		byte[] bytes=null;
		InputStream inputStream=null;
		try {
			inputStream=serialPort.getInputStream();
			int buff=inputStream.available();
			while(buff!=0){
				bytes=new byte[buff];
				inputStream.read(bytes);
				buff=inputStream.available();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}
	//主函数
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetRfid15693 getRfidData=new GetRfid15693();
		SerialPort serialPort=getRfidData.OpenSerial("COM4", 115200);
		//命令帧字符串   : count 01 02 00 27 crc 0d 0a
		//3A 01 01 02 00 27 ，另外校验码只需要 帧编号   功能码  数据长度 命令标识 参与校验
		
		int[] arr=new int[]{ 1, 0x01, 0x02, 0x00, 0x27 };
		Crc16 crc16=new Crc16();
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
		String command = ":0" + 1 + "01020027" + crc + "\r\n";
		System.out.println(command);
		//getbytes()将字符串转成字节
		getRfidData.SendPortData(serialPort, command.getBytes());
        byte[] data=null;
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        data=getRfidData.ReadComData(serialPort);
        String dataOriginal = new String(data);
        System.out.println("reader返回pc原始数据"+dataOriginal);
        /**
         * 串口出来的原始字符串: 01 00 0A 00 00 00 83 5A 68 81 50 01 04 E0 DED2
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
        String string=dataOriginal.substring(13,29);
        String dataend="";
        //返回数据是是低字节在前，因此需要将返回的UID高低字节转换。
        for (int i=0;i<string.length();i=i+2){
        	dataend=string.substring(i, i+2)+dataend;
        }
        System.out.println("卡号:"+dataend);
        //:01000A000000835A6881500104E0DED2
	}

}
