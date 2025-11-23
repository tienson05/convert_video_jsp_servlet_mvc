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
                String[] parts = request.split(AppConfig.PROTOCOL_SEPARATOR);
                
                if (parts.length >= 4) {
                    int jobId = Integer.parseInt(parts[1]);
                    String rawInputPath = parts[2];
                    // Thay thế tất cả dấu / thành \ để Windows hiểu
                    String inputPath = rawInputPath.replace("/", "\\"); 
                    // ----------------------------------------------

                    String format = parts[3]; 
                    // Không cần quan tâm biến 'mode' gửi sang nữa, vì mình luôn tự động xử lý
                    
                    // --- 1. CHUẨN BỊ THƯ MỤC OUTPUT ---
                    File outputDir = new File(AppConfig.OUTPUT_DIR);
                    if (!outputDir.exists()) outputDir.mkdirs();

                    // --- 2. KIỂM TRA DUNG LƯỢNG ---
                    long freeSpace = outputDir.getFreeSpace(); 
                    if (freeSpace < 500 * 1024 * 1024L) {
                        System.err.println(">> [CRITICAL] Output Disk Full! Rejecting Job " + jobId);
                        UdpSender.sendProgress(jobId, "FAILED_DISK_FULL", 0);
                        out.println("ERROR|Server Disk Full");
                        return;
                    }

                    // --- 3. TẠO ĐƯỜNG DẪN FILE ĐẦU RA ---
                    File inputFile = new File(inputPath);
                    String originalName = inputFile.getName();
                    
                    // Lấy tên file không đuôi
                    String baseName = originalName.contains(".") 
                                    ? originalName.substring(0, originalName.lastIndexOf(".")) 
                                    : originalName;

                    // Đặt tên file chung là _converted (hoặc _new) cho thống nhất
                    String outputPath = outputDir.getAbsolutePath() + File.separator + baseName + "_converted." + format;

                    // ---------------------------------------------------------

                    FFmpegService ffmpeg = new FFmpegService();
                    boolean success = false;

                    // --- 4. QUY TRÌNH XỬ LÝ TỰ ĐỘNG (AUTO SMART) ---
                    
                    System.out.println(">> Job " + jobId + ": Auto-processing started. Trying FAST MODE first...");
                    UdpSender.sendProgress(jobId, "PROCESSING_FAST", 0);
                    
                    // BƯỚC A: Thử chạy Fast Mode (Copy luồng)
                    boolean fastSuccess = ffmpeg.convertFastCopy(inputPath, outputPath, jobId);                    
                    if (fastSuccess) {
                        success = true;
                        System.out.println(">> Job " + jobId + ": Fast Mode Success! (Instant Copy)");
                    } else {
                        // BƯỚC B: Nếu Fast thất bại -> Tự động chuyển sang Normal
                        System.err.println(">> [AUTO-SWITCH] Job " + jobId + ": Fast Mode not compatible. Switching to NORMAL MODE (Re-encoding)...");
                        
                        UdpSender.sendProgress(jobId, "PROCESSING_FALLBACK", 0);
                        
                        // Chạy lệnh Normal (Ultrafast preset)
                        success = ffmpeg.convertVideo(inputPath, outputPath, jobId);
                    }

                    // --- 5. TRẢ KẾT QUẢ ---
                    if (success) {
                        System.out.println(">> Job " + jobId + " DONE! Saved at: " + outputPath);
                        UdpSender.sendProgress(jobId, "DONE", 100);
                        out.println("OK|Success|" + outputPath); 
                    } else {
                        System.err.println(">> Job " + jobId + " FAILED ALL ATTEMPTS!");
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