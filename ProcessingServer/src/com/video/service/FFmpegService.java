package com.video.service;

import com.video.config.AppConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFmpegService {

    public boolean convertVideo(String inputPath, String outputPath) {
        try {
            List<String> command = new ArrayList<>();
            command.add(AppConfig.FFMPEG_PATH);
            
            // 1. Input file
            command.add("-i");
            command.add(inputPath);
            
            // --- PHẦN TỐI ƯU (OPTIMIZATION LEVEL 2) ---
            
            // A. Ép sử dụng Codec H.264 (Để các tham số bên dưới hoạt động chuẩn nhất)
            command.add("-c:v");
            command.add("libx264");

            // B. Preset: 'veryfast' 
            // Tốc độ xử lý cực nhanh, file sinh ra dung lượng hợp lý.
            command.add("-preset");
            command.add("veryfast");
            
            // C. CRF (Constant Rate Factor): 23
            // Đây là mức cân bằng vàng. Số càng nhỏ càng nét (file nặng), số càng lớn càng mờ.
            // 23 là chuẩn mặc định cho chất lượng HD/Web.
            command.add("-crf");
            command.add("23");

            // D. Movflags: +faststart (QUAN TRỌNG CHO WEB)
            // Chuyển metadata (thông tin video) lên đầu file. 
            // Giúp trình duyệt load là chạy ngay, không cần tải hết file.
            command.add("-movflags");
            command.add("+faststart");

            // E. Resize về chuẩn HD (1280px chiều ngang)
            // Nếu không cần resize thì bạn có thể comment 2 dòng này lại.
            command.add("-vf");
            command.add("scale=1280:-2"); 
            
            // F. Audio Codec: AAC
            // Đảm bảo âm thanh nghe được trên mọi trình duyệt/điện thoại
            command.add("-c:a");
            command.add("aac");

            // 2. Ghi đè file nếu tồn tại
            command.add("-y");
            
            // 3. Output file
            command.add(outputPath);

            // --- THỰC THI LỆNH ---
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true); // Gộp luồng lỗi vào luồng chính để đọc log
            Process process = builder.start();

            // Đọc log FFmpeg (Bắt buộc phải có để giải phóng bộ nhớ đệm, tránh treo tiến trình)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Uncomment dòng dưới nếu muốn xem log FFmpeg chạy thế nào trong Console
                // System.out.println("[FFmpeg]: " + line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0; // Trả về true nếu thành công (exit = 0)

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * LEVEL 3: SMART COPY (Siêu tốc độ)
     * Chỉ thay đổi đuôi file (Container), giữ nguyên luồng video/audio bên trong.
     * Tốc độ: < 5 giây cho video 1 tiếng.
     */
    public boolean convertFastCopy(String inputPath, String outputPath) {
        try {
            List<String> command = new ArrayList<>();
            command.add(AppConfig.FFMPEG_PATH);
            
            command.add("-i"); 
            command.add(inputPath);
            
            // CỜ QUAN TRỌNG NHẤT: -c copy
            // Nghĩa là: "Copy nguyên si, cấm giải mã/nén lại"
            command.add("-c");
            command.add("copy");
            
            // Vẫn thêm movflags để hỗ trợ Web tốt hơn nếu định dạng đích là MP4/MOV
            if (outputPath.endsWith(".mp4") || outputPath.endsWith(".mov")) {
                command.add("-movflags");
                command.add("+faststart");
            }

            command.add("-y");
            command.add(outputPath);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Vẫn phải đọc log
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null) {}

            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}