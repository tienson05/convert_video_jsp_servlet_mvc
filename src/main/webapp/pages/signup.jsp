<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sign Up</title>
</head>
<body>
<h2>Sign Up</h2>

<% String error = (String) request.getAttribute("error"); %>
<% if(error != null) { %>
    <p style="color:red;"><%= error %></p>
<% } %>

<form action="<%= request.getContextPath() %>/auth/signup" method="post" enctype="multipart/form-data">
    <label>Username:</label>
    <input type="text" name="username" required><br><br>

    <label>Password:</label>
    <input type="password" name="password" required><br><br>

    <label>Avatar:</label>
    <input type="file" name="image" accept="image/*"><br><br>

    <button type="submit">Sign Up</button>
</form>

<p>Already have an account? <a href="<%= request.getContextPath() %>/auth/signin">Sign In</a></p>
</body>
</html>
