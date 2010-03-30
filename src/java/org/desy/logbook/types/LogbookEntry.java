/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desy.logbook.types;

import org.desy.logbook.helper.IOHelper;

/**
 *
 * @author Johannes Strampe
 */
public class LogbookEntry {

    private String filename;
    private String author;
    private String title;
    private String date;
    private String time;
    private String text;

    public boolean saveFile()
    {
        return IOHelper.writeFile(getFilename(),this.toXML());
    }

    private String toXML()
    {
        String result = "";
        //result += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        result += "<entry>";
        result += "<author>"+getAuthor()+"</author>";
        result += "<title>"+getTitle()+"</title>";
        result += "<text>"+getText()+"</text>";
        result += "</entry>";

        return result;
    }

    public String getFilename() {
        return filename;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public void setFilename(String Filename) {
        this.filename = Filename;
    }

    public void setAuthor(String Author) {
        this.author = Author;
    }

    public void setDate(String Date) {
        this.date = Date;
    }

    public void setText(String Text) {
        this.text = Text;
    }

    public void setTime(String Time) {
        this.time = Time;
    }

    public void setTitle(String Title) {
        this.title = Title;
    }



}
