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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null) path = "/";

        Clients client = (Clients) request.getSession().getAttribute("client");

        switch (path) {

            case "/upload":
                // Kiểm tra login
                if (client == null) {
                    response.sendRedirect(request.getContextPath() + "/auth/signin");
                    return;
                }
                request.getRequestDispatcher("/pages/upload.jsp").forward(request, response);
                break;

            case "/convert":
                // Kiểm tra login
                if (client == null) {
                    response.sendRedirect(request.getContextPath() + "/auth/signin");
                    return;
                }

                // Lấy danh sách video để hiển thị trong combobox
                List<Videos> videos = videoBO.getVideosByClientId(client.getClient_id());
                request.setAttribute("videos", videos);

                request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
                break;

            case "/history":
                showHistory(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

    // ====== UPLOAD VIDEO ======
    private void handleUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Clients client = (Clients) request.getSession().getAttribute("client");
        if (client == null) {
            request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"No file selected\"}");
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

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"File save error\"}");
            return;
        }

        // Lưu DB
        Videos video = new Videos();
        video.setClient_id(client.getClient_id());
        video.setOriginal_filename(originalFilename);
        video.setStored_path(uploadPath);
        video.setSize(filePart.getSize());
        video.setMime_type(filePart.getContentType());
        video.setDuration_seconds(0);
        video.setCreated_at(new Timestamp(System.currentTimeMillis()));
        video.setStatus(VideoStatus.UPLOADED);

        int video_id = videoBO.addVideo(video);

        response.setContentType("application/json");

        if (video_id != -1) {
            response.getWriter().write("{\"success\":true,\"video_id\":" + video_id + "}");
        } else {
            response.getWriter().write("{\"success\":false}");
        }
    }

    // ====== CONVERT VIDEO ======
    private void handleConvert(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Clients client = (Clients) request.getSession().getAttribute("client");
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin");
            return;
        }

        String videoIdParam = request.getParameter("video_id");
        String targetFormat = request.getParameter("target_format");

        if (videoIdParam == null || targetFormat == null) {
            request.setAttribute("error", "Missing parameters");
            reloadConvertPage(request, response, client);
            return;
        }

        int videoId = Integer.parseInt(videoIdParam);
        Videos video = videoBO.getVideoById(videoId);

        if (video == null) {
            request.setAttribute("error", "Video not found");
            reloadConvertPage(request, response, client);
            return;
        }

        if (video.getStatus() == VideoStatus.UPLOADED) {
            Worker.submitConversionTask(video, targetFormat);
            request.setAttribute("message", "Conversion started. Check history later.");
        } else {
            request.setAttribute("message", "Video is already processing or completed.");
        }

        reloadConvertPage(request, response, client);
    }

    // Load lại convert.jsp kèm danh sách video
    private void reloadConvertPage(HttpServletRequest request, HttpServletResponse response, Clients client)
            throws ServletException, IOException {

        List<Videos> videos = videoBO.getVideosByClientId(client.getClient_id());
        request.setAttribute("videos", videos);

        request.getRequestDispatcher("/pages/convert.jsp").forward(request, response);
    }

    // ====== HISTORY ======
    private void showHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Clients client = (Clients) request.getSession().getAttribute("client");
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin");
            return;
        }

        List<Videos> videos = videoBO.getVideosByClientId(client.getClient_id());
        request.setAttribute("videos", videos);

        request.getRequestDispatcher("/pages/history.jsp").forward(request, response);
    }
}
