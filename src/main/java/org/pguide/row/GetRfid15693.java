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
	//1 �򿪴��ں���
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
					System.out.println("�Ѵ򿪣�"+commPort);
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
	
  //���˿ڷ������ݺ���
	public void SendPortData(SerialPort serialPort,byte[] data){
		OutputStream outputStream=null;
		try {
			outputStream=serialPort.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
			//�˾��Ŀ�����ڵ��Ե�ʱ��۲������Ƿ��͵�����
			System.out.println("�򴮿ڷ������ݷ��ͳɹ�");
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
	//�Ӷ˿ڶ����ݺ���
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
	//������
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetRfid15693 getRfidData=new GetRfid15693();
		SerialPort serialPort=getRfidData.OpenSerial("COM4", 115200);
		//����֡�ַ���   : count 01 02 00 27 crc 0d 0a
		//3A 01 01 02 00 27 ������У����ֻ��Ҫ ֡���   ������  ���ݳ��� �����ʶ ����У��
		
		int[] arr=new int[]{ 1, 0x01, 0x02, 0x00, 0x27 };
		Crc16 crc16=new Crc16();
		/**
		 * �ӿ�Э����Ҫ��crc��У���룩���ֽ���ǰ���ֽ��ں������Ҫ����õ�У����ߵ��ֽڻ�λ
		 * substring(start,stop)ȡ�ַ���start��stop-1λ
		 */
		String crc = (Integer.toString(crc16.ComputeChecksum(arr), 16).substring(2, 4)
				+ Integer.toString(crc16.ComputeChecksum(arr), 16).substring(0, 2));
		/**
		 * ���ݽӿ�Э��Ѱ��ָ�����ݸ�ʽΪ����ʼ��   ֡���   ������  ���ݳ��� �����ʶ  У����  ������
		 *  ��ʼ��Ϊ       ������һ���ֽ�
		 *  ֡���           0��ʼ����һ���ֽ�ѭ��ʹ��
		 *  ������           01 ��ʾѰ�� ����һ���ֽ�
		 *  ���ݳ���      ���ݳ��� 2���ֽ�02 00�����ֽ���ǰ��
		 *  �����ʶ       0x06��ASK��ʽ��ʹ�÷���ͻ�㷨��ȡ�����ǩ��        0x07��FSK��ʽ��ʹ�÷���ͻ�㷨��ȡ�����ǩ��
         *         0x26��ASK��ʽ����ʹ�÷���ͻ�㷨����ȡ������ǩ��0x27��FSK��ʽ����ʹ�÷���ͻ�㷨����ȡ������ǩ��      
         *  У����          ͨ��CRC16�����У����֮�󣬼�������2�ֽڣ����뱨��֡ʱ�����ֽ���ǰ�����ֽ��ں�
         *  ������          CR-LF��Hex 0x0D��0x0A�� ����2���ֽڡ�
		 */
		//ע������ģ�0 ������ʼ����0ʵ����֡��ŵĸ��ֽڣ���Ϊ֡�����ѭ��ʹ�ã�����Ϊ�˽���ļ���Խ�֡��Ų�ѭ����ֱ����Ϊ0x01
		String command = ":0" + 1 + "01020027" + crc + "\r\n";
		System.out.println(command);
		//getbytes()���ַ���ת���ֽ�
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
        System.out.println("reader����pcԭʼ����"+dataOriginal);
        /**
         * ���ڳ�����ԭʼ�ַ���: 01 00 0A 00 00 00 83 5A 68 81 50 01 04 E0 DED2
         * ����Э���д�����ظ�PC�������ݸ�ʽΪ��
         * ��ʼ��   ֡����  ״̬��  ���ݳ���  ���Ű�����  У����  ������
         * ��ʼ��  �� һ���ֽ�
         * ֡����  ��Ѱ��ʱ�·� ֡����һ��������·���01 ����ʱҲ��01 ,����һ���ֽ�
         * ״̬��   ������ȷ����0x00������ֵ��ʾ����ʧ�ܣ�����һ���ֽ�
         * ���ݳ���  2�ֽڣ����ֽ���ǰ��
         * ���Ű�����  10���ֽڣ���1���ֽ�Ϊ��ǩ״̬Ϊ0x00,��2���ֽڱ�ǩ���ݴ洢��ʶ��
         *        ����8���ֽ�Ϊ��ǩUID�����ֽ��ں�
         * У����  
         * ������  ����2���ֽڣ�0X0d 0X0a
         * 
         * ��� �����Ŀ����Ƿ����ַ����д�13λ��28λ��������Ҫע�������ʼ��0X3Aת���ַ����ǣ�ֻ
         * ռһ��λ�á������Ǵ�13λ��ʼ
         */
        String string=dataOriginal.substring(13,29);
        String dataend="";
        //�����������ǵ��ֽ���ǰ�������Ҫ�����ص�UID�ߵ��ֽ�ת����
        for (int i=0;i<string.length();i=i+2){
        	dataend=string.substring(i, i+2)+dataend;
        }
        System.out.println("����:"+dataend);
        //:01000A000000835A6881500104E0DED2
	}

}
