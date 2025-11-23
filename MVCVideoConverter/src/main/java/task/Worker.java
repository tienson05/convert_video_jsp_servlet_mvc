package task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import bean.ConvertedFiles;
import bean.Jobs;
import bean.Videos;
import bo.ConvertedFileBO;
import bo.JobBO;

public class Worker {
    
    // --- CẤU HÌNH CHỊU TẢI (SCALABILITY CONFIG) ---
    
    // 1. Số nhân viên chính thức (Số luồng chạy đồng thời)
    // Gợi ý: Với Worker Web (IO Bound), nên nhân 4 số nhân CPU để tối ưu
    // private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4; 
    private static final int CORE_POOL_SIZE = 5; // Hoặc để 5 như cũ cũng được
    
    // 2. Sức chứa của phòng chờ (Hàng đợi)
    private static final int MAX_QUEUE_CAPACITY = 100; 
    
    // 3. Khởi tạo ThreadPool thủ công
    private static final ExecutorService executor = new ThreadPoolExecutor(
        CORE_POOL_SIZE,             // Số luồng nòng cốt
        CORE_POOL_SIZE,             // Số luồng tối đa
        0L, TimeUnit.MILLISECONDS,  // Thời gian sống của luồng thừa
        new ArrayBlockingQueue<>(MAX_QUEUE_CAPACITY), // Hàng đợi CÓ GIỚI HẠN
        new ThreadPoolExecutor.AbortPolicy()          // Chính sách: Đuổi về nếu full
    );
    
    private static final String SERVER_IP = "127.0.0.1"; 
    private static final int SERVER_PORT = 9000;

    public static void submitConversionTask(Videos video, String targetFormat, String mode) {
        try {
            // Cố gắng đưa tác vụ vào hồ bơi
            executor.submit(() -> {
                processTask(video, targetFormat, mode);
            });
        } catch (RejectedExecutionException e) {
            // --- XỬ LÝ KHI SERVER QUÁ TẢI (Hàng đợi > 100) ---
            System.err.println(">> [SERVER OVERLOAD] Hàng đợi đã đầy. Từ chối Video ID: " + video.getVideo_id());
            // Ở đây bạn có thể log lỗi hoặc update trạng thái REJECTED vào DB nếu muốn
        }
    }

    // Tách logic xử lý ra hàm riêng
    private static void processTask(Videos video, String targetFormat, String mode) {
        JobBO jobBO = new JobBO();
        ConvertedFileBO cfBO = new ConvertedFileBO();
        
        int jobId = -1;

        try {
            // BƯỚC 1: TẠO JOB
            Jobs job = new Jobs();
            job.setVideo_id(video.getVideo_id());
            job.setTarget_format(targetFormat);
            job.setStatus(Jobs.JobStatus.PENDING); 
            
            jobId = jobBO.addJob(job); 
            
            if (jobId <= 0) {
                System.err.println("[Worker] Error: Could not create Job in DB.");
                return;
            }
            
            System.out.println("[Worker] Created Job ID: " + jobId + ". Mode: " + mode);

            // BƯỚC 2: GỬI TCP
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String command = "CONVERT|" + jobId + "|" + video.getStored_path() + "|" + targetFormat + "|" + mode;
                out.println(command);
                System.out.println("[Worker] Sent TCP: " + command);

                // BƯỚC 3: ĐỢI KẾT QUẢ
                String response = in.readLine();
                System.out.println("[Worker] Received TCP: " + response);

                if (response != null && response.startsWith("OK")) {
                    String[] parts = response.split("\\|");
                    String outputPath = (parts.length >= 3) ? parts[2] : "";

                    // BƯỚC 4: THÀNH CÔNG
                    ConvertedFiles cf = new ConvertedFiles();
                    cf.setJob_id(jobId);      
                    cf.setOutput_path(outputPath);
                    cf.setDuration_seconds(video.getDuration_seconds());
                    
                    java.io.File resultFile = new java.io.File(outputPath);
                    if (resultFile.exists()) cf.setSize(resultFile.length());
                    else cf.setSize(0);
                    
                    cfBO.addConvertedFile(cf);

                    job.setJob_id(jobId);     
                    job.setStatus(Jobs.JobStatus.COMPLETED);
                    job.setProgress(100);
                    jobBO.updateJob(job); 

                    System.out.println("[Worker] Job " + jobId + " completed successfully!");

                } else {
                    handleFailure(jobBO, jobId, "Processing Failed");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (jobId > 0) handleFailure(jobBO, jobId, "System Error");
        }
    }

    private static void handleFailure(JobBO jobBO, int jobId, String reason) {
        try {
            System.err.println("[Worker] Job " + jobId + " failed: " + reason);
            Jobs job = new Jobs();
            job.setJob_id(jobId);
            job.setStatus(Jobs.JobStatus.FAILED);
            jobBO.updateJob(job);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}