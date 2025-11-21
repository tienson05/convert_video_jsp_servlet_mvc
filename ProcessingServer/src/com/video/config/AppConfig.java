package com.video.config;

public class AppConfig {
    // Cấu hình Server
    public static final int TCP_PORT = 9000; // Web Server kết nối vào đây
    public static final int UDP_PORT = 9001; // Web Server lắng nghe ở đây (để nhận progress)
    public static final String WEB_SERVER_IP = "127.0.0.1"; // IP của Web Server

    // Cấu hình Protocol (Dấu phân cách)
    public static final String PROTOCOL_SEPARATOR = "\\|"; // Dùng cho split (regex)
    public static final String MSG_SEPARATOR = "|";        // Dùng để nối chuỗi

    // Lệnh Protocol
    public static final String CMD_CONVERT = "CONVERT";
    public static final String CMD_PROGRESS = "PROGRESS";
    public static final String CMD_RESULT = "RESULT";
    
    // Đường dẫn FFmpeg (Sửa lại cho đúng đường dẫn trên máy bạn)
    // Nếu đã cài biến môi trường thì chỉ cần để "ffmpeg", nếu chưa thì để đường dẫn full exe
    public static final String FFMPEG_PATH = "ffmpeg"; 
    public static final String OUTPUT_DIR = "D:\\CODING\\HK1-25-26\\LTM\\VideoOutput";
}