package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import bean.ConvertedFiles;
// Import class kết nối DB của nhóm (Sửa lại cho đúng tên file của nhóm bạn)
import utils.ConnectDB; 

public class ConvertedFileDAO {

    public boolean addConvertedFile(ConvertedFiles cf) {
        // Kiểm tra lại tên bảng: 'Converted_files' hay 'converted_files' (MySQL Linux phân biệt hoa thường)
        String sql = "INSERT INTO Converted_files (job_id, output_filename, output_path, size, created_at) VALUES (?, ?, ?, ?, NOW())";
        
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

            // Thực thi
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error adding converted file: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // (Tùy chọn) Hàm lấy file kết quả theo Job ID để hiển thị nút Download
    /*
    public ConvertedFiles getConvertedFileByJobId(int jobId) {
        // Code select * from Converted_files where job_id = ? ...
        return null;
    }
    */
}