package org.desy.logbook.types;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;
import javax.servlet.http.HttpServlet;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * @author Johannes Strampe
 */
abstract public class VelocityServlet extends HttpServlet {

    private VelocityContext velocityContext = new VelocityContext();

    @Override
    abstract protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException;

    @Override
    abstract protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException;

    protected void addVelocityVariable(String name, Object content)
    {
        velocityContext.internalPut(name, content);
    }

    protected void render(String template, HttpServletResponse response) throws IOException
    {
        try {
            // prepare template rendering
            StringWriter writer = new StringWriter();
            VelocityEngine ve = getVelocityEngine();

            // render the template
            ve.mergeTemplate(template, "UTF-8", velocityContext, writer);
            response.getWriter().print(writer.toString());
        } catch (Exception e) { response.getWriter().println(e.toString()); }
    }

    private VelocityEngine getVelocityEngine() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty( VelocityEngine.FILE_RESOURCE_LOADER_PATH, getServletContext().getRealPath(""));
        ve.init(p);
        return ve;
    }

    public void redirect(HttpServletResponse response, String url) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    public static final String escapeHTML(String s){
       StringBuffer sb = new StringBuffer();
       int n = s.length();
       for (int i = 0; i < n; i++) {
          char c = s.charAt(i);
          switch (c) {
             case '<': sb.append("&lt;"); break;
             case '>': sb.append("&gt;"); break;
             case '&': sb.append("&amp;"); break;
             case '"': sb.append("&quot;"); break;
             case 'à': sb.append("&agrave;");break;
             case 'À': sb.append("&Agrave;");break;
             case 'â': sb.append("&acirc;");break;
             case 'Â': sb.append("&Acirc;");break;
             case 'ä': sb.append("&auml;");break;
             case 'Ä': sb.append("&Auml;");break;
             case 'å': sb.append("&aring;");break;
             case 'Å': sb.append("&Aring;");break;
             case 'æ': sb.append("&aelig;");break;
             case 'Æ': sb.append("&AElig;");break;
             case 'ç': sb.append("&ccedil;");break;
             case 'Ç': sb.append("&Ccedil;");break;
             case 'é': sb.append("&eacute;");break;
             case 'É': sb.append("&Eacute;");break;
             case 'è': sb.append("&egrave;");break;
             case 'È': sb.append("&Egrave;");break;
             case 'ê': sb.append("&ecirc;");break;
             case 'Ê': sb.append("&Ecirc;");break;
             case 'ë': sb.append("&euml;");break;
             case 'Ë': sb.append("&Euml;");break;
             case 'ï': sb.append("&iuml;");break;
             case 'Ï': sb.append("&Iuml;");break;
             case 'ô': sb.append("&ocirc;");break;
             case 'Ô': sb.append("&Ocirc;");break;
             case 'ö': sb.append("&ouml;");break;
             case 'Ö': sb.append("&Ouml;");break;
             case 'ø': sb.append("&oslash;");break;
             case 'Ø': sb.append("&Oslash;");break;
             case 'ß': sb.append("&szlig;");break;
             case 'ù': sb.append("&ugrave;");break;
             case 'Ù': sb.append("&Ugrave;");break;
             case 'û': sb.append("&ucirc;");break;
             case 'Û': sb.append("&Ucirc;");break;
             case 'ü': sb.append("&uuml;");break;
             case 'Ü': sb.append("&Uuml;");break;
             case '®': sb.append("&reg;");break;
             case '©': sb.append("&copy;");break;
             case '€': sb.append("&euro;"); break;
             // be carefull with this one (non-breaking whitee space)
             case ' ': sb.append("&nbsp;");break;

             default:  sb.append(c); break;
          }
       }
       return sb.toString();
    }

}
