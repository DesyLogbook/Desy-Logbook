<%-- 
    Document   : manager
    Created on : Oct 14, 2009, 10:33:59 AM
    Author     : Johannes Strampe
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%
    
    if (session.getAttribute("manager")==null)
    {
        response.sendRedirect("../NotAvailable");
    }

%>

<html>
<head>
<title>elogbook Manager</title>
<link href="css/manager.css" type="text/css" rel="stylesheet">
<script src="javascript/manager.js" type="text/javascript"></script>
<script src="javascript/prototype.js" type="text/javascript"></script>
</head>
<body onload="init()" bgcolor="#f7f7f7">
<div class="header">elogbook Manager <span id="time"></span></div>
<!--a href="javascript:showDebug();">debug</a-->

 <div id="root">
</div>
</body>
</html>

