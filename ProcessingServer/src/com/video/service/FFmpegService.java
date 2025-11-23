package com.video.service;

import com.video.config.AppConfig;
import com.video.network.UdpSender;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegService {

    public boolean convertVideo(String inputPath, String outputPath, int jobId) {
        try {
            List<String> command = new ArrayList<>();
            command.add(AppConfig.FFMPEG_PATH);
            command.add("-i"); command.add(inputPath);
            
            // --- CẤU HÌNH CHỐNG LAG & TĂNG TỐC ---
            
            // 1. GIỚI HẠN SỐ LUỒNG CPU (Quan trọng để không bị đơ máy)
            // Máy bạn 12 nhân -> Để 6 hoặc 8 thôi. Để lại tài nguyên cho Win chạy.
            command.add("-threads"); 
            command.add("6"); 

            if (outputPath.endsWith(".webm")) {
                // === CẤU HÌNH WEBM (VP9) SIÊU TỐC ===
                command.add("-c:v"); command.add("libvpx-vp9");
                command.add("-c:a"); command.add("libopus");
                
                // Giảm chất lượng nén để tăng tốc độ (Quan trọng nhất)
                command.add("-deadline"); command.add("realtime"); 
                command.add("-cpu-used"); command.add("8"); // Mức cao nhất (nhanh nhất)
                
                // Kích hoạt đa luồng cho VP9 (nhưng bị giới hạn bởi -threads ở trên)
                command.add("-row-mt"); command.add("1");   
                
                // Giới hạn Bitrate để file không bị phình to quá
                command.add("-b:v"); command.add("1M"); 

            } else {
                // === CẤU HÌNH MP4/AVI... ===
                // MP4 thì nhẹ nên cứ để ultrafast
                command.add("-c:v"); command.add("libx264");
                command.add("-c:a"); command.add("aac");
                command.add("-preset"); command.add("ultrafast");
                command.add("-movflags"); command.add("+faststart");
            }

            // Resize về HD (720p) để giảm tải tính toán
            command.add("-vf"); command.add("scale=1280:-2"); 
            
            command.add("-y"); command.add(outputPath);

            // --- THỰC THI ---
            return executeCommand(command, jobId);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean convertFastCopy(String inputPath, String outputPath, int jobId) {
        try {
            List<String> command = new ArrayList<>();
            command.add(AppConfig.FFMPEG_PATH);
            command.add("-i"); command.add(inputPath);
            command.add("-c"); command.add("copy");
            
            if (outputPath.endsWith(".mp4") || outputPath.endsWith(".mov")) {
                command.add("-movflags"); command.add("+faststart");
            }
            command.add("-y"); command.add(outputPath);

            return executeCommand(command, jobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // HÀM CHUNG
    private boolean executeCommand(List<String> command, int jobId) {
        try {
            System.out.println(">> [CMD]: " + String.join(" ", command));

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true); 
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            double totalDuration = 0;
            int lastPercent = 0;
            Pattern timePattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");

            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration:") && totalDuration == 0) {
                    String[] parts = line.split("Duration: ");
                    if (parts.length > 1) {
                        String timeStr = parts[1].split(",")[0];
                        totalDuration = parseTimeToSeconds(timeStr);
                    }
                }
                if (line.contains("time=") && totalDuration > 0) {
                    Matcher matcher = timePattern.matcher(line);
                    if (matcher.find()) {
                        String currentTimeStr = matcher.group(0);
                        double currentSeconds = parseTimeToSeconds(currentTimeStr);
                        int percent = (int) ((currentSeconds / totalDuration) * 100);
                        if (percent > lastPercent && percent <= 100) {
                            lastPercent = percent;
                            UdpSender.sendProgress(jobId, "PROCESSING", percent);
                        }
                    }
                }
                // In lỗi nếu có (để debug)
                if(line.toLowerCase().contains("error")) System.err.println(line);
            }
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private double parseTimeToSeconds(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            double h = Double.parseDouble(parts[0]);
            double m = Double.parseDouble(parts[1]);
            double s = Double.parseDouble(parts[2]);
            return h * 3600 + m * 60 + s;
        } catch (Exception e) { return 0; }
    }
}