package com.video.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.video.config.AppConfig;

public class UdpSender {
    public static void sendProgress(int jobId, String status, int percent) {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Format: PROGRESS|jobId|status|percent
            String message = AppConfig.CMD_PROGRESS + AppConfig.MSG_SEPARATOR 
                           + jobId + AppConfig.MSG_SEPARATOR 
                           + status + AppConfig.MSG_SEPARATOR 
                           + percent;

            byte[] data = message.getBytes();
            InetAddress address = InetAddress.getByName(AppConfig.WEB_SERVER_IP);
            
            DatagramPacket packet = new DatagramPacket(data, data.length, address, AppConfig.UDP_PORT);
            socket.send(packet);
            
            System.out.println("[UDP Sent] " + message);
        } catch (Exception e) {
            System.err.println("[UDP Error] " + e.getMessage());
        }
    }
}