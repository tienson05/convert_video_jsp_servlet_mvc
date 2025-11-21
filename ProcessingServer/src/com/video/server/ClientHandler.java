package com.video.server;

import java.io.*;
import java.net.Socket;
import com.video.config.AppConfig;
import com.video.network.UdpSender;
import com.video.service.FFmpegService;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            // Đọc lệnh từ Web Server
            String request = in.readLine();
            System.out.println("[TCP Received] " + request);

            if (request != null && request.startsWith(AppConfig.CMD_CONVERT)) {
                // Protocol: CONVERT|jobId|inputPath|outputFormat|MODE
                String[] parts = request.split(AppConfig.PROTOCOL_SEPARATOR);
                
                if (parts.length >= 4) {
                    int jobId = Integer.parseInt(parts[1]);
                    String inputPath = parts[2];
                    String format = parts[3]; 
                    
                    // Kiểm tra mode (NORMAL hoặc FAST)
                    String mode = (parts.length >= 5) ? parts[4] : "NORMAL";
                    
                    // --- 1. CHUẨN BỊ THƯ MỤC OUTPUT (QUAN TRỌNG) ---
                    // Lấy đường dẫn từ AppConfig để tách biệt Input/Output
                    File outputDir = new File(AppConfig.OUTPUT_DIR);
                    if (!outputDir.exists()) {
                        outputDir.mkdirs(); // Tự tạo thư mục nếu chưa có
                    }

                    // --- 2. KIỂM TRA DUNG LƯỢNG Ổ ĐĨA ĐÍCH ---
                    // Kiểm tra ổ đĩa chứa thư mục Output có còn đủ chỗ không (>500MB)
                    long freeSpace = outputDir.getFreeSpace(); 
                    long safeMargin = 500 * 1024 * 1024L; 

                    if (freeSpace < safeMargin) {
                        System.err.println(">> [CRITICAL] Output Disk Full! Rejecting Job " + jobId);
                        UdpSender.sendProgress(jobId, "FAILED_DISK_FULL", 0);
                        out.println("ERROR|Server Disk Full");
                        return; // Dừng ngay lập tức
                    }

                    // --- 3. TẠO ĐƯỜNG DẪN FILE KẾT QUẢ ---
                    File inputFile = new File(inputPath);
                    String originalName = inputFile.getName(); // Lấy tên file gốc: "test.mp4"
                    
                    // Bỏ đuôi file cũ: "test"
                    String baseName = originalName.contains(".") 
                                    ? originalName.substring(0, originalName.lastIndexOf(".")) 
                                    : originalName;

                    // Tạo hậu tố tên file
                    String fileNameSuffix = "FAST".equalsIgnoreCase(mode) ? "_fast" : "_optimized";
                    
                    // Ghép thành đường dẫn hoàn chỉnh: D:\OutputVideo\test_fast.mkv
                    String outputPath = outputDir.getAbsolutePath() + File.separator + baseName + fileNameSuffix + "." + format;

                    // ---------------------------------------------------------

                    FFmpegService ffmpeg = new FFmpegService();
                    boolean success;

                    // --- LOGIC XỬ LÝ ---
                    if ("FAST".equalsIgnoreCase(mode)) {
                        System.out.println(">> Job " + jobId + ": Detect FAST MODE -> Saving to: " + outputPath);
                        UdpSender.sendProgress(jobId, "PROCESSING_FAST", 0);
                        success = ffmpeg.convertFastCopy(inputPath, outputPath);
                        
                    } else {
                        System.out.println(">> Job " + jobId + ": Detect NORMAL MODE -> Saving to: " + outputPath);
                        UdpSender.sendProgress(jobId, "PROCESSING", 0);
                        
                        // Giả lập delay để Web hiển thị
                        Thread.sleep(500); 
                        UdpSender.sendProgress(jobId, "PROCESSING", 10);
                        
                        success = ffmpeg.convertVideo(inputPath, outputPath);
                    }

                    // --- TRẢ KẾT QUẢ ---
                    if (success) {
                        System.out.println(">> Job " + jobId + " DONE! Saved at: " + outputPath);
                        UdpSender.sendProgress(jobId, "DONE", 100);
                        // QUAN TRỌNG: Trả về đường dẫn MỚI để Web lưu vào DB
                        out.println("OK|Success|" + outputPath); 
                    } else {
                        System.err.println(">> Job " + jobId + " FAILED!");
                        UdpSender.sendProgress(jobId, "FAILED", 0);
                        out.println("ERROR|Failed");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}