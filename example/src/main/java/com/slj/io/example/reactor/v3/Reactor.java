package com.slj.io.example.reactor.v3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author songlijiang
 * @version 2019/6/24 13:41
 */
public class Reactor implements Runnable{

    final private Selector selector;
    final private ServerSocketChannel serverSocket;

    private ExecutorService executor = new ThreadPoolExecutor(10,10,60, TimeUnit.SECONDS
            , new LinkedBlockingDeque<>(Integer.MAX_VALUE),new ThreadPoolExecutor.AbortPolicy());



    Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(
                new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT,new Acceptor(serverSocket,selector));
    }

    @Override
    public void run() { // normally in a new
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    dispatch(key);
                    keyIterator.remove();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    private void dispatch(SelectionKey k) {
        Runnable r = (Runnable)(k.attachment());
        if (r != null) {
            //r.run();

            if(k.attachment() instanceof Handler){
                executor.execute(r);
            }else {
                r.run();
            }

        }
    }


}
