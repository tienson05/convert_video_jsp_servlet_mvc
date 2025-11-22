<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Sign In</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">
    <h2>Sign In</h2>

    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
        <div class="alert alert-danger"><%=error%></div>
    <% } %>

    <form action="<%=request.getContextPath()%>/auth/signin" method="post">
        <label>Username</label>
        <input name="username" required>

        <label>Password</label>
        <input type="password" name="password" required>

        <button type="submit">Sign In</button>
    </form>
</div>

</body>
</html>