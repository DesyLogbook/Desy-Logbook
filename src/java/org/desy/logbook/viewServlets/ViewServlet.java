/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desy.logbook.viewServlets;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.desy.logbook.controller.ConfValuesController;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import org.desy.logbook.helper.IOHelper;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.desy.logbook.types.ConfValues;
import org.desy.logbook.types.VelocityServlet;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Johannes Strampe
 */
public class ViewServlet extends VelocityServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException("No post method supported!");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            //TODO
            String file = request.getParameter("file");
            String name = request.getParameter("name");
            HttpSession s = request.getSession();
            s.setAttribute("selectedFile", file);
            ConfValues conf = ConfValuesController.getInstance().getConf(name);
            String contentFolder = conf.getDatapath() + "/" + name + file;
            String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            content += "<root>" + readAllContentFiles(contentFolder) + "</root>";

            // create the xml document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            byte[] xmlBytes = content.getBytes();
            ByteArrayInputStream stream = new ByteArrayInputStream(xmlBytes);
            Document doc = builder.parse(stream);

            // set the template variables
            NodeList authors = doc.getElementsByTagName("author");
            NodeList titles = doc.getElementsByTagName("title");
            NodeList texts = doc.getElementsByTagName("text");
            int length = authors.getLength();
            Entry[] entries = new Entry[length];
            for (int i = 0; i < length; i++) {
                entries[i] = new Entry();
                entries[i].setAuthor(authors.item(i).getTextContent());
                entries[i].setTitle(titles.item(i).getTextContent());
                entries[i].setText(texts.item(i).getTextContent());
            }
            this.addVelocityVariable("entries", entries);

            this.render("templates/overview.vm", response);
        
        } catch (SAXException ex) {
            Logger.getLogger(ViewServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ViewServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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

    /**
     * Must be public to be used in templates
     */
    public class Entry{

        private String author;
        private String title;
        private String text;

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
