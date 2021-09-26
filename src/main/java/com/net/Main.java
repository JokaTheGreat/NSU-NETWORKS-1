package com.net;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getByName("224.1.1.1");

        if (args.length == 1) {
            address = InetAddress.getByName(args[0]);
            if(!address.isMulticastAddress()) {
                System.out.println("Address is not multicast");
                System.exit(-1);
            }
        }
        com.net.App app = new com.net.App(address);

        app.multiSend();
        app.multiReceive();
    }
}