package org.pguide;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author DKwms
 * @Date 2023/12/5 21:41
 * @description
 */
public class demo {
    static String  inputString = "16";

    public static void main(String[] args) {
            // 使用默认字符集（通常是UTF-8）编码为字节数组
            byte[] bytesDefaultCharset = inputString.getBytes();
            System.out.println("默认字符集编码：" + Arrays.toString(bytesDefaultCharset));
            for (int i = 0; i < bytesDefaultCharset.length; i++) {
                for (int j = 7; j >= 0; j--) {
                    // 使用位运算符检查第i位是否为1
                    int bit = ( bytesDefaultCharset[i] >> j) & 1;
                    System.out.print(bit);
                }
                System.out.println();
            }
        }
    }
