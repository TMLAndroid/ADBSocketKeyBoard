package com.android.adbkeyboard;

/**
 * Created by tangminglong on 18/3/28.
 */

public class TempTest {

    public TempTest() {

        String a = decode("\\u0020\\u4e2d\\u56fd\\u4eba\\u4eec");
        System.out.println(a);

    }

    public static void main(String[] args) {
        new TempTest();
    }

    public static String decode(String unicode) {
        StringBuffer str = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            str.append((char) data);
        }
        return str.length() > 0 ? str.toString() : unicode;
    }

}
