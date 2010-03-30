/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desy.logbook.viewServlets;

import org.desy.logbook.controller.ConfValuesController;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import org.desy.logbook.helper.IOHelper;
import java.util.Properties;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.desy.logbook.types.ConfValues;
import org.xml.sax.InputSource;

/**
 *
 * @author Johannes Strampe
 */
public class ViewServlet extends HttpServlet {
   


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        try {
            //TODO
            String file = request.getParameter("file");
            String name = request.getParameter("name");

            HttpSession s = request.getSession();
            s.setAttribute("selectedFile", file);
            ConfValues conf = ConfValuesController.getInstance().getConf(name);

            String contentFolder = conf.getDatapath()+"/"+name+file;
            String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            content += "<root>"+readAllContentFiles(contentFolder)+"</root>";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(content));

            Document doc = db.parse(is);

            VelocityEngine ve = getVelocityEngine();
            
            String template = "templates/overview.vm";
            VelocityContext vc = new VelocityContext();

            vc.put("content",escapeHTML(content));
            //vc.put("metainfo", doc.getElementsByTagName("metainfo").item(0).getTextContent() );

            StringWriter writer = new StringWriter();
            ve.mergeTemplate(template, "UTF-8", vc, writer);
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

    private String readAllContentFiles(String folderPath)
    {
        String initContent = IOHelper.readFile(folderPath+"/init.xml");
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith(".xml") &&
                        !file.getName().toLowerCase().equals("init.xml") &&
                        file.isFile();
            }
        };
        File list[] = new File(folderPath).listFiles(ff);
        for (int i = 0; i < list.length; i++) {
            initContent += IOHelper.readFile(list[i]);
        }
        return initContent;
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
