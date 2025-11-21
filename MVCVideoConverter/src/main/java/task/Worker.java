package task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bean.ConvertedFiles;
import bean.Jobs;
import bean.Videos;
import bo.ConvertedFileBO;
import bo.JobBO;
import bo.VideoBO;

public class Worker {
    // Thread pool: Web Server cũng cần pool để không bị treo khi chờ Processing Server trả lời
    private static final ExecutorService executor = Executors.newFixedThreadPool(5); 
    
    // Cấu hình kết nối đến Processing Server (Của bạn)
    private static final String SERVER_IP = "127.0.0.1"; // Nếu chạy khác máy thì sửa IP này
    private static final int SERVER_PORT = 9000;

    public static void submitConversionTask(Videos video, String targetFormat) {
        executor.submit(() -> {
            JobBO jobBO = new JobBO();
            ConvertedFileBO cfBO = new ConvertedFileBO();
            VideoBO videoBO = new VideoBO();
            
            int jobId = -1;

            try {
                // ---------------------------------------------------------
                // BƯỚC 1: TẠO JOB TRONG DB (Trạng thái PENDING)
                // ---------------------------------------------------------
                Jobs job = new Jobs();
                job.setVideo_id(video.getVideo_id());
                job.setTarget_format(targetFormat);
                job.setStatus(Jobs.JobStatus.PENDING); // Ban đầu là chờ
                
                // LƯU Ý CHO TOÀN: Hàm addJob phải trả về ID tự tăng (job_id) mới đúng chuẩn
                jobId = jobBO.addJob(job); 
                
                if (jobId <= 0) {
                    System.err.println("[Worker] Error: Could not create Job in DB.");
                    return;
                }
                
                System.out.println("[Worker] Created Job ID: " + jobId + ". Connecting to Processing Server...");

                // ---------------------------------------------------------
                // BƯỚC 2: GỬI YÊU CẦU QUA TCP SOCKET
                // ---------------------------------------------------------
                try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Chuẩn bị lệnh: CONVERT|jobId|inputPath|format|MODE
                    // Mặc định là NORMAL, nếu muốn FAST thì sửa logic ở đây
                    String command = "CONVERT|" + jobId + "|" + video.getStored_path() + "|" + targetFormat + "|NORMAL";
                    
                    // Gửi lệnh đi
                    out.println(command);
                    System.out.println("[Worker] Sent TCP: " + command);

                    // ---------------------------------------------------------
                    // BƯỚC 3: ĐỢI KẾT QUẢ TRẢ VỀ (Blocking)
                    // Server của bạn sẽ giữ kết nối và trả về khi convert xong
                    // ---------------------------------------------------------
                    String response = in.readLine();
                    System.out.println("[Worker] Received TCP: " + response);

                    if (response != null && response.startsWith("OK")) {
                        // Protocol: OK|Message|OutputPath
                        String[] parts = response.split("\\|");
                        String outputPath = parts[2]; // Đây là đường dẫn file kết quả

                        // -----------------------------------------------------
                        // BƯỚC 4: XỬ LÝ THÀNH CÔNG -> UPDATE DB
                        // -----------------------------------------------------
                        
                        // A. Lưu vào bảng converted_files
                        ConvertedFiles cf = new ConvertedFiles();
                        cf.setJob_id(jobId);
                        cf.setOutput_path(outputPath);
                        cf.setSize(0); // (Tùy chọn) Có thể cập nhật sau
                        cfBO.addConvertedFile(cf);

                        // B. Cập nhật trạng thái Job -> COMPLETED
                        // LƯU Ý CHO TOÀN: Cần có hàm updateStatus trong JobBO
                        job.setJob_id(jobId);
                        job.setStatus(Jobs.JobStatus.COMPLETED);
                        job.setProgress(100);
                        jobBO.updateJob(job); 

                        System.out.println("[Worker] Job " + jobId + " completed successfully!");

                    } else {
                        // -----------------------------------------------------
                        // BƯỚC 5: XỬ LÝ THẤT BẠI TỪ SERVER
                        // -----------------------------------------------------
                        handleFailure(jobBO, jobId, "Processing Failed");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Xử lý lỗi kết nối hoặc lỗi code
                if (jobId > 0) {
                    handleFailure(jobBO, jobId, "System Error");
                }
            }
        });
    }

    // Hàm phụ để update lỗi cho gọn code
    private static void handleFailure(JobBO jobBO, int jobId, String reason) {
        try {
            System.err.println("[Worker] Job " + jobId + " failed: " + reason);
            Jobs job = new Jobs();
            job.setJob_id(jobId);;
            job.setStatus(Jobs.JobStatus.FAILED);
            jobBO.updateJob(job);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}