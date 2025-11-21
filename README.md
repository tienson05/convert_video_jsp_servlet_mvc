📌 VIDEO UPLOAD & CONVERT PROJECT  
Dự án Java Web (Servlet/JSP + FFmpeg + MySQL)

✅ YÊU CẦU MÔI TRƯỜNG
• Java JDK 8+
• Eclipse (Enterprise Java)
• Apache Tomcat 9/10
• MySQL (XAMPP hoặc riêng)

✅ CÁCH CHẠY

1️⃣ Clone dự án
git clone https://github.com/username/YourProject.git

2️⃣ Tạo database
• Tạo DB tên: video_convert
• Import file schema.sql (tạo bảng clients, videos, jobs, converted_files)

3️⃣ Cấu hình DB (nếu cần)
URL: jdbc:mysql://localhost:3306/video_convert?useSSL=false&serverTimezone=UTC
User: root
Pass: "" (để trống nếu XAMPP mặc định)

4️⃣ Tạo thư mục lưu file (ổ D:)
D:\data\uploads\images\
D:\data\uploads\videos\
D:\data\converted\
D:\data\defaults\default_avatar.png

5️⃣ Nếu lỗi MySQL driver
→ Kiểm tra mysql-connector-j-8.x.jar vào
WEB-INF\lib\ chưa

6️⃣ Chạy
Eclipse → Run As → Run on Server → Tomcat

7️⃣ Link chính (sau khi chạy)
http://localhost:8080/TenProject/

🔥 CHỨC NĂNG CHÍNH
/user/signin      → Đăng nhập
/user/signup      → Đăng ký
/client/upload    → Upload video
/client/convert   → Chọn chất lượng & convert (dùng FFmpeg)
/client/history   → Xem lịch sử + tải video đã convert

Chạy xong mà bị lỗi gì thì cứ nhắn mình nhé! 🚀
