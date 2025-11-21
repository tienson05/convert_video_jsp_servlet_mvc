1) Kiến trúc hệ thống (Architecture)
graph LR
    User[Người Dùng] -- 1. Upload (HTTP) --> Web[Web Server Tomcat :8080]
    Web -- 2. Lưu File & DB --> HDD[(Ổ Cứng & MySQL)]
    Web -- 3. Gửi lệnh (TCP) --> Proc[Processing Server :9000]
    Proc -- 4. Đọc File --> HDD
    Proc -- 5. Xử lý (FFmpeg) --> Proc
    Proc -- 6. Báo tiến độ (UDP) --> Web
    Proc -- 7. Trả kết quả (TCP) --> Web
*Các thành phần:
  Web Server (MVCVideoConverter): Chạy trên Tomcat 10.
  Processing Server (ProcessingServer): Chạy Java Console (như file .exe).
  Database (MySQL): Lưu thông tin User, Video, Job.
  FFmpeg: Công cụ dòng lệnh để xử lý video thực tế.
2) Quy trình hoạt động (Workflow)
  *Khi một người dùng muốn convert video, hệ thống chạy như sau:
  *Upload: User upload file test.mp4 lên Web.
  *Lưu trữ: Web Server lưu file vào thư mục D:/data/uploads/ và ghi vào bảng videos (Trạng thái: UPLOADED).
  *Tạo Job: User bấm nút "Convert", Web tạo một bản ghi trong bảng jobs (Trạng thái: PENDING).
  *Gửi Lệnh (TCP): Web Server mở kết nối đến Processing Server (Port 9000) và gửi lệnh: "Ê, convert cái file ở D:/data/uploads/test.mp4 sang định dạng MKV đi!"
  *Xử lý (Processing):
     -Processing Server nhận lệnh, đưa vào hàng đợi (Thread Pool).
     -Gọi FFmpeg chạy ngầm.
     -Real-time: Cứ vài giây, nó bắn tin nhắn UDP về Web: "Xong 10%... Xong 50%...".
  *Hoàn tất:
     -FFmpeg chạy xong, tạo ra file mới ở D:/CODING/.../VideoOutput/.
     -Processing Server báo TCP về Web: "Xong rồi nhé, file mới nằm ở đây nè".
     -Web cập nhật Database thành COMPLETED và hiện link tải cho User.
3) Giao thức giao tiếp (Protocol)
- Đây là "ngôn ngữ chung" để 2 server hiểu nhau. Cấm sửa đổi nếu không thống nhất cả 2 bên.
A. Web gửi lệnh sang Processing (TCP - Port 9000)
  Format: CONVERT | JobID | Đường_Dẫn_File_Gốc | Định_Dạng_Đích | Chế_Độ

Ví dụ: CONVERT|88|D:/data/uploads/abc.mp4|avi|NORMAL

Ví dụ Fast Mode: CONVERT|89|D:/data/uploads/xyz.mkv|mp4|FAST

B. Processing báo cáo tiến độ (UDP - Port 9001)
  Format: PROGRESS | JobID | Trạng_Thái | Phần_Trăm

Ví dụ: PROGRESS|88|PROCESSING|45

C. Processing trả kết quả (TCP Reply)
  Format: OK | Success | Đường_Dẫn_File_Mới

Ví dụ: OK|Success|D:/CODING/.../VideoOutput/abc_optimized.avi

3 minutes ago

Enhance README with testing instructions
Nếu lỗi: ERROR|Lý do lỗi

4) Test chạy thử: vào ProcessingServer.java rồi chạy, lúc này thì ProcessingServer sẽ ở trạng thái lắng nghe (listening)
   Rồi sau đó ấn chay TestWorker.java thì lúc này sẽ kết nối tới Processing Server
   rồi gửi chuỗi lệnh: LỆNH | ID_CÔNG_VIỆC | ĐƯỜNG_DẪN_FILE_GỐC | ĐỊNH_DẠNG_ĐÍCH | CHẾ_ĐỘ
              tức là   command | Job ID | Input Path | Target Format | Mode
              Ví dụ:   CONVERT|1|D:\data\uploads\test.mp4|mkv|FAST
   Processing Server sẽ dùng chuỗi lệnh này để làm công việc convert rồi sẽ báo tiến độ và trả kết quả về cho Web server:
   Đây là kết quả khi test:
   === BẮT ĐẦU TEST WORKER ===
>> Đang gửi yêu cầu Convert cho Video ID: 1
>> Input: D:\data\uploads\test.mp4
>> Target: mkv | Mode: FAST
>> Đang chờ Processing Server xử lý... (Giữ kết nối 15s)
[Worker] Created Job ID: 1. Mode: FAST
[Worker] Sent TCP: CONVERT|1|D:\data\uploads\test.mp4|mkv|FAST
...[Worker] Received TCP: OK|Success|D:\CODING\HK1-25-26\LTM\VideoOutput\test_fast.mkv
[Worker] Job 1 completed successfully!
............
=== KẾT THÚC TEST ===