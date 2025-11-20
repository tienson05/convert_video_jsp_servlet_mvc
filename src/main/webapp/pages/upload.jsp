<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload Video</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <h2>Upload Video</h2>

    <!-- Hiển thị lỗi hoặc thông báo -->
    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>
    <c:if test="${not empty message}">
        <p style="color:green;">${message}</p>
    </c:if>

    <form action="${pageContext.request.contextPath}/client/upload" method="post" enctype="multipart/form-data">
        <label for="file">Select video:</label>
        <input type="file" name="file" id="file" accept="video/*" required><br><br>

        <button type="submit">Upload</button>
    </form>

    <br>
    <a href="${pageContext.request.contextPath}/client/history">View Upload History</a>
</body>
</html>
