package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import bean.Jobs;
import bean.Jobs.JobStatus;
import utils.ConnectDB; 

public class JobDAO {

    // Hàm thêm Job và TRẢ VỀ ID (Quan trọng cho Worker)
    public int addJob(Jobs job) {
        int generatedId = -1;
        String sql = "INSERT INTO jobs (video_id, target_format, status, progress, created_at) VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = new ConnectDB().getConnection();
             // Tham số thứ 2: RETURN_GENERATED_KEYS -> Yêu cầu trả về ID tự tăng
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, job.getVideo_id());
            ps.setString(2, job.getTarget_format());
            ps.setString(3, job.getStatus().name());
            ps.setInt(4, job.getProgress()); // Mặc định là 0

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                // Lấy key vừa sinh ra
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1); // Lấy cột đầu tiên (ID)
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId; // Trả về ID (ví dụ 88), hoặc -1 nếu lỗi
    }

    // Hàm cập nhật trạng thái Job
    public boolean updateJob(Jobs job) {
        String sql = "UPDATE jobs SET status = ?, progress = ? WHERE job_id = ?";
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

        	ps.setString(1, job.getStatus().name());
            ps.setInt(2, job.getProgress());
            ps.setInt(3, job.getJob_id()); // 
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Jobs> getAllJobs(int clientId) {
        List<Jobs> jobsList = new ArrayList<>();
        String sql = "SELECT j.* FROM jobs j " +
                     "INNER JOIN videos v ON j.video_id = v.video_id " +
                     "WHERE v.client_id = ?";

        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobsList.add(extract(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobsList;
    }
    
    public Jobs getJob(int jobId) {
        String sql = "SELECT * FROM jobs WHERE job_id = ?";

        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extract(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // nếu không tìm thấy
    }
    
    public static Jobs extract(ResultSet rs) throws Exception {
        int jobId = rs.getInt("job_id");
        int videoId = rs.getInt("video_id");
        String targetFormat = rs.getString("target_format");
        JobStatus status = JobStatus.valueOf(rs.getString("status"));
        int progress = rs.getInt("progress");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new Jobs(jobId, videoId, targetFormat, status, progress, createdAt, updatedAt);
    }
}

   