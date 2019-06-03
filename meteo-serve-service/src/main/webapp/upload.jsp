<%--
  Created by IntelliJ IDEA.
  User: xiaoyu
  Date: 16/11/29
  Time: 下午1:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>上传测试</title>
</head>
<head>
    <script src="static/js/lib/jquery-1.11.2.min.js"></script>
</head>
<body>
<form method="post" action="tools/fileupload" enctype="multipart/form-data">
    <input type="file" name="file" />
    <input type="submit" />
</form>



</body>
</html>
