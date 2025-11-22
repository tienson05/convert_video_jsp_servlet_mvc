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

public class Worker {
    // Thread pool 10 luồng
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); 
    
    private static final String SERVER_IP = "127.0.0.1"; 
    private static final int SERVER_PORT = 9000;

    public static void submitConversionTask(Videos video, String targetFormat, String mode) {
        executor.submit(() -> {
            JobBO jobBO = new JobBO();
            ConvertedFileBO cfBO = new ConvertedFileBO();
            
            int jobId = -1;

            try {
                // ---------------------------------------------------------
                // BƯỚC 1: TẠO JOB
                // ---------------------------------------------------------
                Jobs job = new Jobs();
                job.setVideo_id(video.getVideo_id());
                job.setTarget_format(targetFormat);
                
                // Dùng Enum PENDING
                job.setStatus(Jobs.JobStatus.PENDING); 
                
                jobId = jobBO.addJob(job); 
                
                if (jobId <= 0) {
                    System.err.println("[Worker] Error: Could not create Job in DB.");
                    return;
                }
                
                System.out.println("[Worker] Created Job ID: " + jobId + ". Mode: " + mode);

                // ---------------------------------------------------------
                // BƯỚC 2: GỬI TCP
                // ---------------------------------------------------------
                try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Gửi lệnh kèm mode
                    String command = "CONVERT|" + jobId + "|" + video.getStored_path() + "|" + targetFormat + "|" + mode;
                    
                    out.println(command);
                    System.out.println("[Worker] Sent TCP: " + command);

                    // ---------------------------------------------------------
                    // BƯỚC 3: ĐỢI KẾT QUẢ
                    // ---------------------------------------------------------
                    String response = in.readLine();
                    System.out.println("[Worker] Received TCP: " + response);

                    if (response != null && response.startsWith("OK")) {
                        // Protocol: OK|Success|Output/Path
                        String[] parts = response.split("\\|");
                        String outputPath = (parts.length >= 3) ? parts[2] : "";

                        // -----------------------------------------------------
                        // BƯỚC 4: THÀNH CÔNG -> UPDATE DB
                        // -----------------------------------------------------
                        
                        // A. Lưu vào bảng converted_files
                        ConvertedFiles cf = new ConvertedFiles();
                        
                        cf.setJob_id(jobId);      
                        cf.setOutput_path(outputPath);
                        cf.setDuration_seconds(video.getDuration_seconds());
                     // --- LOGIC MỚI: Đọc kích thước file thật ---
                        java.io.File resultFile = new java.io.File(outputPath);
                        if (resultFile.exists()) {
                            long bytes = resultFile.length();
                            cf.setSize(bytes); // Lưu size thật (byte)
                        } else {
                            cf.setSize(0);
                        }
                        // ------------------------------------------                        
                        cfBO.addConvertedFile(cf);

                        // B. Cập nhật trạng thái Job -> COMPLETED
                        
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
                if (jobId > 0) {
                    handleFailure(jobBO, jobId, "System Error");
                }
            }
        });
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