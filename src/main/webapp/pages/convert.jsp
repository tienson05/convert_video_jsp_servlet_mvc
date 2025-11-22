<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.List, bean.Videos"%>
<!DOCTYPE html>
<html>
<head>
    <title>Convert</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">

    <h2>Convert Video</h2>

    <% String err = (String)request.getAttribute("error"); %>
    <% String msg = (String)request.getAttribute("message"); %>

    <% if(err != null){ %><div class="alert alert-danger"><%=err%></div><% } %>
    <% if(msg != null){ %><div class="alert alert-success"><%=msg%></div><% } %>

    <p style="margin-bottom: 12px;">
        After submitting a convert request, you can check status in <b>My Jobs</b>.
    </p>

    <%
        List<Videos> list = (List<Videos>) request.getAttribute("videos");
    %>

    <form action="<%=request.getContextPath()%>/client/convert" method="post">

        <label>Select Video</label>
        <select name="video_id" required>
            <% if(list != null){ for(Videos v : list){ %>
                <option value="<%=v.getVideo_id()%>">
                    [ID <%=v.getVideo_id()%>] <%=v.getOriginal_filename()%>
                </option>
            <% }} %>
        </select>

        <label>Target Format</label>
        <select name="target_format">
            <option value="mp4">MP4</option>
            <option value="avi">AVI</option>
            <option value="mkv">MKV</option>
            <option value="webm">WEBM</option>
        </select>

        <!-- Tham số thêm: độ phân giải (BE có thể dùng hoặc bỏ qua) -->
        <label>Resolution (optional)</label>
        <select name="resolution">
            <option value="">Original</option>
            <option value="480p">480p</option>
            <option value="720p">720p</option>
            <option value="1080p">1080p</option>
        </select>
        
        <label>Mode (optional)</label>
        <select name="mode">
            <option value="normal">Normal</option>
            <option value="fast">Fast</option>
        </select>

        <button type="submit">Start Convert</button>
    </form>

</div>

</body>
</html>