package dao;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        String url  = "jdbc:mysql://localhost:3306/video_convert?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "363888";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Kết nối thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
