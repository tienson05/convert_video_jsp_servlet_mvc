<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Sign Up</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">
    <h2>Create Account</h2>

    <% 
        String error = (String) request.getAttribute("error");
        String msg   = (String) request.getAttribute("message");
    %>

    <% if (error != null) { %>
        <div class="alert alert-danger"><%=error%></div>
    <% } %>

    <% if (msg != null) { %>
        <div class="alert alert-success"><%=msg%></div>
    <% } %>

    <!-- CHÚ Ý: multipart/form-data để BE đọc được Part -->
    <form action="<%=request.getContextPath()%>/auth/signup"
          method="post"
          enctype="multipart/form-data">

        <label>Username</label>
        <input name="username" required>

        <label>Password</label>
        <input type="password" name="password" required>

        <label>Confirm Password</label>
        <input type="password" name="confirm" required>

        <!-- Ô chọn ảnh avatar -->
        <!-- name="image" PHẢI trùng với AuthController: request.getPart("image") -->
        <label>Avatar (optional)</label>
        <input type="file" name="image" accept="image/*">

        <button type="submit">Sign Up</button>
    </form>
</div>

</body>
</html>