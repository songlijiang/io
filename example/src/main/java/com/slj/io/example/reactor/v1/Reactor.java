package com.slj.io.example.reactor.v1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

/**
 * @author songlijiang
 * @version 2019/6/24 13:41
 */
public class Reactor implements Runnable{

    final private Selector selector;
    final private ServerSocketChannel serverSocket;


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


}
