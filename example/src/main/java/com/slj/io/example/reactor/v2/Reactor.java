package com.slj.io.example.reactor.v2;


import com.slj.io.example.Constant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;

/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class Reactor implements Runnable {

    final private Selector selector;
    final private ServerSocketChannel serverSocket;


    public Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(
                new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT,new Acceptor());

    }

    @Override
    public void run() { // normally in a new
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey selectionKey : selected) {
                    dispatch(selectionKey);
                }
                selected.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    private void dispatch(SelectionKey k) {
        Runnable r = (Runnable)(k.attachment());
        if (r != null) {
            r.run();
        }
    }



    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    new Handler(selector, c);
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static final int MAXIN =1024;

    private static final int MAXOUT =1024;

    final class Handler implements Runnable {

        final SocketChannel socket;
        final SelectionKey sk;
        ByteBuffer input = ByteBuffer.allocate(MAXIN);
        ByteBuffer output = ByteBuffer.allocate(MAXOUT);
        static final int READING = 0, SENDING = 1;
        int state = READING;


        Handler(Selector sel, SocketChannel c) throws IOException {
            socket = c;
            socket.configureBlocking(false);
// Optionally try first read now
            sk = socket.register(sel, SelectionKey.OP_READ,this);
            sel.wakeup();
        }

        private void process(byte [] inputByte) {
            String outputString = new Date()+ "received"+new String(inputByte,Constant.charset);
            System.out.println(outputString);
            output.clear();
            output.put(outputString.getBytes(Constant.charset));
        }

        /**
         * state design pattern
         */
        @Override
        public void run() {
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


}