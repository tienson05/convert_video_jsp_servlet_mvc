package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    // Cấu hình kết nối MySQL (Sửa lại password cho đúng máy bạn)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/convert_video?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private final String PASS = "";

    public Connection getConnection() {
        Connection conn = null;
        try {
            // Nạp driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Mở kết nối
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
    
    // Hàm main để test thử xem kết nối được không
    public static void main(String[] args) {
        ConnectDB db = new ConnectDB();
        if(db.getConnection() != null) {
            System.out.println("Kết nối CSDL thành công!");
        } else {
            System.out.println("Kết nối thất bại!");
        }
    }
}