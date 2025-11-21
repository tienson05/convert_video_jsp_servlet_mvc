package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import bean.Videos;
import bean.Videos.VideoStatus;
import utils.ConnectDB;

public class VideoDAO {

    public VideoDAO() {
    }

    // Thêm video mới
    public int addVideo(Videos video) {
        String sql = "INSERT INTO videos(client_id, original_filename, stored_path, size, duration_seconds, mime_type, status, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())"; // Dùng NOW() của SQL cho chính xác thời gian server DB
        
        // Mở kết nối và tự động đóng sau khi xong (try-with-resources)
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, video.getClient_id());
            ps.setString(2, video.getOriginal_filename());
            ps.setString(3, video.getStored_path());
            ps.setLong(4, video.getSize());
            ps.setDouble(5, video.getDuration_seconds());
            ps.setString(6, video.getMime_type());
            
            // Chuyển Enum thành String
            ps.setString(7, video.getStatus().name()); 
            
            // created_at dùng NOW() trong SQL nên không cần set ở đây, 
            // nhưng nếu bạn muốn set thủ công thì giữ nguyên dòng dưới:
            // ps.setTimestamp(8, video.getCreated_at()); 

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    video.setVideo_id(id);
                    return id;
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Lấy video theo ID
    public Videos getById(int videoId) {
        String sql = "SELECT * FROM videos WHERE video_id = ?";
        
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, videoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractVideo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy danh sách video theo client_id
    public List<Videos> getByClientId(int clientId) {
        List<Videos> list = new ArrayList<>();
        String sql = "SELECT * FROM videos WHERE client_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractVideo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Cập nhật trạng thái video
    public boolean updateStatus(int videoId, VideoStatus status) {
        String sql = "UPDATE videos SET status = ? WHERE video_id = ?";
        
        try (Connection conn = new ConnectDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, status.name());
            ps.setInt(2, videoId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm helper trích xuất ResultSet -> Videos
    private Videos extractVideo(ResultSet rs) throws SQLException {
        Videos v = new Videos();
        v.setVideo_id(rs.getInt("video_id"));
        v.setClient_id(rs.getInt("client_id"));
        v.setOriginal_filename(rs.getString("original_filename"));
        v.setStored_path(rs.getString("stored_path"));
        v.setSize(rs.getLong("size"));
        v.setDuration_seconds(rs.getDouble("duration_seconds"));
        v.setMime_type(rs.getString("mime_type"));
        v.setCreated_at(rs.getTimestamp("created_at"));

        String statusStr = rs.getString("status");
        try {
            if(statusStr != null) {
                v.setStatus(VideoStatus.valueOf(statusStr));
            }
        } catch (IllegalArgumentException e) {
            v.setStatus(VideoStatus.UPLOADED); // Default nếu DB sai lệch
        }

        return v;
    }
}