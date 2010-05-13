/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desy.logbook.servlets;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.desy.logbook.helper.ConfFileHelper;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.VelocityServlet;


/**
 * Used to install/configure the main conf.xml file
 * @author Johannes Strampe
 */
public class Install extends VelocityServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addVelocityVariable("sendAction", "Install");
        render("templates/install.vm", response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setGlobalContextPath(getServletContext().getRealPath(""));
        if (saveConfFile(request))
        {
            redirect(response, "/newElog");
        }
        else
        {
            addVelocityVariable("sendAction", "Install");
            render("templates/install.vm", response);
        }
    }

    /**
     * reads post parameters and writes them to the conf file
     * @param request
     * @return
     */
    private boolean saveConfFile(HttpServletRequest request) {
        String logbook_path = request.getParameter("logbook_path");
        String host_data = request.getParameter("host_data");
        String context = request.getParameter("context");
        String datapath = request.getParameter("datapath");

        if (isBlank(logbook_path) || isBlank(host_data)  || isBlank(context) || isBlank(datapath))
            return false;

        File conf = new File(System.getProperty(Settings.GLOBAL_CONTEXT_PATH)+"/conf/conf.xml");
        ConfFileHelper cfh = new ConfFileHelper(conf);

        cfh.updateOrCreateTextElement("datapath", datapath);
        cfh.updateOrCreateTextElement("context", context);
        cfh.updateOrCreateTextElement("host_data", host_data);
        cfh.updateOrCreateTextElement("logbook_path", logbook_path);
        return cfh.save();
    }

    /**
     * Sets the contextPath as a System global
     * variable which is vital for the loading of the conf
     * files in the ConfValuesController.
     * @param contextPath
     */
    private void setGlobalContextPath(String contextPath)
    {
        if (System.getProperty(Settings.GLOBAL_CONTEXT_PATH)==null)
            System.setProperty(Settings.GLOBAL_CONTEXT_PATH, contextPath);
    }

    /**
     * return true if s is null or empty string
     * @param s
     * @return
     */
    private Boolean isBlank(String s)
    {
        return (s==null || s.equals(""));
    }

}
