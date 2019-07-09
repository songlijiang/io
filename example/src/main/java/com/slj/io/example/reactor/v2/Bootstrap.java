package com.slj.io.example.reactor.v2;

import com.slj.io.example.Constant;

import java.io.IOException;

/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class Bootstrap {

    public static void main(String[] args) throws IOException {
        new Thread(new Reactor(Constant.serverPort)).start();
    }
}
