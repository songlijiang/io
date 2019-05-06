package com.slj.io;


import org.junit.Before;
import org.junit.Test;

/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class ServerTest {

    @Before
    public void before(){
    }

    @Test
    public void run() {
        new Server().run();
        Client.connect();
    }
}