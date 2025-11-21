package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

import bean.Clients;
import bean.Videos;
import bean.Videos.VideoStatus;
import bo.VideoBO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import task.Worker;

@WebServlet("/client/*")
@MultipartConfig
public class ClientController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final String UPLOAD_DIR = "D:/data/uploads/";

    private VideoBO videoBO = new VideoBO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "/";

        switch (path) {
            case "/upload":
                request.getRequestDispatcher("/pages/upload.jsp").forward(request, response);
                break;
            case "/convert":
                request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
                break;
            case "/history":
                showHistory(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "/";

        switch (path) {
            case "/upload":
                handleUpload(request, response);
                break;
            case "/convert":
                handleConvert(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Upload video
    private void handleUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Clients client = (Clients) request.getSession().getAttribute("client");
        if (client == null) {
        	request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "No file selected");
            request.getRequestDispatcher("/pages/upload.jsp").forward(request, response);
            return;
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String originalFilename = filePart.getSubmittedFileName();
        String newFilename = System.currentTimeMillis() + "_" + originalFilename;
        String uploadPath = uploadDir.getAbsolutePath() + "/" + newFilename;

        try (InputStream input = filePart.getInputStream();
             FileOutputStream output = new FileOutputStream(uploadPath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch(IOException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error saving file on server.");
            return;
        }

        // Lưu vào DB
        Videos video = new Videos();
        video.setClient_id(client.getClient_id());
        video.setOriginal_filename(originalFilename);
        video.setStored_path(uploadPath);
        video.setSize(filePart.getSize());
        video.setMime_type(filePart.getContentType());
        video.setDuration_seconds(0); // Có thể cập nhật sau khi convert
        video.setCreated_at(new Timestamp(System.currentTimeMillis()));
        video.setStatus(VideoStatus.UPLOADED);

        int video_id = videoBO.addVideo(video);

        if(video_id != -1) {
            response.setStatus(HttpServletResponse.SC_OK);
            // trả về video_id dưới dạng JSON
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true,\"video_id\":" + video_id + "}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false}");
        }
       
    }
    
    // Convert Video
    private void handleConvert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String videoIdParam = request.getParameter("video_id");
        String targetFormat  = request.getParameter("target_format");
        if(videoIdParam == null){
            request.setAttribute("error", "No video selected for convert!");
            request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
            return;
        }

        int videoId = Integer.parseInt(videoIdParam);
        Videos video = videoBO.getVideoById(videoId);
        if(video == null){
            request.setAttribute("error", "Video not found!");
            request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
            return;
        }

        // Chỉ submit convert nếu trạng thái UPLOADED
        if(video.getStatus() == VideoStatus.UPLOADED){
//        	Worker.submitConversionTask(video, targetFormat, mode);
            request.setAttribute("message", "Convert task submitted. You can check status later.");
        } else {
            request.setAttribute("message", "Video is already processing or completed.");
        }

        request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
    }

    // Xem lịch sử
    private void showHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Clients client = (Clients) request.getSession().getAttribute("client");
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/user/signin");
            return;
        }

        List<Videos> videos = videoBO.getVideosByClientId(client.getClient_id());
        request.setAttribute("videos", videos);
        request.getRequestDispatcher("/pages/history.jsp").forward(request, response);
    }
}
