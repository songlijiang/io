package com.slj.reactor;

import com.slj.Constant;

import java.io.IOException;

/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class Bootstrap {

    public static void main(String[] args) throws IOException {
        new Thread(new ReactorV2(Constant.serverPort)).start();
    }
}
