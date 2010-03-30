/*
 * makeTreeXML.java
 *
 * Created on May 13, 2008, 11:12 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
 
package org.desy.logbook.javascriptTree;
 
import org.desy.logbook.controller.LogController;
import java.io.*;
 
/**
 *
 * @author Johannes Strampe
 */
public class MakeTreeXML extends Thread{
    
    private String sourcePath       = "/var/www/TESTelog/data";
    private String targetPath       = "/var/www/TESTelog/jsp";
    private String filename         = "treeData.xml";
    private String tempFilename     = "treeDataTemp.xml";
    private String backupFilename   = "treeData.xml.bak";
    private String sortArgs[];
    
    private long timeStart;
    private long timeRead;
    private long timeWrite;
    private long timeDelta;
    
    private String target;
    private String tempTarget;
    private String backupTarget;
    private int countFolder = 0;
    private int countFile = 0;
    private int countTotal = 0;    
    private String resultXML = "";    
    
    private FileFilter filter = null;
    
    //testparas
    //  /web/ttf2svr3/htdocs/TTFelog/data /home/jstrampe/Desktop
    // /var/www/TESTelog/jsp/data /var/www/TESTelog/jsp -d -n comments -de
           
    /**
     * processes the arguments and starts a new thread
     * or
     * prints help text
     * @param args see documentation for all
     * possible arguments
     */
    public MakeTreeXML(String[] args)
    {
        //String[] args2 = {"/web/ttf2svr3/htdocs/TTFelog/data", "/home/jstrampe/Desktop", "-de"};
        //String[] args3 = {"/var/www/TESTelog/data", "/var/www/TESTelog/jsp", "-d", "-n", "comments", "-de"};
        //String[] args4 = {"/web/ttf2svr3/htdocs/TTFelog/data", "/home/jstrampe/Desktop", "-d", "-n", "-de"};
        this.setPriority(Thread.MIN_PRIORITY);
        
        if (!processArgs(args))
        {
            printHelp();
        }
        else
        {
            start();
        }
    }
    
    /**
     * thread starts here
     */
    public void run() {
        this.setPriority(Thread.MIN_PRIORITY);
        filter = new FileFilter() {
            public boolean accept(File f) 
            {
                return f.isDirectory();
            }
        };
        try 
        {
            timeStart = System.currentTimeMillis();
            timeDelta = System.currentTimeMillis();
            createXMLFromPath();
            timeRead = System.currentTimeMillis();
            writeXMLToFile();
            timeWrite = System.currentTimeMillis();

            say("Time elapsed for reading file structure: "+(timeRead-timeStart)+"ms");                
            say("Time elapsed for writing the xml-file: "+(timeWrite - timeRead)+"ms");
            say("Time elapsed total: "+(timeWrite-timeStart)+"ms");
            say("File entries: "+countFile);
            say("Folder entries: "+countFolder);
            say("Total entries: "+countTotal);
        } 
        catch (FileNotFoundException ex) 
        {
            LogController.getInstance().log(ex.toString());
            say(ex.getMessage());
        }
    }

    /**
     * println text to the console
     * @param s text to be printed
     */
    private void say(String s)
    {
        //System.out.println("- "+s);
    }

    /**
     * process the arguments
     * @param args passed arguments
     * @return true if arguments are correct
     */
    private boolean processArgs(String[] args)
    {
        if (args.length < 2)
        {
            return false;
        }
        sourcePath = args[0];
        targetPath = args[1];
        if (!targetPath.endsWith("/")) targetPath += "/";
        target = targetPath + filename;
        tempTarget = targetPath + tempFilename;
        backupTarget = targetPath + backupFilename;
        
        sortArgs = new String[args.length-2];
        for (int i = 0; i < args.length-2; i++) 
        {
            sortArgs[i] = args[i+2];            
        }
        return true;
    }

    /**
     * used when this is a standalone executable
     */
    private void printHelp()
    {
        System.out.print("/****************************************/\n");
        System.out.print("\nUsage : \n" +
                "Source Destination [sortArguments]\n" +
                "the first sortArgument has priority\n" +
                "sortArguments can be:\n" +
                "   -F : files are bigger\n" +
                "   -D : directories are bigger\n"+ 
                "   -L : lowercase is bigger\n" +
                "   -U : uppercase is bigger\n");
        System.out.print(
                "   -C : chars (letters) are bigger\n" +
                "   -N : numbers are bigger\n" +
                "   -Sxxx : the suffix xxx is bigger\n" +
                "   -A : sort ascending\n"+
                "   -DE : sort descending\n" +
                "   xxx : the word xxx is bigger\n\n" +
                "a good choice could be -d -n -de\n");
        System.out.print("/****************************************/\n");
    }
     
    /**
     * opens the start location and writes the
     * result root tag
     */
    private void createXMLFromPath() throws FileNotFoundException
    {
        File file = new File(sourcePath);
        resultXML = "<Root>\n";        
        if (file.exists() && file.isDirectory())
        {            
            addToXML(file);
        }
        else
        {
            throw new FileNotFoundException("Folder "+ sourcePath +" does not exist");
        }
        resultXML += "</Root>";                        
    }
    
    /**
     * sorts the Files with the shellsort algorithm
     * @param list the unsorted files to be sorted
     * @return sorted list of files
     */
    private File[] shellSort(File[] list)
    {
        int i, j, k, h;  
        File t;        
        //vordefinierte gute gap sizes (können verändert werden)  
        int cols[] = {23,10,4,1}; 
        for (k=0; k<cols.length; k++) 
        { 
            //gap size auslesen  
            h = cols[k]; 
            //vertauschen, falls in falsche Reihenfolge  
            for (i=h; i<list.length; i++) 
            { 
                j = i; 
                t = list[j]; 
                while (j>=h && isBigger(t,list[j-h])) 
                { 
                    list[j] = list[j-h]; 
                    j = j-h;
                } 
                list[j] = t; 
            } 
        }
        return list;
    }
    
    /**
     * indicates if the first file is bigger
     * than the second
     * possible arguments :
     * -F file
     * -D directory
     * -U upcase
     * -L lowercase
     * -C char
     * -N nummer
     * -S suffix
     * -A ascending
     * -DE descending
     */
    private boolean isBigger(File first, File second)
    {   
        //if (true )return false;
        for (int i = 0; i < sortArgs.length; i++) 
        {            
            String arg = sortArgs[i].toUpperCase();            
            if (arg.length()>1 && arg.charAt(0)=='-')
            {
                int argLen = arg.length();
                char c1 = arg.charAt(1);
                // the File is bigger argument
                if (c1=='F')
                {
                    boolean hasSub1 = hasSubDirectories(first);
                    boolean hasSub2 = hasSubDirectories(second);
                    if(hasSub1 && !hasSub2) return false;
                    if(!hasSub1 && hasSub2) return true;
                }
                else 
                // the Directory is bigger argument
                if (c1=='D' && argLen==2)
                {
                    boolean hasSub1 = hasSubDirectories(first);
                    boolean hasSub2 = hasSubDirectories(second);
                    if(hasSub1 && !hasSub2) return true;
                    if(!hasSub1 && hasSub2) return false;
                }
                else
                // the upper case is bigger argument
                if (c1=='U')
                {
                    int len = first.getName().length();
                    if (second.getName().length()>len) len = second.getName().length();
                    for (int j = 0; j < len; j++) 
                    {
                        if (isUpcaseChar(first.getName(),j) && !isUpcaseChar(second.getName(),j)) return true;
                        if (!isUpcaseChar(first.getName(),j) && isUpcaseChar(second.getName(),j)) return false;
                    }
                }
                else                                        
                // the lower case is bigger argument
                if (c1=='L')
                {
                    int len = first.getName().length();
                    if (second.getName().length()>len) len = second.getName().length();
                    for (int j = 0; j < len; j++) 
                    {
                        if (isUpcaseChar(first.getName(),j) && !isUpcaseChar(second.getName(),j)) return false;
                        if (!isUpcaseChar(first.getName(),j) && isUpcaseChar(second.getName(),j)) return true;
                    }
                }
                else
                // the char is bigger argument
                if (c1=='C')
                {
                    int len = first.getName().length();
                    if (second.getName().length()<len) len = second.getName().length();
                    for (int j = 0; j < len; j++) 
                    {
                        if (isNumber(first.getName().charAt(j)) && !isNumber(second.getName().charAt(j))) return false;
                        if (!isNumber(first.getName().charAt(j)) && isNumber(second.getName().charAt(j))) return true;
                    }                 
                }
                else
                // the number is bigger argument
                if (c1=='N')
                {
                    int len = first.getName().length();
                    if (second.getName().length()<len) len = second.getName().length();
                    for (int j = 0; j < len; j++) 
                    {
                        if (isNumber(first.getName().charAt(j)) && !isNumber(second.getName().charAt(j))) return true;
                        if (!isNumber(first.getName().charAt(j)) && isNumber(second.getName().charAt(j))) return false;
                    }                 
                }
                else
                // the ascending argument
                if (c1=='A')
                {
                    if (first.getName().compareTo(second.getName())>0) return false;
                    if (first.getName().compareTo(second.getName())<0) return true;                    
                }
                else
                // the descending argument
                if (argLen > 2 && c1=='D' && arg.charAt(2)=='E')
                {
                    if (isSpecialTTFelogLogic(first,second)) return specialTTFelogisBigger(first,second);
                    if (first.getName().compareTo(second.getName())>0) return true;
                    if (first.getName().compareTo(second.getName())<0) return false;                    
                }
                else
                // the suffix tag is bigger argument                
                if (c1=='S')
                {
                    String suffix = arg.substring(2);
                    if(first.getName().endsWith(suffix) && !second.getName().endsWith(suffix)) return true;
                    if(!first.getName().endsWith(suffix) && second.getName().endsWith(suffix)) return false;
                }
                
                
            }// '-' argument end
            else
            {
                if ( first.getName().toUpperCase().compareTo(arg.toUpperCase())==0 &&
                     !(second.getName().toUpperCase().compareTo(arg.toUpperCase())==0) 
                    ) return true;
                if ( !(first.getName().toUpperCase().compareTo(arg.toUpperCase())==0) &&
                     second.getName().toUpperCase().compareTo(arg.toUpperCase())==0 
                    ) return false;
            }
        }// for passing all args end
        
        return false;
    }
    
    /**
     * Checks if the a char is upcase or not
     * @param s String where the char is taken from
     * @param pos Position of the char in the String s
     */
    private boolean isUpcaseChar(String s, int pos)
    {
        try
        {
            return ( s.toUpperCase().charAt(pos) ==
                     s.charAt(pos));
        }
        catch (Exception ex)
        {
            return false;
        }
    }  
    
     /**
     * finds out if a char is a number
     */    
    private boolean isNumber(char c)
    {
        switch (c)
        {
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
            case '0' : return true;
            default : return false;
        }
    }
    
    /**
     * gets a file and adds all subfolders to the
     * resultXML string
     */    
    private void addToXML(File file)
    {
        File list[] = file.listFiles(filter);
        //File list[] = file.listFiles();
        //list = sort(list);
        //list = insertionSort(list);
        list = shellSort(list);
        
        if (list!=null)
        {
            for (int i = 0; i < list.length; i++) 
            {
                //if (list[i].isDirectory())
                //{
                    countTotal++;
                    if (countTotal % 250 == 0)
                    {
                        say(countTotal+" entries in "+(System.currentTimeMillis()-timeStart)+"ms last 250 in "+(System.currentTimeMillis()-timeDelta)+"ms");
                        /*if(countTotal==1000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*15.56));else                        
                        if(countTotal==2000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*8.725));else                    
                        if(countTotal==3000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*5.22));else
                        if(countTotal==4000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*2.73));else
                        if(countTotal==5000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*1.814));else
                        if(countTotal==6000) say("geschaetzt insgesamt :"+((System.currentTimeMillis()-timeStart)*1.315));*/
                        
                        timeDelta = System.currentTimeMillis();
                    }                
                    if(hasSubDirectories(list[i]))
                    {
                        resultXML += "<S n=\""+list[i].getName()+"\">\n";
                        addToXML(list[i]);
                        resultXML += "</S>\n";
                        countFolder++;
                    }
                    else
                    {
                        resultXML += "<D n=\""+list[i].getName()+"\"";
                        if (isEmptyFile(list[i]))
                        {
                            resultXML += " e=\"\"";
                        }                    
                        resultXML += "></D>\n";
                        countFile++;
                    }
                //}            
            }
        }
    }
    
    /**
     * finds out if a file is a directory with 
     * subdirectories
     */
    private boolean hasSubDirectories(File file)
    {
        if (file==null) return false;        
        if (file.isDirectory() && file.listFiles()!=null)
        {
            return file.listFiles(filter).length>0;            
        }
        return false;
    }


    
    /**
     * creates a new xml file and fills it with the
     * resultXML content
     */
    private void writeXMLToFile()
    {

        File file = new File(target);
        File tempFile = new File(tempTarget);
        
        // if file exist try to create a backup file
        if(file.exists())
        {
            File backupFile = new File(backupTarget);
            if (!file.renameTo(backupFile))
            {
                say("could not rename treeData.xml to treeData.xml.bak");
                long time = System.currentTimeMillis();
                backupFile = new File(backupTarget+time);
                if (!file.renameTo(backupFile))
                {
                    say("could not rename treeData.xml to treeData.xml.bak"+time);
                    say("no backup was created");
                }
                else
                {
                    say("backup file was created : "+backupFile.getName());
                }
            }
            else
            {
                say("backup file was created : "+backupFile.getName());
            }
        }
        else
        {
            say("treeData.xml does not exists yet");
        }        
        
        
        try 
        {
            //file.createNewFile();
            tempFile.createNewFile();
        } 
        catch (IOException ex) 
        {
            LogController.getInstance().log(ex.toString());
            say("IO exception : "+ex.getMessage());
        }        
        
        // if creation was successfull write tempfile and overwrite old file
        if (tempFile.exists())
        {
            say("created temp file : "+tempFile.getName());
            java.io.FileWriter fw;
            try 
            {
                fw = new FileWriter(tempFile);
                fw.write(resultXML);
                fw.close();
                say("writing data to "+tempFile.getName()+" successfull");
                if (!tempFile.renameTo(file))
                {
                    file.delete();
                    if (!tempFile.renameTo(file))
                    {
                        say("Error : could not overwrite "+file.getName()+" with "+tempFile.getName());
                    }
                    else
                    {
                        say(tempFile.getName()+" was renamed to "+file.getName());
                    }
                }
                else
                {
                    say(tempFile.getName()+" was renamed to "+file.getName());
                }
            } 
            catch (IOException ex) 
            {
                LogController.getInstance().log(ex.toString());
                say("IO exception : "+ex.getMessage());
            }
            finally
            {
                try
                {
                    if(!tempFile.delete())
                    {
                        say("unable to delete "+tempFile.getName());
                    }
                }
                catch(Exception ex)
                {
                    LogController.getInstance().log(ex.toString());
                    say("unable to delete "+tempFile.getName());
                }
            }
        }
    }     
 
    /**
     * checks if folder has any xml entries
     * besides the init.xml entry. If there are
     * then the file is not considered empty
     * @param file shiftfolder which is checked
     * @return true if folder is 'empty'
     */
    private boolean isEmptyFile(File file) 
    {
        //if (true) return false;
        if (file!=null && file.isDirectory())
        {
            File list[] = file.listFiles();
            if (list==null || list.length <= 1) return true;
            for (int i = 0; i < list.length; i++) 
            {
                if (list[i].isFile() && 
                    list[i].getName().toUpperCase().endsWith(".XML") &&
                    !(list[i].getName().toUpperCase().compareTo("INIT.XML")==0) /*&&
                    list[i].length()<10000000*/)
                {
                    return false;
                    // the feature to search for severity!=DELETE was disabled due to performance
                    /*try
                    {
                        FileReader FR = new FileReader(list[i]);                        
                        char[] content = new char[(int)list[i].length()];
                        FR.read(content);
                        String sContent = String.valueOf(content);
                        int index = sContent.toUpperCase().indexOf("<severity>");
                        try
                        {
                            sContent = sContent.substring(index+11).toUpperCase();
                            if (!sContent.startsWith("DELETE")) return false;
                        }
                        catch(StringIndexOutOfBoundsException ex)
                        {
                            LogHelper.getInstance().log(ex.toString()+" makeTreeXML.java 665 bad init.xml: "+list[i].getAbsolutePath());
                            return false;
                        }
                        
                        //say("length is "+ sContent.length());
                    }
                    catch (IOException ex)
                    {
                        LogHelper.getInstance().log(ex.toString()+" makeTreeXML.java 672 bad init.xml: "+list[i].getAbsolutePath());
                        say("Error while parsing xml files for severity tag !");
                    }*/
                    }
                }
            }
        return true;
    }


    /**
     * in TTF 01.03 is bigger than 28.02
     * 28.02 is bigger than 27.02
     * 28.02_n is bigger than 28.02_a
     * 28.02_a is bigger than 28.02_M
     * @param first first file
     * @param second second file
     * @return true if the first file is bigger than the second
     */
    private boolean specialTTFelogisBigger(File first, File second) {
        String f = first.getName();
        String s = second.getName();
        if(f.charAt(3)>s.charAt(3)) return true;
        if(f.charAt(3)<s.charAt(3)) return false;
        if(f.charAt(4)>s.charAt(4)) return true;
        if(f.charAt(4)<s.charAt(4)) return false;
        if(f.charAt(0)>s.charAt(0)) return true;
        if(f.charAt(0)<s.charAt(0)) return false;
        if(f.charAt(1)>s.charAt(1)) return true;
        if(f.charAt(1)<s.charAt(1)) return false;
        if(f.charAt(6)=='n' && s.charAt(6)!='n') return true;
        if(f.charAt(6)!='n' && s.charAt(6)=='n') return false;
        if(f.charAt(6)=='a' && s.charAt(6)=='M') return true;
        if(f.charAt(6)=='M' && s.charAt(6)=='a') return false;
        return true;
    }

    /**
     * Tests for TTF date format which is dd.mm_S
     * d = day
     * m = month
     * S = Shift (M a n){morning afternoon night}
     * @param first first file
     * @param second second file
     * @return true if both files are in TTF date format
     */
    private boolean isSpecialTTFelogLogic(File first, File second) {
        String f = first.getName();
        String s = second.getName();
        return (f.length() == 7 &&
             s.length() == 7 &&
             f.charAt(2)=='.' &&
             s.charAt(2)=='.' &&
             f.charAt(5)=='_' &&
             s.charAt(5)=='_');
    }

}//class end
 

 