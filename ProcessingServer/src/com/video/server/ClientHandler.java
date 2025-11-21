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
            String request = in.readLine();
            System.out.println("[TCP Received] " + request);

            if (request != null && request.startsWith(AppConfig.CMD_CONVERT)) {
                // Protocol: CONVERT|jobId|inputPath|outputFormat|MODE
                String[] parts = request.split(AppConfig.PROTOCOL_SEPARATOR);
                
                if (parts.length >= 4) {
                    int jobId = Integer.parseInt(parts[1]);
                    String inputPath = parts[2];
                    String format = parts[3]; 
                    String mode = (parts.length >= 5) ? parts[4] : "NORMAL";
                    
                    // --- 1. CHUẨN BỊ THƯ MỤC OUTPUT ---
                    File outputDir = new File(AppConfig.OUTPUT_DIR);
                    if (!outputDir.exists()) outputDir.mkdirs();

                    // --- 2. KIỂM TRA DUNG LƯỢNG ---
                    long freeSpace = outputDir.getFreeSpace(); 
                    long safeMargin = 500 * 1024 * 1024L; 

                    if (freeSpace < safeMargin) {
                        System.err.println(">> [CRITICAL] Output Disk Full! Rejecting Job " + jobId);
                        UdpSender.sendProgress(jobId, "FAILED_DISK_FULL", 0);
                        out.println("ERROR|Server Disk Full");
                        return;
                    }

                    // --- 3. TẠO ĐƯỜNG DẪN OUTPUT ---
                    File inputFile = new File(inputPath);
                    String originalName = inputFile.getName();
                    String baseName = originalName.contains(".") 
                                    ? originalName.substring(0, originalName.lastIndexOf(".")) 
                                    : originalName;

                    // Logic đặt tên file (để tạm _optimized, lát nếu fast thành công thì tốt, không thì vẫn là file này)
                    String fileNameSuffix = "FAST".equalsIgnoreCase(mode) ? "_fast" : "_optimized";
                    String outputPath = outputDir.getAbsolutePath() + File.separator + baseName + fileNameSuffix + "." + format;

                    // ---------------------------------------------------------

                    FFmpegService ffmpeg = new FFmpegService();
                    boolean success = false;

                    // --- 4. LOGIC XỬ LÝ THÔNG MINH (SMART FALLBACK) ---
                    
                    if ("FAST".equalsIgnoreCase(mode)) {
                        System.out.println(">> Job " + jobId + ": Trying FAST MODE (Copy)...");
                        UdpSender.sendProgress(jobId, "PROCESSING_FAST", 0);
                        
                        // Thử chạy Fast Mode
                        boolean fastSuccess = ffmpeg.convertFastCopy(inputPath, outputPath);
                        
                        if (fastSuccess) {
                            success = true;
                            System.out.println(">> Job " + jobId + ": Fast Mode Success!");
                        } else {
                            // NẾU FAST MODE THẤT BẠI -> CHUYỂN SANG NORMAL MODE
                            System.err.println(">> [WARNING] Job " + jobId + ": Fast Mode failed (Incompatible format). Switching to NORMAL MODE...");
                            
                            UdpSender.sendProgress(jobId, "PROCESSING_FALLBACK", 0);
                            
                            // Gọi lệnh Convert thường (Nén lại)
                            success = ffmpeg.convertVideo(inputPath, outputPath);
                        }

                    } else {
                        // Chế độ Normal (User chọn từ đầu)
                        System.out.println(">> Job " + jobId + ": Detect NORMAL MODE -> Saving to: " + outputPath);
                        UdpSender.sendProgress(jobId, "PROCESSING", 0);
                        Thread.sleep(500); 
                        UdpSender.sendProgress(jobId, "PROCESSING", 10);
                        
                        success = ffmpeg.convertVideo(inputPath, outputPath);
                    }

                    // --- 5. TRẢ KẾT QUẢ ---
                    if (success) {
                        System.out.println(">> Job " + jobId + " DONE! Saved at: " + outputPath);
                        UdpSender.sendProgress(jobId, "DONE", 100);
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