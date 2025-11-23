<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Upload</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">
    <h2>Upload Video</h2>

    <div id="msg"></div>

    <form id="uploadForm" enctype="multipart/form-data">
        <label>Select File</label>
        <input type="file" name="file" required>

        <button type="submit">Upload</button>
    </form>
</div>

<script>
document.getElementById("uploadForm").onsubmit = function(e){
    e.preventDefault();
    let msg = document.getElementById("msg");
    msg.innerHTML = "<div class='alert alert-success'>Uploading...</div>";

    const fd = new FormData(this);

    fetch("<%=request.getContextPath()%>/client/upload", {
        method: "POST",
        body: fd
    })
    .then(r => r.json())
    .then(data => {
        if(data.success){
            msg.innerHTML = "<div class='alert alert-success'>Upload OK — video_id = "+data.video_id+"</div>";
        } else {
            msg.innerHTML = "<div class='alert alert-danger'>Upload failed</div>";
        }
    });
}
</script>

</body>
</html>