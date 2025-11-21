package com.video.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class WebServerSimulator {

    // Cấu hình giả lập
    private static final int SERVER_TCP_PORT = 9000; // Port Server của bạn
    private static final int WEB_UDP_PORT = 9001;    // Port Web lắng nghe (giả lập)
    private static final String FILE_PATH = "D:\\CODING\\HK1-25-26\\LTM\\Videotest\\test.mp4"; // File thật

    public static void main(String[] args) {
        System.out.println("=== WEB SERVER SIMULATOR STARTING ===");

        // 1. KHỞI ĐỘNG UDP LISTENER (Chạy ngầm để hứng tin nhắn tiến độ)
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(WEB_UDP_PORT)) {
                System.out.println("[WEB] Listening for UDP Progress on port " + WEB_UDP_PORT + "...");
                byte[] buffer = new byte[1024];
                
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("   [UDP RECV] " + msg); // In ra xem Server báo gì
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 2. GỬI CÁC REQUEST TEST (TCP)
        // Đợi 1 chút cho UDP sẵn sàng
        try { Thread.sleep(1000); } catch (Exception e) {}

        // --- KỊCH BẢN TEST 1: Gửi 1 Job Bình thường ---
        sendTestRequest(101, "avi", "NORMAL");

        // --- KỊCH BẢN TEST 2: Gửi 1 Job Siêu tốc (Sau 2 giây) ---
        try { Thread.sleep(2000); } catch (Exception e) {}
        sendTestRequest(102, "mkv", "FAST");

        // --- KỊCH BẢN TEST 3: STRESS TEST (Gửi 5 job cùng lúc xem Thread Pool có chạy không) ---
        /*
        System.out.println(">> STARTING STRESS TEST...");
        for (int i = 1; i <= 5; i++) {
            sendTestRequest(200 + i, "mp4", "NORMAL");
        }
        */
    }

    // Hàm gửi request TCP
    private static void sendTestRequest(int jobId, String format, String mode) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", SERVER_TCP_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Format: CONVERT|jobId|inputPath|outputFormat|MODE
                String command = "CONVERT|" + jobId + "|" + FILE_PATH + "|" + format + "|" + mode;
                
                System.out.println("[WEB SENT] Job " + jobId + ": " + command);
                out.println(command);

                // Đọc phản hồi TCP
                String response = in.readLine();
                System.out.println("[WEB TCP RESPONSE] Job " + jobId + ": " + response);

            } catch (Exception e) {
                System.err.println("Lỗi kết nối TCP: " + e.getMessage());
            }
        }).start();
    }
}