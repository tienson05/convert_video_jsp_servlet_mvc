<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.List, bean.Videos"%>
<!DOCTYPE html>
<html>
<head>
    <title>My Videos</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">
    <h2>My Videos</h2>

    <%
        List<Videos> list = (List<Videos>) request.getAttribute("videos");
    %>

    <table>
        <tr>
            <th>ID</th>
            <th>Filename</th>
            <th>Status</th>
            <th>Size</th>
        </tr>
        <% if(list != null){ for(Videos v : list){ %>
        <tr>
            <td><%=v.getVideo_id()%></td>
            <td><%=v.getOriginal_filename()%></td>
            <td><%=v.getStatus()%></td>
            <td><%=v.getSize()%></td>
        </tr>
        <% }} %>
    </table>

</div>

</body>
</html>