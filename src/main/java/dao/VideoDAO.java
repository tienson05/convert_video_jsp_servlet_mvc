package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import bean.Videos;
import bean.Videos.VideoStatus;

public class VideoDAO {
	// Connect DB
		private static final String URL = "jdbc:mysql://localhost:3306/video_convert?useSSL=false&serverTimezone=UTC";
	    private static final String USER = "root";
	    private static final String PASSWORD = "363888";

	    private Connection conn;

	    // Constructor khởi tạo kết nối
	    public VideoDAO() {
	    	try {
	    	    Class.forName("com.mysql.cj.jdbc.Driver");
	    	    this.conn = DriverManager.getConnection(URL, USER, PASSWORD);

	    	    if (this.conn != null) {
	    	        System.out.println("Kết nối DB thành công!");
	    	    } else {
	    	        System.out.println("Kết nối DB thất bại: conn null");
	    	    }
	    	} catch (ClassNotFoundException | SQLException e) {
	    	    System.out.println("Lỗi khi kết nối DB");
	    	    e.printStackTrace();
	    	}
	    }
    // Thêm video mới
    public int addVideo(Videos video) {
        String sql = "INSERT INTO videos(client_id, original_filename, stored_path, size, duration_seconds, mime_type, status, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, video.getClient_id());
            ps.setString(2, video.getOriginal_filename());
            ps.setString(3, video.getStored_path());
            ps.setLong(4, video.getSize());
            ps.setDouble(5, video.getDuration_seconds());
            ps.setString(6, video.getMime_type());
            ps.setString(7, video.getStatus().name()); // lưu enum thành string
            ps.setTimestamp(8, video.getCreated_at());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) return -1; // thêm thất bại

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    video.setVideo_id(id);
                    return id; // trả về id vừa thêm
                } else {
                    return -1; // không lấy được key
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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            v.setStatus(VideoStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            v.setStatus(VideoStatus.UPLOADED); // default nếu DB sai
        }

        return v;
    }
}

