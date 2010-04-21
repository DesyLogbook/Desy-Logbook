package org.desy.logbook.viewServlets;

import org.desy.logbook.controller.ConfValuesController;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.desy.logbook.types.LogbookEntry;
import org.desy.logbook.types.VelocityServlet;

/**
 *
 * @author Johannes Strampe
 */
public class NewEntryServlet extends VelocityServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        this.addVelocityVariable("sendAction", "NewEntryServlet");
        this.render("templates/newEntryForm.vm", response);
    } 

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try{
            saveEntry(request);
            String logname = request.getSession(false).getAttribute("logName").toString();
            redirect(response, "/newElog/"+logname);
        }
        catch (NullPointerException ex)
        {
            addVelocityVariable("title", "Error");
            addVelocityVariable("message", "Could not redirect properly "+ex.toString());
            render("templates/message.vm", response);
        }   
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
        filepath += selectedFile;

        LogbookEntry logbookEntry = new LogbookEntry();

        Date now = new Date();
        String fileName = getDateAsIsoString(now)+".xml";

        logbookEntry.setFilename(filepath+"/"+fileName);
        logbookEntry.setAuthor(request.getParameter("author"));
        logbookEntry.setTitle(request.getParameter("title"));
        logbookEntry.setText(request.getParameter("text"));
        logbookEntry.setDate(now);
        logbookEntry.setTime(now);
        return logbookEntry.saveFile();
    }

    private String getDateAsIsoString(Date date)
    {
        SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSDF = new SimpleDateFormat("HH:mm:ss");
        return dateSDF.format(date)+"T"+timeSDF.format(date);
    }
}