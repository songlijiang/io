package com.slj;

import java.nio.ByteBuffer;

/**
 * @author songlijiang
 * @version 2019-05-07
 */
public class ByteBufferUtils {



    public static byte[] getArray(int byteBufferOffset, int length , ByteBuffer byteBuffer){
        byte[] temp = new byte[length];
        int mark = byteBuffer.position();
        byteBuffer.position(byteBufferOffset);
        byteBuffer.get(temp,0,length);
        byteBuffer.position(mark);
        return temp;
    }
}
