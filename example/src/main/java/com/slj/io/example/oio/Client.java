package com.slj.io.example.oio;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

import static com.slj.io.example.Constant.serverIp;
import static com.slj.io.example.Constant.serverPort;


/**
 * @author songlijiang
 * @version 2019-05-06
 */
public class Client {



    private static Charset charset = Charset.forName("UTF-8");

    public static void connect(String request){
        byte[] bytes = new byte[1024];
        try ( Socket socket = new Socket(serverIp,serverPort)){
            socket.getOutputStream().write(request.getBytes(charset));
            int size = socket.getInputStream().read(bytes);
            System.out.println(new String(Arrays.copyOfRange(bytes,0,size),charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Client.connect("hello word"+i);
        }
        System.out.println((System.currentTimeMillis()-start)/1000);
    }

}
