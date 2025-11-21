package com.video.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        // Đường dẫn file video thật của bạn
        String inputPath = "D:\\CODING\\HK1-25-26\\LTM\\Videotest\\test.mp4"; 
        
        // Cấu hình kết nối
        String serverIp = "localhost";
        int serverPort = 9000;

        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println(">> Đã kết nối tới Server " + serverIp + ":" + serverPort);

            // --- LỰA CHỌN CHẾ ĐỘ TEST (Bỏ comment dòng bạn muốn chạy) ---

            // CÁCH 1: Chế độ NORMAL (Có nén, có resize, chạy lâu hơn xíu nhưng file nhẹ)
            // String command = "CONVERT|1|" + inputPath + "|avi|NORMAL";
            
            // CÁCH 2: Chế độ FAST (Copy luồng, siêu nhanh, giữ nguyên chất lượng)
            // Lưu ý: Chế độ này đổi đuôi file là chính (ví dụ mp4 -> mkv)
             String command = "CONVERT|2|" + inputPath + "|mkv|FAST";

            // ------------------------------------------------------------

            System.out.println(">> Sending Command: " + command);
            out.println(command); // Gửi lệnh đi

            // Đọc phản hồi từ Server (để xem kết quả cuối cùng)
            String response = in.readLine();
            System.out.println(">> Server Response: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}