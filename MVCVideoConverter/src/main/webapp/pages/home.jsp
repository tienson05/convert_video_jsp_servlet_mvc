<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Home</title>
    <base href="${pageContext.request.contextPath}/">
    <link rel="stylesheet" type="text/css" href="css/home.css">
</head>
<body>
    <h1>Welcome to MyApp</h1>
    <p>
        <a href="${pageContext.request.contextPath}/auth/signin">Sign In</a> | 
        <a href="${pageContext.request.contextPath}/auth/signup">Sign Up</a>
    </p>
</body>
</html>
