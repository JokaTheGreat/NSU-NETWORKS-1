package com.net;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        App app;

        if (args.length == 1) {
            app = new App(args[0]);
        } else {
            app = new App("224.1.1.1");
        }

        Runnable support = app::multiSend;
        Thread supportThread = new Thread(support);
        supportThread.start();

        app.multiReceive();
    }
}
