package org.pguide;

import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;

public class Tools {
    public static void main(String[] args) {

        int[] ints = changeToHex("5BD1834D500104E0");
        System.out.println(Arrays.toString(ints));
    }

    public static int[] changeToHex(String card){
        String[] split = card.split("");

        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < split.length-1; i+=2) {
            list.add(split[i] + split[i+1]);
        }

        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = Integer.parseInt(list.get(i),16);
        }



        return res;
    }
}
