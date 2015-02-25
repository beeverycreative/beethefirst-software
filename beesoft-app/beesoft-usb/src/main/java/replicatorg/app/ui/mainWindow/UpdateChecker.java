package replicatorg.app.ui.mainWindow;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FontMetrics;
import static java.awt.Frame.ICONIFIED;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.beeverycreative.beesoft.drivers.usb.Version;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.tools.XML;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.drivers.Driver;

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
public class UpdateChecker extends javax.swing.JFrame {

    private int posX = 0, posY = 0;
    private Driver driver;
    private File fileFromServer = null;
    private boolean updateStableAvailable;
    private boolean updateBetaAvailable;
    private String updateString;

    public UpdateChecker() {
        initComponents();
        setFont();
        setTextLanguage();
        centerOnScreen();
        enableDrag();
        evaluateInitialConditions();
        driver = Base.getMachineLoader().getMachineInterface().getDriver();
        setIconImage(new ImageIcon(Base.getImage("images/icon.png", this)).getImage());
    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel18.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel19.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        int fileKey = 1;
        jLabel2.setText(Languager.getTagValue(fileKey, "Other", "NotSupported"));
        jLabel18.setText(Languager.getTagValue(fileKey, "OptionPaneButtons", "Line6"));
        jLabel19.setText(Languager.getTagValue(fileKey, "Other", "Download"));
    }

    private void centerOnScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        this.setLocationRelativeTo(Base.getMainWindow());
    }

    private void evaluateInitialConditions() {
        updateBetaAvailable = false;
        updateStableAvailable = false;
        updateString = null;
        fileFromServer = getFileFromServer();

        if (fileFromServer != null) {
            if (seekUpdates()) {
                if (updateBetaAvailable) {
                    setMessage("AvailableBeta");
                } else {
                    setMessage("AvailableStable");
                }
                updateBetaAvailable = true;
                updateStableAvailable = true;
                fileFromServer.delete();
                jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
            } else {
                setMessage("NotAvailable");
                jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_15.png")));
                fileFromServer.delete();
            }
        } else {
            setMessage("NoAccess");
            jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_disabled_15.png")));
        }
    }

    private String splitString(String s) {
        int width = 325;
        return buildString(s.split("\\."), width);
    }

    private String buildString(String[] parts, int width) {
        String text = "";
        String ihtml = "<html>";
        String ehtml = "</html>";
        String br = "<br>";

        for (int i = 0; i < parts.length; i++) {
            if (i + 1 < parts.length) {
                if (getStringPixelsWidth(parts[i]) + getStringPixelsWidth(parts[i + 1]) < width) {
                    text = text.concat(parts[i]).concat(".").concat(parts[i + 1]).concat(".").concat(br);
                    i++;
                } else {
                    text = text.concat(parts[i]).concat(".").concat(br);
                }
            } else {
                text = text.concat(parts[i]).concat(".");
            }
        }

        return ihtml.concat(text).concat(ehtml);
    }

    private int getStringPixelsWidth(String s) {
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(GraphicDesignComponents.getSSProRegular("10"));
        return fm.stringWidth(s);
    }

    public boolean isUpdateBetaAvailable() {
        return updateBetaAvailable;
    }

    public boolean isUpdateStableAvailable() {
        return updateStableAvailable;
    }

    public boolean seekUpdates() {
        String softVersionString = Base.VERSION_BEESOFT.split("-")[0];
        String softServerVersionString = getTagValue("Software", "Version");
        String softServerVersionBetaString = getTagValue("Software", "Version_beta");
        int betaServerVersion = Integer.valueOf(getTagValue("Software", "BetaVersion"));
        int betaSoftVersion = 0;
        String softServerDateString = getTagValue("Software", "Date");
        String softServerDateBetaString = getTagValue("Software", "Date_beta");

//        String firmVersionString = Base.firmware_version_in_use;
//        String firmServerVersionString = getTagValue("Firmware", "Version");

        Version currentSoftwareVersion = new Version();
        Version softwareFromServer = new Version();
        Version softwareBetaFromServer = new Version();
        currentSoftwareVersion.setVerionFromString(softVersionString);
        softwareFromServer.setVerionFromString(softServerVersionString);
        softwareBetaFromServer.setVerionFromString(softServerVersionBetaString);

//        Version currentFirmwareVersion = new Version();
//        Version firmwareFromServer = new Version();
//        currentFirmwareVersion = new Version().fromFile(firmVersionString);   
//        firmwareFromServer = new Version().fromFile(firmServerVersionString);

//        if(currentFirmwareVersion.compareTo(firmwareFromServer) < 0 )
//            System.out.println("yes");

        /**
         * If is a beta versions, alert for updates of betas or stable
         */
        if (softVersionString.contains("beta")) {
            betaSoftVersion = Integer.valueOf(softVersionString.split("beta")[1]);

            //Base beta version may be different
            if (currentSoftwareVersion.compareTo(softwareBetaFromServer) < 0) {
                updateBetaAvailable = true;
                updateString = softServerVersionBetaString + "-" + softServerDateBetaString;
                return true;
            } else if (currentSoftwareVersion.compareTo(softwareBetaFromServer) == 0 && (betaServerVersion > betaSoftVersion)) {
                //Same base beta version but different version number
                updateBetaAvailable = true;
                updateString = softServerVersionBetaString + "-" + softServerDateBetaString;
                return true;
            } else if (currentSoftwareVersion.compareTo(softwareFromServer) < 0) {
                //In case of none beta update may exist a stable update
                updateStableAvailable = true;
                updateString = softServerVersionString + "-" + softServerDateString;
                return true;
            }
        } else {
            /**
             * Alert for stable updates if BEESOFT version is stable
             */
            if (currentSoftwareVersion.compareTo(softwareFromServer) < 0) {
                updateStableAvailable = true;
                updateString = softServerVersionString + "-" + softServerDateString;
                return true;
            }
        }

        return false;
    }

    public void setMessage(String message) {
        if (message.contains("Available") && !message.equals("NotAvailable")) {
            if (updateBetaAvailable) {
                jLabel2.setText(splitString(Languager.getTagValue(1, "Updates", "AvailableBeta")));
            } else {
                jLabel2.setText(splitString(Languager.getTagValue(1, "Updates", "AvailableStable")));
            }
        } else {
            jLabel2.setText(splitString(Languager.getTagValue(1, "Updates", message)));
        }
    }

    private void enableDrag() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });


        this.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged			
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);

            }
        });
    }

    private File getFileFromServer() {
        URL url;

        try {
            // get URL content
            url = new URL("https://www.beeverycreative.com/public/software/software/updates.xml");
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;

            //save to this filename
            String fileName = "updates.xml";
            fileFromServer = new File(fileName);

            if (!fileFromServer.exists()) {
                fileFromServer.createNewFile();
            }

            //use FileWriter to write file
            FileWriter fw = new FileWriter(fileFromServer.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            while ((inputLine = br.readLine()) != null) {
                bw.write(inputLine);
            }

            bw.close();
            br.close();

        } catch (MalformedURLException e) {
            setMessage("NoConnection");
            Base.writeLog("Cant read update xml from server  " + e.getMessage());
            fileFromServer = null;
        } catch (IOException e) {
            setMessage("NoConnection");
            Base.writeLog("Cant read update xml from server  " + e.getMessage());
            fileFromServer = null;
        }
        return fileFromServer;
    }

    public String getTagValue(String rootTag, String subTag) {

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file

            if (fileFromServer.exists() && fileFromServer.isFile() && fileFromServer.canRead()) {

                dom = db.parse(fileFromServer);
                Element doc = dom.getDocumentElement();
                Node rootNode = doc.cloneNode(true);

                if (XML.hasChildNode(rootNode, "tags")) {
                    Node startnode = XML.getChildNodeByName(rootNode, "tags");
                    org.w3c.dom.Element element = (org.w3c.dom.Element) startnode;
                    NodeList nodeList = element.getChildNodes(); // NodeList

                    for (int i = 1; i < nodeList.getLength(); i++) {
                        if (!nodeList.item(i).getNodeName().equals("#text") && !nodeList.item(i).hasChildNodes()) {
                            if (nodeList.item(i).getNodeName().equals(rootTag)) // Found rooTag
                            {
                                return nodeList.item(i).getAttributes().getNamedItem("value").getNodeValue();
                            }
                        } else if (!nodeList.item(i).getNodeName().equals("#text") && nodeList.item(i).hasChildNodes()) //SubNode List
                        {
                            if (nodeList.item(i).getNodeName().equals(rootTag)) // Found rooTag
                            {
                                for (int j = 1; j < nodeList.item(i).getChildNodes().getLength(); j += 2) //Each NodeSubList
                                {
                                    if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals(subTag)) // Found subTag
                                    {
                                        return nodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("value").getNodeValue().toString();
                                    }
                                }

                            }
                        }
                    }
                }
            } else {
                Base.writeLog("Permission denied over " + "languages/".concat(Base.language).concat(".xml"));
            }
        } catch (ParserConfigurationException pce) {
            Base.writeLog(pce.getMessage());
        } catch (SAXException se) {
            Base.writeLog(se.getMessage());
        } catch (IOException ioe) {
            Base.writeLog(ioe.getMessage());
        }


        return null;
    }

    private void openURL(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) { /* TODO: error handling */ }
        } else { /* TODO: error handling */ }
    }

    private void doExit() {
        dispose();
        fileFromServer.delete();
        Base.bringAllWindowsToFront();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(350, 180));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(350, 180));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("BEESOFT");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel4.setBackground(new java.awt.Color(255, 203, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_11.png"))); // NOI18N
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel13MousePressed(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_10.png"))); // NOI18N

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_9.png"))); // NOI18N
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel15MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel2.setText("Update available");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 199, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jLabel2)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 203, 5));
        jPanel2.setMinimumSize(new java.awt.Dimension(20, 46));

        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_18.png"))); // NOI18N
        jLabel18.setText("OK");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel18MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel18MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel18MousePressed(evt);
            }
        });

        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        jLabel19.setText("Download");
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel19MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel19MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel19MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addGap(12, 12, 12)
                .addComponent(jLabel18)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_18.png")));
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        jLabel18.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_18.png")));
    }//GEN-LAST:event_jLabel18MouseExited

    private void jLabel19MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseEntered
        if (updateBetaAvailable || updateStableAvailable) {
            jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
        }
    }//GEN-LAST:event_jLabel19MouseEntered

    private void jLabel19MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MouseExited
        if (updateBetaAvailable || updateStableAvailable) {
            jLabel19.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
        }
    }//GEN-LAST:event_jLabel19MouseExited

    private void jLabel18MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MousePressed
        doExit();
    }//GEN-LAST:event_jLabel18MousePressed

    private void jLabel19MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel19MousePressed
        fileFromServer.delete();
        if (updateBetaAvailable || updateStableAvailable) {
            try {
                if (Base.isLinux()) {
                    openURL(new URI("https://www.beeverycreative.com/public/software/software/BEESOFT-" + updateString + "-linux.zip"));
                } else if (Base.isMacOS()) {
                    openURL(new URI("https://www.beeverycreative.com/public/software/software/BEESOFT-" + updateString + "-mac.zip"));
                } else {
                    openURL(new URI("https://www.beeverycreative.com/public/software/software/BEESOFT-" + updateString + "-windows.zip"));
                }
            } catch (URISyntaxException ex) {
                Base.writeLog("Searching for new software version. Cant connect to internet");
            }
            dispose();
            Base.bringAllWindowsToFront();
            //Closes BEESOFT
            System.exit(0);
        }
    }//GEN-LAST:event_jLabel19MousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        doExit();
    }//GEN-LAST:event_jLabel15MousePressed

    private void jLabel13MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MousePressed
        setState(ICONIFIED);
    }//GEN-LAST:event_jLabel13MousePressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
