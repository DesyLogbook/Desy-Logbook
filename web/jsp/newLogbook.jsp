<%-- 
    Document   : newLogbook
    Created on : Oct 16, 2009, 9:32:32 AM
    Author     : Johannes Strampe
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%
/*
 * any error that arrives will be placed in the ERROR
 * variable. Then the transport cookie is deleted
 */
Cookie cookies[] = request.getCookies();

String ERROR = "";
if (cookies!=null)
{
    for (int i=0; i<cookies.length; i++)
    {
        if (cookies[i].getName().equals("ERROR"))
        {
            ERROR += cookies[i].getValue();
            cookies[i].setValue("");
            response.addCookie(cookies[i]);
        }
    }
}

%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>New Logbook</title>
        <style>
            .error {
                color:red;
                font-size:20px;
                font-family:sans-serif;
                font-weight:bold;
                text-decoration:underline;
                padding:10px;
                margin:10px;
            }
        </style>
    </head>
    <body>
    <div class="error"><%= ERROR %></div>
        <form name="newLogbookForm" action="../NewLogbook" method="POST">
            <p>Name:<br><input name="name" type="text" size="30" maxlength="30"></p>
            <p>Title:<br><input name="title" type="text" size="30" maxlength="30"></p>
            <p>New shift (Y,M,W,D,3 or cron notation):<br><input name="newShift" type="text" size="30" maxlength="30"></p>
            <input type="submit" value="Send ">
        </form>
    </body>
</html>
