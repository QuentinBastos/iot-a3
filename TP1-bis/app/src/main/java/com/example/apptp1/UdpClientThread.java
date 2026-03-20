package com.example.apptp1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UdpClientThread extends Thread {

    public interface Listener {
        void onMessage(String message);
        void onError(Exception exception);
    }

    private final String host;
    private final int port;
    private final Listener listener;
    private DatagramSocket socket;

    public UdpClientThread(String host, int port, Listener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);

            byte[] buffer = new byte[2048];
            while (!isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                } catch (IOException timeout) {
                    // Timeout is expected to allow interruption checks.
                }
            }
        } catch (SocketException exception) {
            if (listener != null) {
                listener.onError(exception);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public void send(String message) throws IOException {
        if (socket == null || socket.isClosed()) {
            return;
        }
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }

    public void shutdown() {
        interrupt();
        if (socket != null) {
            socket.close();
        }
    }
}

