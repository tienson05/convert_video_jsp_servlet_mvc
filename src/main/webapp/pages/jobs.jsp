<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.List, bean.Jobs"%>
<!DOCTYPE html>
<html>
<head>
    <title>My Jobs</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
</head>
<body>

<jsp:include page="navbar.jsp"/>

<div class="container">
    <h2>My Convert Jobs</h2>

    <p style="margin-bottom: 12px;">
        Here you can see all your convert jobs, their status and progress.
    </p>

    <%
        List<Jobs> jobs = (List<Jobs>) request.getAttribute("jobs");
    %>

    <% if (jobs == null || jobs.isEmpty()) { %>
        <p>You don't have any convert jobs yet.</p>
        <p>
            Go to <b>Upload</b> to upload a video, then <b>Convert</b> to create a job.
        </p>
    <% } else { %>

        <table>
            <tr>
                <th>Job ID</th>
                <th>Video ID</th>
                <th>Target Format</th>
                <th>Status</th>
                <th>Progress</th>
                <th>Created</th>
                <th>Updated</th>
                <th>Action</th>
            </tr>

            <% for (Jobs j : jobs) {
                   int percent = j.getProgress();
            %>
            <tr>
                <td><%= j.getJob_id() %></td>
                <td><%= j.getVideo_id() %></td>
                <td><%= j.getTarget_format() %></td>
                <td><%= j.getStatus() %></td>
                <td>
                    <div style="margin-bottom:4px;"><%= percent %>%</div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width:<%= percent %>%"></div>
                    </div>
                </td>
                <td><%= j.getCreated_at() %></td>
                <td><%= j.getUpdated_at() %></td>
                <td>
                    <% if (j.getStatus() == Jobs.JobStatus.COMPLETED) { %>
                        <!-- BE cần implement /client/download?jobId= -->
                        <a href="<%=request.getContextPath()%>/client/download?jobId=<%=j.getJob_id()%>">
                            Download
                        </a>
                    <% } else { %>
                        -
                    <% } %>
                </td>
            </tr>
            <% } %>
        </table>

        <form method="get" action="<%=request.getContextPath()%>/client/jobs" style="margin-top: 12px;">
            <button type="submit">Refresh</button>
        </form>
    <% } %>

</div>

</body>
</html>