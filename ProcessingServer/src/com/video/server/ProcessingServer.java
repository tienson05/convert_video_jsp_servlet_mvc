package com.video.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.video.config.AppConfig;

public class ProcessingServer {
    
    // --- CẤU HÌNH TỐI ƯU HÓA SERVER ---
    // Chỉ cho phép chạy tối đa 4 job convert cùng một lúc (Tùy vào số nhân CPU của máy bạn)
    // Nếu có job thứ 5 gửi đến, nó sẽ tự động nằm chờ trong hàng đợi.
	private static final int MAX_CONCURRENT_JOBS = calculateOptimalThreadCount();

    // Hàm tính toán số luồng dựa trên CPU
    private static int calculateOptimalThreadCount() {
        int cores = Runtime.getRuntime().availableProcessors();
        // Chiến thuật an toàn: Luôn chừa lại 1 nhân cho hệ điều hành
        // Nếu máy chỉ có 1 nhân thì chấp nhận chạy 1 job
        int optimal = (cores > 1) ? (cores - 1) : 1;
        
        System.out.println(">> CPU Cores Detected: " + cores);
        System.out.println(">> Optimal Job Pool  : " + optimal);
        return optimal;
    }
    
    // Tạo Thread Pool (Hồ chứa các nhân viên làm việc)
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_JOBS);

    public static void main(String[] args) {
        System.out.println("=== HIGH PERFORMANCE VIDEO PROCESSING SERVER STARTING ===");
        System.out.println("Listen TCP Port: " + AppConfig.TCP_PORT);
        System.out.println("Target UDP Port: " + AppConfig.UDP_PORT);
        System.out.println("Worker Threads : " + MAX_CONCURRENT_JOBS);
        System.out.println("Check FFmpeg   : " + AppConfig.FFMPEG_PATH);
        System.out.println("-------------------------------------------------------");

        try (ServerSocket serverSocket = new ServerSocket(AppConfig.TCP_PORT)) {
            while (true) {
                System.out.println("Waiting for request...");
                
                // 1. Chấp nhận kết nối từ Web Server
                Socket clientSocket = serverSocket.accept();
                System.out.println(">> New connection received from: " + clientSocket.getInetAddress());

                // 2. Tạo đối tượng xử lý (Worker)
                ClientHandler handler = new ClientHandler(clientSocket);

                // 3. TỐI ƯU: Đưa vào Thread Pool để quản lý
                // Thay vì: new Thread(handler).start(); (Cách cũ dễ gây sập server)
                threadPool.execute(handler);
                
                System.out.println(">> Job added to Execution Queue.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Đóng Pool khi tắt server (Quy tắc dọn dẹp)
            threadPool.shutdown();
        }
    }
}