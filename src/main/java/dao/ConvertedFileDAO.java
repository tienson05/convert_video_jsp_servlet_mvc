package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import bean.ConvertedFiles;
// Import class kết nối DB 
import utils.ConnectDB; 

public class ConvertedFileDAO {

    public boolean addConvertedFile(ConvertedFiles cf) {
    	String sql = "INSERT INTO Converted_files (job_id, output_filename, output_path, size, duration_seconds, created_at) VALUES (?, ?, ?, ?, ?, NOW())";        
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 1. Set Job ID
            ps.setInt(1, cf.getJob_id());

            // 2. Xử lý output_filename (Tách từ đường dẫn full)
            // Ví dụ: D:/Output/video_fast.mp4 -> video_fast.mp4
            String fullPath = cf.getOutput_path();
            String fileName = "unknown";
            if (fullPath != null && !fullPath.isEmpty()) {
                fileName = new File(fullPath).getName();
            }
            ps.setString(2, fileName);

            // 3. Set Output Path
            ps.setString(3, fullPath);

            // 4. Set File Size (Nếu Worker chưa set thì mặc định là 0)
            ps.setLong(4, cf.getSize());
            //DÒNG NÀY ĐỂ LƯU DURATION
            ps.setDouble(5, cf.getDuration_seconds());

            // Thực thi
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error adding converted file: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public ConvertedFiles getFileByJobId(int jobId) throws SQLException {
    	String sql = "SELECT * FROM converted_files WHERE job_id = ?";
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ConvertedFiles file = new ConvertedFiles();
                    file.setId(rs.getInt("id"));
                    file.setJob_id(rs.getInt("job_id"));
                    file.setOutput_filename(rs.getString("output_filename"));
                    file.setOutput_path(rs.getString("output_path"));
                    file.setSize(rs.getLong("size"));
                    file.setDuration_seconds(rs.getDouble("duration_seconds"));
                    file.setCreated_at(rs.getTimestamp("created_at"));
                    return file;
                }
            }
        }
        return null; // không tìm thấy
    }
} 