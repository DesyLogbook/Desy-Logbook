package org.desy.logbook.types;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDate(Date date) {
        SimpleDateFormat dateSDF = new SimpleDateFormat("dd-MM-yyyy");
        this.date = dateSDF.format(date);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTime(Date time) {
        SimpleDateFormat timeSDF = new SimpleDateFormat("HH:mm:ss");
        this.date = timeSDF.format(time);
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
