package replicatorg.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    private static final String LANGUAGE_FILE = getLanguageFilePath();
    private static final String STARTEND_FILE = Base.getApplicationDirectory() + "/machines/startEndCode.xml";

    private static final Map<String, Map<String, String>> LANGUAGE_MAP = getLanguageMap();
    private static final Map<String, String[]> STARTEND_MAP = getStartEndGCode();

    /**
     * Gets the language file selected in config.properties file. If an
     * appropriate file doesn't exist, return the path to the english file.
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

        if (languageFile.exists() == true) {
            return finalPath;
        } else {
            return languageFilesDir + "en.xml";
        }

    }

    private static Node nodeSearch(final NodeList nodeList, final String nodeName) {
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Node node = nodeList.item(i);

            if (node.getNodeName().equals(nodeName)) {
                return node;
            }
        }

        return null;
    }
    
    private static Document getDocument(final String documentName) {
        final DocumentBuilderFactory documentBuilderFactory;
        final DocumentBuilder documentBuilder;
        
        documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Base.writeLog("ParserConfigurationException while creating document builder.", Languager.class);
            Base.writeLog(ex.getMessage(), Languager.class);
            return null;
        }

        try {
            return documentBuilder.parse(documentName);
        } catch (SAXException | IOException ex) {
            Base.writeLog(ex.getClass().getName() + " while creating document.", Languager.class);
            Base.writeLog(ex.getMessage(), Languager.class);
            return null;
        }
    }
    
    private static Map<String, String[]> getStartEndGCode() {
        final Document document;
        final NodeList rootNodeList;
        final Node gcodeNode, startCodeNode, endCodeNode;
        final Element startCodeElement, endCodeElement;
        final Map<String, String[]> resultingMap;

        resultingMap = new HashMap<>();
        document = getDocument(STARTEND_FILE);

        rootNodeList = document.getChildNodes();
        gcodeNode = nodeSearch(rootNodeList, "gcode");

        if (gcodeNode != null) {
            startCodeNode = nodeSearch(gcodeNode.getChildNodes(), "startCode");
            endCodeNode = nodeSearch(gcodeNode.getChildNodes(), "endCode");

            if (startCodeNode instanceof Element) {
                startCodeElement = (Element) startCodeNode;
                resultingMap.put(startCodeElement.getNodeName(), startCodeElement.getAttribute("value").split(","));
            }
            
            if(endCodeNode instanceof Element) {
                endCodeElement = (Element) endCodeNode;
                resultingMap.put(endCodeElement.getNodeName(), endCodeElement.getAttribute("value").split(","));
            }
        }
        
        return resultingMap;
    }

    private static Map<String, Map<String, String>> getLanguageMap() {
        final Document document;
        final NodeList rootNodeList, tagsNodeList;
        final Node languagesNode, tagsNode;
        final Map<String, Map<String, String>> resultingMap;

        resultingMap = new HashMap<>();
        document = getDocument(LANGUAGE_FILE);

        rootNodeList = document.getChildNodes();
        languagesNode = nodeSearch(rootNodeList, "languages");

        if (languagesNode != null) {
            tagsNode = nodeSearch(languagesNode.getChildNodes(), "tags");

            if (tagsNode != null) {
                tagsNodeList = tagsNode.getChildNodes();

                for (int i = 0; i < tagsNodeList.getLength(); ++i) {
                    final Node node = tagsNodeList.item(i);
                    final NodeList nodeList;
                    final Map<String, String> subMap = new HashMap<>();

                    if (node.hasChildNodes()) {
                        nodeList = node.getChildNodes();

                        for (int j = 0; j < nodeList.getLength(); ++j) {
                            final Node childNode = nodeList.item(j);
                            final Element element;

                            if (childNode instanceof Element) {
                                element = (Element) childNode;
                                subMap.put(element.getNodeName(), element.getAttribute("value"));
                            }
                        }

                        resultingMap.put(node.getNodeName(), subMap);
                    }

                }
            }
        }

        return resultingMap;
    }

    /**
     * Parses tag value from XML and removes string chars only.
     *
     * @param subTag
     * @return plain text array without spaces
     */
    public static String[] getGCodeArray(String subTag) {
        return STARTEND_MAP.get(subTag);
    }

    /**
     * Gets copy from file associated to rootag and subtag.
     *
     * @param rootTag root tag for file.
     * @param subTag sub tag of the root tag.
     * @return copy or null value.
     */
    public static String getTagValue(final String rootTag, final String subTag) {
        return LANGUAGE_MAP.get(rootTag).get(subTag);
    }
}
