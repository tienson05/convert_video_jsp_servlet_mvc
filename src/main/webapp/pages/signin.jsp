<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sign In</title>
</head>
<body>
<h2>Sign In</h2>

<% String error = (String) request.getAttribute("error"); %>
<% if(error != null) { %>
    <p style="color:red;"><%= error %></p>
<% } %>

<form action="<%= request.getContextPath() %>/auth/signin" method="post">
    <label>Username:</label>
    <input type="text" name="username" required><br><br>

    <label>Password:</label>
    <input type="password" name="password" required><br><br>

    <button type="submit">Sign In</button>
</form>

<p>Don't have an account? <a href="<%= request.getContextPath() %>/auth/signup">Sign Up</a></p>
</body>
</html>
