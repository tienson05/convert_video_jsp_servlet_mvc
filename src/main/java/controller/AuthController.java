package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bean.Clients;
import bo.ClientBO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.HashPW;

@WebServlet("/auth/*")
@MultipartConfig
public class AuthController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final String DEFAULT_AVATAR = "D:/data/uploads/defaults/default_avatar.jpg";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo(); // /signin, /signup, /logout
        if (path == null) path = "/";

        switch (path) {
            case "/signin":
                request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
                break;
            case "/signup":
                request.getRequestDispatcher("/pages/signup.jsp").forward(request, response);
                break;
            case "/logout":
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/signin");
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo(); // /signin hoặc /signup
        if (path == null) path = "/";

        ClientBO clientBO = new ClientBO();
        HashPW hash = new HashPW();

        switch (path) {
            case "/signin":
                handleSignin(request, response, clientBO, hash);
                break;
            case "/signup":
                handleSignup(request, response, clientBO, hash);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response, ClientBO clientBO, HashPW hash)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Clients client = clientBO.getByName(username);
        if (client == null) {
            request.setAttribute("error", "User not found!");
            request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
            return;
        }

        String hashed_pw = hash.hashPassword(password);
        System.out.println("hashed: "+ hashed_pw);
        System.out.println("get: " + client.getPassword_hash());
        if (client.getPassword_hash().equals(hashed_pw)) {
            request.getSession().setAttribute("client", client);
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            request.setAttribute("error", "Incorrect password!");
            request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
        }
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response, ClientBO clientBO, HashPW hash)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Clients existingClient = clientBO.getByName(username);
        if (existingClient != null) {
            request.setAttribute("error", "Username already exists!");
            request.getRequestDispatcher("/pages/signup.jsp").forward(request, response);
            return;
        }
        
        // Xử lý ảnh
        Part filePart = request.getPart("image");
        String uploadPath = DEFAULT_AVATAR;

        if (filePart != null && filePart.getSize() > 0) {
            File uploadDir = new File("D:/data/uploads/images/");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String originalFilename = filePart.getSubmittedFileName();
            String newFilename = System.currentTimeMillis() + "_" + originalFilename;

            uploadPath = uploadDir.getAbsolutePath() + "/" + newFilename;

            try (InputStream input = filePart.getInputStream();
                 FileOutputStream output = new FileOutputStream(uploadPath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        }

        // Hash password
        String hashedPassword = hash.hashPassword(password);
        
        // Lưu vào DB
        boolean ok = clientBO.addClient(username, hashedPassword, uploadPath);
		if(ok) {
			// chuyển về trang đăng nhập
			request.getRequestDispatcher("/pages/signin.jsp").forward(request, response);
		} else {
			// chuyển về trang đăng ký
			request.setAttribute("error", "Error saving user to database.");
			request.getRequestDispatcher("/pages/signup.jsp").forward(request, response);
		}
    }
}
