package com.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class App {
    private final int timeout = 3;
    private final MulticastSocket multicastSocket;
    private final String thisCopyName;
    private final DatagramPacket packet;
    private HashMap<AbstractMap.SimpleEntry<String, String>, Integer> copiesMap;

    App(String multicastAddress) throws IOException {
        multicastSocket = new MulticastSocket(8000);
        multicastSocket.joinGroup(InetAddress.getByName(multicastAddress));

        thisCopyName = String.valueOf(Math.random());
        packet = new DatagramPacket(
                thisCopyName.getBytes(),
                thisCopyName.getBytes().length,
                InetAddress.getByName(multicastAddress),
                8000
        );

        copiesMap = new HashMap<>();
    }

    void multiReceive() throws IOException {
        var receivedPacket = new DatagramPacket(new byte[400], 400);
        while(true) {
            multicastSocket.receive(receivedPacket);
            packetParser(receivedPacket);
        }
    }

    void packetParser(DatagramPacket receivedPacket) {
        String copyName = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
        var copyParametres = new AbstractMap.SimpleEntry<>(copyName, receivedPacket.getAddress().getHostAddress());

        if(!copiesMap.containsKey(copyParametres)) {
            copiesMap.put(copyParametres, 0);

            System.out.println(copiesMap.size() + " copies are active:");
            copiesMap.forEach((pairKey, value) -> {
                System.out.println(pairKey.getValue());
            });
            System.out.println("");
        }
        else {
            copiesMap.replace(copyParametres, 0);
        }

        if(thisCopyName.equals(copyParametres.getKey())) {
            copiesMap.replaceAll((key, value) -> value + 1);

            if(copiesMap.values().remove(timeout)) {
                System.out.println(copiesMap.size() + " copies are active:");
                copiesMap.forEach((pairKey, value) -> {
                    System.out.println(pairKey.getValue());
                });
                System.out.println("");
            }
        }
    }

    void multiSend() {
        try {
            while(true) {
                multicastSocket.send(packet);
                TimeUnit.SECONDS.sleep(1);
            }
        }
        catch (InterruptedException ignored) { }
        catch (IOException e) {
            System.err.println("Error sending to socket");
            System.exit(-1);
        }
    }
}
