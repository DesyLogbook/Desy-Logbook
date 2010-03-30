<!--
This resource should be protected by the servlet engine.
When a visitor was able to access this file, the manager
session-attribute is set an he is forwarded to the manager
application
-->

<%
        session.setAttribute("manager", "manager");
%>

<jsp:forward page="/jsp/manager.jsp" /> 

