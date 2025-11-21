<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Convert Video</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <h2>Convert Video</h2>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>
    <c:if test="${not empty message}">
        <p style="color:green;">${message}</p>
    </c:if>

    <form action="${pageContext.request.contextPath}/client/convert" method="post">
        <label for="video_id">Select video:</label>
        <select name="video_id" id="video_id" required>
            <c:forEach var="video" items="${videos}">
                <option value="${video.video_id}">${video.original_filename}</option>
            </c:forEach>
        </select><br><br>

        <label for="target">Target format:</label>
        <select name="target" id="target" required>
            <option value="mp4">MP4</option>
            <option value="webm">WebM</option>
            <option value="gif">GIF</option>
            <option value="mkv">MKV</option> 
        </select><br><br>
        
        <label>
            <input type="checkbox" name="fast_mode" value="yes"> Fast Mode (Smart Copy)
        </label><br><br>

        <button type="submit">Convert</button>
    </form>

    <br>
    <a href="${pageContext.request.contextPath}/client/history">View Conversion History</a>
</body>
</html>