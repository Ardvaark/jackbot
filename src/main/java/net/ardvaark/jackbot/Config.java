/*
 * This file is part of JackBot IRC Bot (JackBot).
 * 
 * JackBot is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * JackBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JackBot; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.ardvaark.jackbot;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.ardvaark.jackbot.logging.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class that loads configuration information from an XML file.
 * 
 * @author Brian Vargas
 * @version $Revision: 61 $ $Date: 2008-06-08 00:48:18 -0400 (Sun, 08 Jun 2008) $
 */
public class Config
{
    private static final Log log = Log.getLogger(Config.class);
    
    /**
     * Constructs a Config object that loads from the given file name.
     * 
     * @param filename The name of the file from which to load the XML.
     * @throws FileNotFoundException If the file given by <CODE>filename</CODE>
     *         is not found.
     */
    public Config(String filename) throws FileNotFoundException
    {
        this.filename = filename;
        this.reload();
    }

    /**
     * Reloads the config file.
     */
    public void reload()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = this.configDoc = db.parse(new File(this.filename));
            Element root = doc.getDocumentElement();

            if (!root.getNodeName().equals("Config"))
            {
                // Throw some kind of exception here because
                // this isn't a config file.
            }
        }
        catch (Exception e)
        {
            log.warn("Cannot load config file: {0}", e, this.filename);
        }
    }

    /**
     * Gets the first node in the document under the root node with the given
     * name.
     * 
     * @param nodeName The name of the node to fetch.
     * @return The element node with the given name, or <code>null</code> if
     *         one does not exist.
     */
    public Element getConfigElement(String nodeName)
    {
        Element root = this.configDoc.getDocumentElement();
        NodeList nodes = root.getElementsByTagName(nodeName);
        Element ret = null;

        if (nodes.getLength() > 1)
        {
            // Throw an exception because there is more than
            // one configuration node.
        }
        else
        {
            ret = (Element) nodes.item(0);
        }

        return ret;
    }

    /**
     * The loaded XML document.
     */
    private Document configDoc = null;

    /**
     * The name of the config file.
     */
    private String   filename;
}
