/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desy.logbook.viewServlets;

import org.desy.logbook.controller.ConfValuesController;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.desy.logbook.types.LogbookEntry;


/**
 *
 * @author Johannes Strampe
 */
public class NewEntryServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            VelocityEngine ve = getVelocityEngine();
            StringWriter writer = new StringWriter();
            String template = "templates/newEntryForm.vm";
            VelocityContext vc = new VelocityContext();
            
            vc.put("sendAction", "NewEntryServlet");

            Date now = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//            vc.put("date", sdf.format(now));
//            sdf = new SimpleDateFormat("HH:mm:ss");
//            vc.put("time", sdf.format(now));


            ve.mergeTemplate(template, "UTF-8", vc, writer);
            response.getWriter().print(writer.toString());
        } catch (Exception e) { response.getWriter().println(e.toString()); }
    } 

    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        //saveEntry(request);
        PrintWriter out = response.getWriter();
        out.print("Save war ");
        out.print(saveEntry(request));

//        Enumeration e = request.getParameterNames();
//        while(e.hasMoreElements())
//        {
//            Object o = e.nextElement();
//            out.println("name: "+o.toString());
//            out.println("val: "+request.getParameter(o.toString())+"\n");
//        }
    }

    private VelocityEngine getVelocityEngine() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty( VelocityEngine.FILE_RESOURCE_LOADER_PATH, getServletContext().getRealPath(""));
        ve.init(p);
        return ve;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private boolean saveEntry(HttpServletRequest request) {
        String logname;
        String selectedFile;
        try{
            logname = request.getSession(false).getAttribute("logName").toString();
            selectedFile = request.getSession(false).getAttribute("selectedFile").toString();
        }
        catch (NullPointerException ex)
        {
            return false;
        }
        String filepath = ConfValuesController.getInstance().getConf(logname).getDatapath() + "/" + logname;
        System.out.println(filepath);
        filepath += selectedFile;

        LogbookEntry logbookEntry = new LogbookEntry();

        String fileName = getDateAsIsoString(new Date())+".xml";

        logbookEntry.setFilename(filepath+"/"+fileName);
        System.out.println(logbookEntry.getFilename());
        logbookEntry.setAuthor(request.getParameter("author"));
        logbookEntry.setTitle(request.getParameter("title"));
        logbookEntry.setText(request.getParameter("text"));
        return logbookEntry.saveFile();
    }

    private String getDateAsIsoString(Date date)
    {
        SimpleDateFormat dateSDF = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeSDF = new SimpleDateFormat("HH_mm_ss");

        return dateSDF.format(date)+"T"+timeSDF.format(date);
    }

}
