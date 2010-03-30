package org.desy.logbook.helper;

import org.desy.logbook.controller.*;
import org.desy.logbook.types.ConfValues;

/**
 * Provides methods to reload all the data stored
 * in memory. Can create rooting for the web.xml file.
 * Only provides static methods.
 * @author Johannes Strampe
 */
public class StartupHelper {

    /**
     * clears the data stored in all controllers and
     * reloads them afterwards
     */
    public static synchronized void restartAllControllers()
    {
        ConfValuesController.getInstance().clear();
        FrontController.getInstance().clear();
        SchedulerController.getInstance().clear();
        DataController.getInstance().clear();
        TreeController.getInstance().clear();

        ConfValuesController.getInstance().readAllConfs();
        FrontController.getInstance().init();
        DataController.getInstance().getAllLogbooks();
        TreeController.getInstance().init();
    }

    /**
     * searches all logbooks in the logbook path and checks
     * if their routing in the web.xml is correct. All missing
     * routes will be automatically written to the web.xml file.
     */
    public static synchronized void updateWebXML()
    {
        ConfValues cv = ConfValuesController.getInstance().getGeneralConf();
        if (cv!=null)
        {
            String webXMLPath = cv.getContext_path()+"/WEB-INF/web.xml";
            
            // read the web.xml
            String content = IOHelper.readFile(webXMLPath);

            //create a backup
            IOHelper.writeFile(webXMLPath+".bak", content);

            // remove the closing root tag so data can be added
            content = content.replace("</web-app>", "");
            String logbookNames[] = ConfValuesController.getInstance().getAllConfNames();
            boolean hasChanges = false;
            for (int i = 0; i < logbookNames.length; i++)
            {
                if (!content.contains("<url-pattern>/"+logbookNames[i]+"</url-pattern>"))
                {
                    content += "\n";
                    content += "<!-- this mapping was added automatically -->";
                    content += "\n";
                    content += "<servlet-mapping>";
                    content += "\n";
                    content += "    <servlet-name>FrontController</servlet-name>";
                    content += "\n";
                    content += "    <url-pattern>/"+logbookNames[i]+"</url-pattern>";
                    content += "\n";
                    content += "</servlet-mapping>";
                    content += "\n";
                    content += "\n";
                    hasChanges = true;
                }
            }

            // close the root tag again
            content += "</web-app>";

            // only write file if changes took place
            if (hasChanges)
            {
                IOHelper.writeFile(webXMLPath, content);
            }
        }
    }
}
