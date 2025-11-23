package task;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import jakarta.servlet.http.HttpServletResponse;

import bean.Jobs;
import dao.JobDAO;

public class Download {

    /**
     * Truyền video về client.
     * @param jobId ID của job
     * @param response HttpServletResponse (nếu dùng servlet/web)
     * @return true nếu thành công, false nếu lỗi
     */
    public static boolean DownloadJob(int jobId, HttpServletResponse response) {
        JobDAO jobDAO = new JobDAO();
        Jobs job = jobDAO.getJob(jobId);

        if (job == null) {
            System.err.println("Job not found: " + jobId);
            return false;
        }

        // Ví dụ: file video lưu trên server, đường dẫn từ Jobs (giả sử có getFilePath)
        String videoPath = "D:/videos/" + job.getVideo_id() + "." + job.getTarget_format();
        File videoFile = new File(videoPath);

        if (!videoFile.exists()) {
            System.err.println("File not found: " + videoPath);
            return false;
        }

        try (FileInputStream in = new FileInputStream(videoFile);
             OutputStream out = response.getOutputStream()) {

            // Cấu hình header để client download
            response.setContentType("video/" + job.getTarget_format());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + videoFile.getName() + "\"");
            response.setContentLengthLong(videoFile.length());

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
