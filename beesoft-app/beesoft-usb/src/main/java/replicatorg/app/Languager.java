package replicatorg.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import replicatorg.app.tools.XML;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems This file is part of BEESOFT
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. BEESOFT is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * BEESOFT. If not, see <http://www.gnu.org/licenses/>.
 */
public class Languager {

    private static final String languager_file = getLanguageFilePath();
    private static final String printsetup_file = Base.getApplicationDirectory() + "/machines/printSetup.xml";
    private static final String colors_file = Base.getApplicationDirectory() + "/machines/colorsGCode.xml";
    private static final String startend_file = Base.getApplicationDirectory() + "/machines/startEndCode.xml";


    /**
     * Gets the language file selected in config.properties file. If an appropriate file doesn't exist, return the
     * path to the english file.
     * 
     * @return absolute path to the language file
     */
    private static String getLanguageFilePath() {
        String languageFilesDir, selectedLanguage, finalPath; 
        File languageFile;
        
        languageFilesDir = Base.getApplicationDirectory() + "/languages/";
        selectedLanguage = ProperDefault.get("language").toLowerCase();
        finalPath = languageFilesDir + selectedLanguage + ".xml";
        languageFile = new File(finalPath);
        
        if(languageFile.exists() == true) {
            return finalPath;
        } else {
            return languageFilesDir + "en.xml";
        }
     
    }
    
    /**
     * Gets the file based on a code key
     *
     * @param code key to access the correct file
     * @return file path
     */
    private static String getFile(int code) {
        if (code == 1) {
            return languager_file;
        } else if (code == 2) {
            return printsetup_file;
        } else if (code == 3) {
            return colors_file;
        } else if (code == 4) {
            return startend_file;
        }

        return " ";
    }
    
    /**
     * Gets the base tag from the file type
     *
     * @param code key to access the correct file
     * @return file path
     */
    private static String getBaseTag(int code) {
        if (code == 1) {
            return "tags";            
        } else if (code == 2) {
            return "colors";            
        } else if (code == 3) {
            return "gcode";
        } else if (code == 4) {
            return "gcode";
        }
        return " ";
    }    

    /**
     * Parses tag value from XML and removes string chars only.
     *
     * @param code
     * @param rootTag
     * @param subTag
     * @return plain text array without spaces
     */
    public static String[] getGCodeArray(int code, String rootTag, String subTag) {
        String plain_code = getTagValue(code, rootTag, subTag);
        return plain_code.split(",");
    }

    /**
     * Gets copy from file associated to rootag and subtag.
     *
     * @param code access code to a specific file.
     * @param rootTag root tag for file.
     * @param subTag sub tag of the root tag.
     * @return copy or null value.
     */
    public static String getTagValue(int code, String rootTag, String subTag) {
        String filePath = getFile(code);
        String BASE_TAG = getBaseTag(code);
        
        if (filePath.isEmpty()) {
            return "Error getting tag value";
        }

        if (subTag.contains("endCode")) {
            subTag = "endCode";
        }

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file

            File f = new File(filePath);
            if (f.exists() && f.isFile() && f.canRead()) {

                dom = db.parse(f);
                Element doc = dom.getDocumentElement();
                Node rootNode = doc.cloneNode(true);

                if (XML.hasChildNode(rootNode, BASE_TAG)) {
                    Node startnode = XML.getChildNodeByName(rootNode, BASE_TAG);
                    org.w3c.dom.Element element = (org.w3c.dom.Element) startnode;
                    NodeList nodeList = element.getChildNodes(); // NodeList

                    for (int i = 1; i < nodeList.getLength(); i++) {
                        if (!nodeList.item(i).getNodeName().equals("#text") && !nodeList.item(i).hasChildNodes()) {
                            if (nodeList.item(i).getNodeName().equals(rootTag)) // Found rooTag
                            {
                                return nodeList.item(i).getAttributes().getNamedItem("value").getNodeValue();
                            }
                            //System.out.print(nodeList.item(i).getNodeName() + " Value: " + nodeList.item(i).getAttributes().getNamedItem("value")+"\n");    
                        } else if (!nodeList.item(i).getNodeName().equals("#text") && nodeList.item(i).hasChildNodes()) //SubNode List
                        {
                            if (nodeList.item(i).getNodeName().equals(rootTag)) // Found rooTag
                            {
                                for (int j = 1; j < nodeList.item(i).getChildNodes().getLength(); j += 2) //Each NodeSubList
                                {
                                    if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals(subTag)) // Found subTag
                                    {
                                        return nodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("value").getNodeValue();
                                    }
                                    //                           System.out.println(nodeList.item(i).getNodeName());
                                    //                           System.out.println("\t" + nodeList.item(i).getChildNodes().item(j).getNodeName() + " "+nodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }

                            }
                        }
                    }
                }

            } 
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }


        return null;
    }

    /**
     * Gets all copy from file associated to rootag and subtag. Difference from
     * other similar method is that returns all entries child to subtag.
     *
     * @param code access code to a specific file.
     * @param rootTag root tag for file.
     * @param subTag sub tag of the root tag.
     * @return copy or null value.
     */
    public static HashMap<String, String> getTagValues(int code, String rootTag, String subTag) {
        String filePath = getFile(code);
        String BASE_TAG = getBaseTag(code);
        
        HashMap<String, String> childNodes_rootag = new HashMap<String, String>();

        if (filePath.isEmpty()) {
            return null;
        }
        if (subTag.contains("endCode")) {
            subTag = "endCode";
        }

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file

            File f = new File(filePath);
            if (f.exists() && f.isFile() && f.canRead()) {

                // Parses file and gets rootNode by it self
                dom = db.parse(f);
                Element doc = dom.getDocumentElement();
                Node rootNode = doc.cloneNode(true);

                //root Node has children with value = TAG var
                if (XML.hasChildNode(rootNode, BASE_TAG)) {
                    Node startnode = XML.getChildNodeByName(rootNode, BASE_TAG);
                    org.w3c.dom.Element element = (org.w3c.dom.Element) startnode;
                    NodeList nodeList = element.getChildNodes();

                    //Runs over all children of TAG
                    for (int i = 1; i < nodeList.getLength(); i++) {
                        //If
                        if (!nodeList.item(i).getNodeName().equals("#text") && nodeList.item(i).hasChildNodes()) {
                            //If one of the TAG children is the rootTag
                            if (nodeList.item(i).getNodeName().equals(rootTag)) {
                                //Run over the rootTag children
                                for (int j = 1; j < nodeList.item(i).getChildNodes().getLength(); j += 2) {
                                    Node subNode = nodeList.item(i).getChildNodes().item(j);

                                    if (subNode.getNodeName().equals(subTag) && subNode.hasChildNodes()) // Found subTag and it has childs
                                    {   //Run over the subTag children
                                        for (int k = 1; k < subNode.getChildNodes().getLength(); k += 2) {
                                            childNodes_rootag.put(subNode.getChildNodes().item(k).getAttributes().getNamedItem("value").getNodeValue(), subNode.getChildNodes().item(k).getNodeName());
                                        }
                                        return childNodes_rootag;
                                    }
                                }

                            }
                        }
                    }
                }

            } 
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }


        return null;
    }
    
    /**
     * Gets the list of tag names under rootTag
     *
     * @param code access code to a specific file.
     * @param rootTag root tag for file.
     * 
     * @return copy or null value.
     */
    public static List<String> getTagList(int code, String rootTag) {
        String filePath = getFile(code);
        String BASE_TAG = rootTag;
        
        List<String> childNodes_rootag = new ArrayList<String>();

        if (filePath.isEmpty()) {
            return null;
        }

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file

            File f = new File(filePath);
            if (f.exists() && f.isFile() && f.canRead()) {

                // Parses file and gets rootNode by it self
                dom = db.parse(f);
                Element doc = dom.getDocumentElement();
                Node rootNode = doc.cloneNode(true);

                //root Node has children with value = TAG var
                if (XML.hasChildNode(rootNode, BASE_TAG)) {
                    Node startnode = XML.getChildNodeByName(rootNode, BASE_TAG);
                    org.w3c.dom.Element element = (org.w3c.dom.Element) startnode;
                    NodeList nodeList = element.getChildNodes();

                    //Runs over all children of TAG
                    for (int i = 1; i < nodeList.getLength(); i++) {
                        if (!nodeList.item(i).getNodeName().equals("#text")) {
                            childNodes_rootag.add(nodeList.item(i).getNodeName());
                        }
                    }
                    
                    return childNodes_rootag;
                }

            } else {
                //Base.logger.log(Level.INFO, "Permission denied over {0}", "file with root tag" + BASE_TAG);
            }
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return null;
    }    
}
