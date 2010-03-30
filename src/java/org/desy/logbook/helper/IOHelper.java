package org.desy.logbook.helper;
import org.desy.logbook.controller.LogController;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class to read and write files to disk.
 * only static methods are offered
 * @author Johannes Strampe
 */
public class IOHelper {
    
    /**
     * Renames the "from" file to the "to" file
     * if creation or overwriting does not succeed
     * no exception is launched
     * @param from source filepath
     * @param to target filepath
     */
    public static void moveFile(String from, String to)
    {
        try
        {
            File fFrom = new File(from);
            File fTo = new File(to);
            fFrom.renameTo(fTo);
        }
        catch (Exception ex){LogController.getInstance().log(ex.toString());}
    }
    
    /**
     * saves the 'content' to the file with the given 'path'
     * will overwrite a file if possible
     * @param path target filepath
     * @param content content of the target file
     * @return true if writing was successful
     */
    public static boolean writeFile(String path, String content)
    {   
        FileWriter fw = null;
        File f = new File(path);
        boolean writingSuccessful = true;
        try
        {
            
            if (!f.exists()) {
                (new File(f.getParent())).mkdirs();
                System.out.println("create war "+f.createNewFile());
            }
            
            fw = new FileWriter(f);
            fw.write(content);                       
        }
        catch(IOException ex)
        {            
            // could not write file
            writingSuccessful = false;
            LogController.getInstance().log(ex.toString());
        }
        finally 
        {
            if(fw!=null)
                try {fw.close();} catch (IOException ex) {LogController.getInstance().log(ex.toString());}
            return writingSuccessful;
        }
    }
    
    /**
     * reads file from 'path' and returns its content
     * on error an empty string is returned
     * @param path of the file
     * @return content of the file or "" on error
     */
    public static String readFile(String path)
    {
        String result = "";
        FileReader fr = null;        
        try
        {
            fr = new FileReader(path);
            char[] buf = new char[10000];
            int readResult = fr.read(buf);
            while(readResult!=-1)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(buf);
                result+=sb.substring(0,readResult);
                readResult = fr.read(buf);
            }
        }
        catch(IOException ex)
        {
            LogController.getInstance().log(ex.toString());
            // could not read 
        }
        finally
        {
            if(fr!=null) 
                try {fr.close();} catch (IOException ex) {LogController.getInstance().log(ex.toString());}
        }
        return result;
    }

    /**
     * reads content of file
     * on error an empty string is returned
     * @param file file to read
     * @return content of the file or "" on error
     */
    public static String readFile(File file)
    {
        String result = "";
        FileReader fr = null;
        try
        {
            fr = new FileReader(file);
            char[] buf = new char[10000];
            int readResult = fr.read(buf);
            while(readResult!=-1)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(buf);
                result+=sb.substring(0,readResult);
                readResult = fr.read(buf);
            }
        }
        catch(IOException ex)
        {
            LogController.getInstance().log(ex.toString());
            // could not read
        }
        finally
        {
            if(fr!=null)
                try {fr.close();} catch (IOException ex) {LogController.getInstance().log(ex.toString());}
        }
        return result;
    }

}//class end
