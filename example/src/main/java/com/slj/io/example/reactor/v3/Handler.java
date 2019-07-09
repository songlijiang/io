package com.slj.io.example.reactor.v3;

import com.slj.io.example.Constant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * @author songlijiang
 * @version 2019/6/24 13:46
 */
public class Handler implements Runnable {

    private static final int MAX_IN =1024;

    private static final int MAX_OUT =1024;

    private final SocketChannel socket;
    private final SelectionKey sk;
    private final Selector selector;

    private ByteBuffer input = ByteBuffer.allocate(MAX_IN);
    private ByteBuffer output = ByteBuffer.allocate(MAX_OUT);

    private static final int READING = 0, SENDING = 1;
    private int state = READING;


    Handler(Selector sel, SocketChannel c) throws IOException {
        socket = c;
        socket.configureBlocking(false);
        selector=sel;
        sk = socket.register(sel, 0);
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
// Optionally try first read now
        //sk = socket.register(sel, SelectionKey.OP_READ,this);
        sel.wakeup();
    }

    private void process(byte [] inputByte) {
        String outputString = new Date()+ " received "+new String(inputByte, Constant.charset);
        System.out.println(outputString);
        output.clear();
        output.put(outputString.getBytes(Constant.charset));
    }

    /**
     * state design pattern
     */
    @Override
    public synchronized void run() {
        try {
            if (state == READING) {
                read();
            }
            else if (state == SENDING) {
                send();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * handle for  READING
     * @throws IOException
     */
    void read() throws IOException {
        input.clear();
        socket.read(input);
        input.flip();
        byte[] temp =new byte[input.limit()-input.position()];
        input.get(temp,input.position(),input.limit());
        //ignore package split case
        process(temp);
        state = SENDING;
        // Normally also do first write now
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * handle for SENDING
     * @throws IOException
     */
    void send() throws IOException {
        output.flip();
        socket.write(output);
        sk.cancel();
    }


}
