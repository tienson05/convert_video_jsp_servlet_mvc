package listener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import bean.Jobs;
import bo.JobBO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// @WebListener giúp Tomcat tự động chạy file này khi khởi động web
// tức là khi chạy web server thì sẽ bắt những UDP send là sẽ cập nhật trong DB luôn
// vậy nên chỉ với file JobProgressListener.java này thôi thì cũng đủ xong chức năng báo cáo tiến độ rồi
@WebListener 
public class JobProgressListener implements ServletContextListener {

    private DatagramSocket socket;
    private Thread listenerThread;
    private boolean running = true;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== [WEB] UDP LISTENER STARTED ON PORT 9001 ===");
        
        // Tạo luồng riêng để nghe UDP, tránh làm treo Web
        listenerThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(9001); // Mở cổng 9001 để hứng tin
                byte[] buffer = new byte[1024];

                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Chờ nhận gói tin...

                    // 1. Đọc tin nhắn: PROGRESS|jobId|status|percent
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    // System.out.println("[UDP RECV] " + msg); // Uncomment để debug

                    String[] parts = msg.split("\\|");
                    if (parts.length >= 4 && "PROGRESS".equals(parts[0])) {
                        try {
                            int jobId = Integer.parseInt(parts[1]);
                            // String status = parts[2]; // "PROCESSING"
                            int percent = Integer.parseInt(parts[3]);

                            // 2. Cập nhật Database ngay lập tức
                            // Lưu ý: Cần tối ưu để không spam DB quá nhiều (VD: mỗi 5-10% mới update)
                            JobBO jobBO = new JobBO();
                            
                            // Tạo object job tạm để update
                            Jobs job = new Jobs();
                            job.setJob_id(jobId);
                            job.setStatus(Jobs.JobStatus.PROCESSING); // Đang chạy
                            job.setProgress(percent);
                            
                            // Gọi hàm update (Cần đảm bảo JobDAO.updateJob chỉ update status/progress)
                            jobBO.updateJob(job);

                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi format UDP: " + msg);
                        }
                    }
                }
            } catch (Exception e) {
                if (running) e.printStackTrace();
            }
        });
        
        listenerThread.start(); // Bắt đầu chạy
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Khi tắt Tomcat thì tắt luôn luồng này
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("=== [WEB] UDP LISTENER STOPPED ===");
    }
}