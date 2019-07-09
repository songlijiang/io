package com.slj.io.example.oio;

import com.slj.io.example.Constant;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author songlijiang
 * @version 2019-05-06
 */

 public  class Server implements Runnable {



    private static final int MAX_INPUT =1024;



    private ExecutorService executor = new ThreadPoolExecutor(10,10,60, TimeUnit.SECONDS
            , new LinkedBlockingDeque<>(Integer.MAX_VALUE),new DefaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(Constant.serverPort);
            while (!Thread.interrupted()){
                //new Thread(new Handler(ss.accept())).start();
                executor.execute(new Handler(ss.accept()));
            }
            // or, single-threaded, or a thread pool
        } catch (IOException ex) {
            System.out.println("error"+ex);
        }
    }
    static class Handler implements Runnable {

        final Socket socket;

        Handler(Socket s) {
            socket = s;
        }
        @Override
        public void run() {
            try {
                byte[] input = new byte[MAX_INPUT];
                int size = socket.getInputStream().read(input);
                byte[] output = process(Arrays.copyOfRange(input,0,size));
                socket.getOutputStream().write(output);
            } catch (IOException ex) {

            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private byte[] process(byte[] cmd) {
            String input = new String(cmd, Constant.charset);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(input);
            return ("received"+input).getBytes(Constant.charset);
        }
    }



    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
 }


   // Note: most exception handling elided from code examples

