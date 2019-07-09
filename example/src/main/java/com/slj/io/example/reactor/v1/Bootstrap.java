package com.slj.io.example.reactor.v1;

import com.slj.io.example.Constant;

import java.io.IOException;

/**
 * @author songlijiang
 * @version 2019/6/24 13:49
 */
public class Bootstrap {

    public static void main(String[] args) throws IOException {
        new Thread(new Reactor(Constant.serverPort)).start();
    }
}
