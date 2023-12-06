package org.pguide.row;

public class Crc16 {
    
    private final int polynomial = 0x8408;
     
    private int[] table = new int[256];
     
    public int ComputeChecksum(int[] bytes) {
        int crc = 0xffff;
        for (int i = 0; i < bytes.length; ++i) {
            int index = (crc ^ bytes[i]) % 256;
            crc = (crc >> 8) ^ table[index];
        }
        return crc;
    }
     
    public Crc16() {
        int value;
        int temp;
        for (int i = 0; i < table.length; ++i) {
            value = 0;
            temp = i;
            for (byte j = 0; j < 8; ++j) {
                if (((value ^ temp) & 0x0001) != 0) {
                    value = (value >> 1) ^ polynomial;
                } else {
                    value >>= 1;
                }
                temp >>= 1;
            }
            table[i] = value;
        }
    }
    
    public int covert(int javaReadInt){
    	// ?????????????  
    	byte byte4 = (byte) (javaReadInt & 0xff);  
    	byte byte3 = (byte) ((javaReadInt & 0xff00) >> 8);  
    	byte byte2 = (byte) ((javaReadInt & 0xff0000) >> 16);  
    	byte byte1 = (byte) ((javaReadInt & 0xff000000) >> 24);  
    	  
    	// ???? "???????????????"????
    	int realint = (byte1& 0xff)<<0  |(byte2& 0xff)<<8 | (byte3& 0xff)<< 16| (byte4& 0xff)<<24 ; 
    	System.out.printf("%x\n", realint); 
    	return realint;
    		
    }
     
    public static void main(String[] args) {
//        Crc16 c = new Crc16();
//        int[] arr = new int[]{0x02,0x01,0x02,0x00,0x01};
//        System.out.println(Integer.toString(c.ComputeChecksum(arr), 16).substring(2, 4)+Integer.toString(c.ComputeChecksum(arr), 16).substring(0, 2));
//        arr = new int[]{0xB, 0x0, 0x1, 0x1, 0x1, 0x4, 0xEE, 0x35, 0x45, 0x45 };
//        int a = Integer.parseInt("d1", 16);
//        System.out.println(a);
//        System.out.println(Integer.toString(c.ComputeChecksum(arr), 16));
//        byte[] beginCommand = new byte[] { 0x3a, 0x03, 0x01, 0x02, 0x00, 0x27,(byte) 0xd1,(byte) 0xfa,0x0d,0x0a};
//		 //System.out.println(Utils.get_AllHexString(beginCommand));

        Crc16 crc16 = new Crc16();
        int[] arr = new int[]{0x73,0xF4,0x02,0x00,0x02};
        int i = crc16.ComputeChecksum(arr);
        System.out.println(Integer.toString(i, 16));
    }
}