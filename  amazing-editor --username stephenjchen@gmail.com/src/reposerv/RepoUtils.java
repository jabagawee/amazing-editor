/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reposerv;

import java.io.*;

/**
 *
 * @author Stephen
 */
public class RepoUtils {

    public static void putInt(int value, byte[] array, int offset) {
        array[offset] = (byte) (0xff & (value >> 24));
        array[offset + 1] = (byte) (0xff & (value >> 16));
        array[offset + 2] = (byte) (0xff & (value >> 8));
        array[offset + 3] = (byte) (0xff & value);
    }

    public static int getInt(byte[] array, int offset) {
        return ((array[offset] & 0xff) << 24)
                | ((array[offset + 1] & 0xff) << 16)
                | ((array[offset + 2] & 0xff) << 8)
                | (array[offset + 3] & 0xff);
    }

    public static byte[] concat(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);

        return C;
    }
    
    public static byte[] headerizeMessage(char flag, byte[] bytes) {
        byte[] headerBytes = new byte[5];
        headerBytes[0] = (byte)flag; //ERROR flag
        putInt(bytes.length, headerBytes, 1);
        byte[] theBytes = concat(headerBytes, bytes);
        return theBytes;
    }
       
    public static byte[] getErrorMessage(String msg) {
        return headerizeMessage('e', msg.getBytes());
    }
    
    public static byte[] getSuccessMessage(String msg) {
        return headerizeMessage('s', msg.getBytes());
    }
}
