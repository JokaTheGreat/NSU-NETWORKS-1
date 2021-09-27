package com.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class App {
    private static final int TIMEOUT = 3000;
    private static final long SECOND_IN_MILLIS = 1000;
    private static final String COPY_NAME_PREFIX = "App@";
    private long lastMapUpdateTime;
    private final MulticastSocket multicastSocket;
    private final DatagramPacket packet;
    private HashMap<AbstractMap.SimpleEntry<String, String>, Long> copiesMap;

    public App(InetAddress multicastAddress) throws IOException {
        multicastSocket = new MulticastSocket(8000);
        multicastSocket.joinGroup(multicastAddress);

        lastMapUpdateTime = System.currentTimeMillis();
        String thisCopyName = COPY_NAME_PREFIX + String.valueOf(Math.random());
        packet = new DatagramPacket(
                thisCopyName.getBytes(),
                thisCopyName.getBytes().length,
                multicastAddress,
                8000
        );

        copiesMap = new HashMap<>();
    }

    public void multiReceive() throws IOException {
        var receivedPacket = new DatagramPacket(new byte[400], 400);
        while(true) {
            multicastSocket.receive(receivedPacket);
            parsePacket(receivedPacket);
        }
    }

    private void parsePacket(DatagramPacket receivedPacket) {
        String copyName = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
        if (!copyName.contains(COPY_NAME_PREFIX)) {
            return;
        }
        var copyParameters = new AbstractMap.SimpleEntry<>(copyName, receivedPacket.getAddress().getHostAddress());

        updateMap(copyParameters);
    }

    private void updateMap(AbstractMap.SimpleEntry<String, String> copyParameters) {
        boolean isNewEntry = (null == copiesMap.put(copyParameters, (long)0));
        if (isNewEntry) {
            printListOfIP();
        }

        if(System.currentTimeMillis() - lastMapUpdateTime > SECOND_IN_MILLIS) {
            lastMapUpdateTime += SECOND_IN_MILLIS;
            copiesMap.replaceAll((key, value) -> value + SECOND_IN_MILLIS);

            if(deleteByTimeout()) {
                printListOfIP();
            }
        }
    }

    private boolean deleteByTimeout() {
        return copiesMap.entrySet().removeIf(entry -> entry.getValue() > TIMEOUT);
    }

    private void printListOfIP() {
        System.out.println(copiesMap.size() + " copies are active:");
        copiesMap.forEach((pairKey, value) -> {
            System.out.println(pairKey.getValue());
        });
        System.out.println("");
    }

    public void multiSend() {
        Runnable support = () -> {
            try {
                while (true) {
                    multicastSocket.send(packet);
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            } catch (InterruptedException ignored) {
            } catch (IOException e) {
                System.err.println("Error sending to socket");
            }
        };

        Thread supportThread = new Thread(support);
        supportThread.start();
    }
}