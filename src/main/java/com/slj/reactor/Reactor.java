package com.slj.reactor;

import com.slj.Constant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class Reactor implements Runnable {

    final private Selector selector;
    final private ServerSocketChannel serverSocket;


    Reactor(int port) throws IOException {
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
    void dispatch(SelectionKey k) {
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
        private boolean outPutCompleted = false;
        Handler(Selector sel, SocketChannel c) throws IOException {
            socket = c;
            socket.configureBlocking(false);
// Optionally try first read now
            sk = socket.register(sel, SelectionKey.OP_READ,this);
            sel.wakeup();
        }

        boolean inputIsComplete() {
            String endTag = "bye";
            return endWithString(input, endTag);
        }
        boolean outputIsComplete() {
            return outPutCompleted;
        }

        private boolean endWithString(final ByteBuffer byteBuffer,String endTag){

            if(byteBuffer.position()<endTag.length()){
                return false;
            }
            byte[] end = new byte[endTag.length()];
            for (int i =0; i <endTag.length() ; i++) {
                end[i]=byteBuffer.get(byteBuffer.position()+i-endTag.length());
            }
            return endTag.equals(new String(end, Constant.charset));
        }
        void process() {
            //just copy
            //todo 粘包
            byte[] temp = new byte[input.position()];
            input.clear();
            input.get(temp);
            output.clear();
            output.put(temp);
            outPutCompleted =true;
        }

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
        void read() throws IOException {
            socket.read(input);
            if (inputIsComplete()) {
                process();
                state = SENDING;
// Normally also do first write now
                sk.interestOps(SelectionKey.OP_WRITE);
            }
        }
        void send() throws IOException {
            output.flip();
            socket.write(output);
            if (outputIsComplete()) {
                sk.cancel();
            }
        }

    }


}

/*
Alternatively, use explicit SPI provider:
SelectorProvider p = SelectorProvider.provider();
selector = p.openSelector();
serverSocket = p.openServerSocketChannel();
*/
