/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package mapboat.roboboat_ysu.net;

import java.io.UnsupportedEncodingException;

import mapboat.roboboat_ysu.net.Utils.LogHelper;

/**
 * Created by Muhammad Resna Rizki on 30/09/2015.
 */
public class DataParser {

    private static final String TAG = DataParser.class.toString();

    private static OnReadableDataAvailableListener mListener = null;

    private static byte[] bufferData = new byte[0];
    private static int bufferPos = 0;
    private static String bufferedString = "";

    private static final char SOL = '^';
    private static final char EOL = '$';

    public static void setOnReadableDataAvailableListener(OnReadableDataAvailableListener listener) {
        mListener = listener;
    }

    public static void appendData(byte[] data) {
        if(validData(data)) {
            bufferData = concatenateByteArrays(bufferData, data);
        }

        while (bufferPos < bufferData.length) {
            if (bufferData[bufferPos] == EOL) {
                try {
                    bufferedString = new String(bufferData, 0, bufferPos, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                LogHelper.simpleLog(TAG, "New Data : " + bufferedString);

                bufferData = new byte[0];
                bufferPos = 0;

                if (mListener != null) {
                    mListener.onNewData(bufferedString);
                }

                break;
            }
            bufferPos++;
        }
    }

    static boolean validData(byte[] data) {
        if (bufferData.length < 1 && data[0] != SOL) return false;

        if(bufferData.length >= data.length) {
            boolean duplicate = true;
            for (int i = data.length - 1, j = bufferData.length - 1; i > -1; i--, j--) {
                if (data[i] != bufferData[j]) {
                    duplicate = false;
                }
            }

            return !duplicate;
        }

        return true;
    }

    public static boolean hasReadableData() {
        return (!bufferedString.isEmpty());
    }

    public static String getReadableData() {
        if (hasReadableData()) {
            return bufferedString;
        }

        return null;
    }

    public static void clearData() {
        bufferData = new byte[0];
        bufferPos = 0;
        bufferedString = "";
    }

    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public interface OnReadableDataAvailableListener {
        void onNewData(String data);
    }

}
