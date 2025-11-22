<%@ page import="bean.Clients" %>
<%
    Clients user = (Clients) session.getAttribute("client");
    String cPath = request.getContextPath();
%>

<div class="navbar">
    <div class="navbar-title">Video Converter</div>
    <div>
        <a href="<%=cPath%>/home">Home</a>

        <% if (user != null) { %>
            <a href="<%=cPath%>/client/upload">Upload</a>
            <a href="<%=cPath%>/client/convert">Convert</a>
            <a href="<%=cPath%>/client/history">My Videos</a>
            <!-- mới thêm: My Jobs -->
            <a href="<%=cPath%>/client/jobs">My Jobs</a>
            <a href="<%=cPath%>/auth/logout">Logout</a>
        <% } else { %>
            <a href="<%=cPath%>/auth/signin">Sign In</a>
            <a href="<%=cPath%>/auth/signup">Sign Up</a>
        <% } %>
    </div>
</div>
