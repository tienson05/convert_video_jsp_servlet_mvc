package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import bean.Clients;
import utils.ConnectDB;

public class ClientDAO {
	// Connect DB
//	private static final String URL = "jdbc:mysql://localhost:3306/convert_video?useSSL=false&serverTimezone=UTC";
//    private static final String USER = "root";
//    private static final String PASSWORD = "";
//
//    private Connection conn;
//
//    // Constructor khởi tạo kết nối
//    public ClientDAO() {
//    	try {
//    	    Class.forName("com.mysql.cj.jdbc.Driver");
//    	    this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
//
//    	    if (this.conn != null) {
//    	        System.out.println("Kết nối DB thành công!");
//    	    } else {
//    	        System.out.println("Kết nối DB thất bại: conn null");
//    	    }
//    	} catch (ClassNotFoundException | SQLException e) {
//    	    System.out.println("Lỗi khi kết nối DB");
//    	    e.printStackTrace();
//    	}
//    }
    
	public boolean addClient(Clients client) {
		String sql = "INSERT INTO `clients` (username, password_hash, image) VALUES(?,?,?)";
		try (Connection conn = new ConnectDB().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getUsername());
            ps.setString(2, client.getPassword_hash());
            ps.setString(3, client.getImage());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public Clients getById(int id) {
        String sql = "SELECT * FROM `clients` WHERE id = ?";
        try (Connection conn = new ConnectDB().getConnection();
        		PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public Clients getByName(String username) {
        String sql = "SELECT * FROM `clients` WHERE username = ?";
        try (Connection conn = new ConnectDB().getConnection();
        		PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	// Hàm tiện ích chuyển ResultSet sang User
    private Clients mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("client_id");
        String name = rs.getString("username");
        String password = rs.getString("password_hash");
        String image = rs.getString("image");
        Timestamp created = rs.getTimestamp("create_at");

        return new Clients(id, name, password, image, created);
    }
}
