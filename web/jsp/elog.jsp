<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="org.desy.logbook.types.ConfLinkValues;"%>

<!-- when the session starts, a new session-bean is created -->
<jsp:useBean id="logBean" scope="session" class="org.desy.logbook.types.ElogBean"/>

 <%
    // the attribute is set by the front-controller
    Object logName = session.getAttribute("logName");

    // the username is shown with the logout button
    String userName = request.getRemoteUser();

    // when no logname was set by the front-controller
    // the not-available page is shown. Happens when
    // elog.jsp is called directly.
    if (logName==null)
    {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/NotAvailable");
        dispatcher.forward(request, response);
    }
    else
    {
        // check for showDir-variable. The content is a folder
        // that will be shown as start-content. Is used
        // to address a specific content directly
        Object showDir = request.getParameter("showDir");
        if (showDir!=null && !showDir.equals(""))
        {
            logBean.setShowDir((String)showDir);
            Object showPos = request.getParameter("showPos");
            if (showPos!=null && !showPos.equals(""))
                logBean.setShowPos((String)showPos);
        }

        // sets the logbook-name. This initializes the
        // bean
        logBean.setLogName(logName.toString());

        // secure-flag is set when called over https:
        logBean.setIsSecure(request.isSecure());
    }
%>


<html>

    <head>
        <title><jsp:getProperty name="logBean" property="title" /></title>
        <%= makeImports(logBean.getCss(), logBean.getJs()) %>
    </head>

    <body onload="resizeAll();init(<jsp:getProperty name="logBean" property="initParams" />)">

        <iframe scrolling="auto"
                frameborder="1"
                id="contentFrame"
                src=""
                name="contentFrame">
        </iframe>

        <div id="leftElem">
            <div id="DivRoot"></div>
            <%= makeHomeLink(logBean.getActualDir()) %>
            <%= makeDropBox(logBean.getDropBoxItems()) %>
            <%= makeLinks(logBean.getLinks()) %>
            <%= makeLogoutLink(userName) %>
        </div>

        <div id="topElem">
            <div id="logo">
            <img src="<jsp:getProperty name="logBean" property="logo" />" alt="logbook logo"/>
            </div>
        </div>

    </body>

</html>


<%!

/*
 * creates a select-box with the passed links
 */
public String makeDropBox(ConfLinkValues[] items)
{
    String result = "";
    if (items.length>0)
    {
        result="<form name=\"logbooks\">" +
                "<select name=\"lbselect\" " +
                "class=\"dropbox\" " +
                "onChange=\"parent.location.href=document.logbooks.lbselect.value;document.logbooks.lbselect.selectedIndex= 0;\">";
        for (int i=0; i<items.length; i++)
        {
            String item = "<option value=\""+items[i].getTarget()+"\">";
            item += items[i].getLabel();
            item += "</option>";
            result += item;
        }
        result += "</select></form>";
    }
    return result;

}

/*
 * converts the passed links to html-links
 */
public String makeLinks(ConfLinkValues[] links)
{
    String result="";
    for (int i=0; i<links.length; i++)
    {
        String link = "<a href=\""+links[i].getTarget()+"\"";
        link += " target=\"contentFrame\"";
        link += " class=\"leftMenuLink\"";
        link += ">"+links[i].getLabel()+"</a>";
        result += link+"\n";
    }
    return result;
}

/*
 * takes the css and javascript sources an creates
 * the import statements
 */
public String makeImports(String[] css, String[] js)
{
    String result = "";
    for (int i=0; i<js.length; i++)
    {
        result += "<script type=\"text/javascript\" src=\""+js[i]+"\"></script>\n";
    }
    for (int i=0; i<css.length; i++)
    {
        result += "<link rel=\"stylesheet\" type=\"text/css\" href=\""+css[i]+"\"/>\n";
    }
    return result;
}

/*
 * creates the link that resets the tree to the init-value
 */
public String makeHomeLink(String actualDir)
{
    if (actualDir==null || actualDir.equals("")) return "";
    //<a title="Show actual logbook page" href="javascript:showActualAddress('/2009/10');">View Current</a>
    String result = "<a title=\"Show actual logbook page\" ";
    result += " class=\"viewCurrentLink\"";
    result += "href=\"javascript:showActualAddress('"+actualDir+"')\">";
    result += "View Current";
    result += "</a>";
    return result;
 }

/*
 * shows the username and the logout-button if a
 * user has logged in.
 */
 public String makeLogoutLink(String userName)
 {
     if (userName!=null && !userName.equals(""))
     {
         return "<a class=\"logoutLink\" " +
                "href=\"jsp/logout.jsp\">" +
                "logout " + userName +
                "</a>";
     }
     return "";
 }
%>
