package task;

import java.sql.Timestamp;
import bean.Videos;
import bean.Videos.VideoStatus;

public class TestWorker {

    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST WORKER ===");

        // 1. GIẢ LẬP ĐỐI TƯỢNG VIDEO (Thay vì query DB cho mất công)
        // Bạn điền thông tin y hệt như trong Database
        Videos mockVideo = new Videos();
        mockVideo.setVideo_id(1); // ID khớp với DB để lát nữa Worker update trạng thái
        mockVideo.setClient_id(1);
        
        // QUAN TRỌNG: Đường dẫn này phải có thật
        mockVideo.setStored_path("D:\\data\\uploads\\test.mp4"); 
        
        mockVideo.setOriginal_filename("test.mp4");
        mockVideo.setStatus(VideoStatus.UPLOADED);
        mockVideo.setCreated_at(new Timestamp(System.currentTimeMillis()));

        // 2. CẤU HÌNH THAM SỐ CONVERT
        String targetFormat = "mkv"; // Muốn chuyển sang đuôi gì?
        String mode = "FAST";        // Hoặc "NORMAL"

        System.out.println(">> Đang gửi yêu cầu Convert cho Video ID: " + mockVideo.getVideo_id());
        System.out.println(">> Input: " + mockVideo.getStored_path());
        System.out.println(">> Target: " + targetFormat + " | Mode: " + mode);

        // 3. GỌI WORKER (Kích hoạt tiến trình)
        // Hàm này sẽ chạy bất đồng bộ (Async)
        Worker.submitConversionTask(mockVideo, targetFormat, mode);

        // 4. GIỮ CHƯƠNG TRÌNH KHÔNG BỊ TẮT
        // Vì Worker chạy ngầm (Thread), nếu hàm main kết thúc ngay thì Worker cũng chết theo.
        // Ta cho hàm main ngủ 15 giây để đợi Worker làm xong việc.
        try {
            System.out.println(">> Đang chờ Processing Server xử lý... (Giữ kết nối 15s)");
            for (int i = 0; i < 15; i++) {
                Thread.sleep(1000);
                System.out.print("."); // In dấu chấm cho đỡ buồn
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n=== KẾT THÚC TEST ===");
    }
}