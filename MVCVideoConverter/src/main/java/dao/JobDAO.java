package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import bean.Jobs;
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
}

   