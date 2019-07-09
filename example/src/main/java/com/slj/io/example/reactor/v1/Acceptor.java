package com.slj.io.example.reactor.v1;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author songlijiang
 * @version 2019/6/24 13:43
 */
public class Acceptor implements Runnable{


    private final ServerSocketChannel serverSocket;

    private final Selector selector;

    public Acceptor(ServerSocketChannel serverSocket, Selector selector) {
        this.serverSocket = serverSocket;
        this.selector =selector;
    }


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
