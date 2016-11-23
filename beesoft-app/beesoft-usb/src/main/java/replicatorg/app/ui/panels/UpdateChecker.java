package replicatorg.app.ui.panels;

import java.awt.Desktop;
import java.awt.Dialog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.beeverycreative.beesoft.drivers.usb.Version;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.GraphicDesignComponents;
import replicatorg.app.ui.panels.BaseDialog;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

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
public class UpdateChecker extends BaseDialog {

    private static final String SERVER_URL = "https://www.beeverycreative.com/public/software/BEESOFT/";
    private static final String VERSION_FILE = "updates_new.xml";
    private static final String FILAMENTS_REPO_PATH = Base.getAppDataDirectory() + "/filaments/";
    private static final String FILAMENTS_REPO_URL = ProperDefault.get("git.filament_repo_url").replaceAll("\"", "");
    private static final String FILAMENTS_REPO_BRANCH = ProperDefault.get("git.filament_repo_branch").replaceAll("\"", "");
    private File fileFromServer = null;
    private boolean updateStableAvailable = false, updateBetaAvailable = false;
    private String filenameToDownload;

    public UpdateChecker() {
        super(Base.getMainWindow(), Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
        setFont();
        setTextLanguage();
        super.centerOnScreen();
        super.enableDrag();
        evaluateInitialConditions();
    }

    private static void copyFilamentProfiles() {
        final File filamentsFolder;
        final File[] filamentFileArray;
        final FilenameFilter xmlFilter;
        int length;
        byte[] buffer = new byte[1024];
        BufferedInputStream inStream = null;
        BufferedOutputStream outStream = null;
        FileInputStream fileInStream;
        FileOutputStream fileOutStream;

        filamentsFolder = new File(Base.getAppDataDirectory() + "/filaments");

        if (filamentsFolder.exists() == false) {
            filamentsFolder.mkdir();

            xmlFilter = (File dir, String name1) -> name1.endsWith(".xml");

            filamentFileArray = new File(Base.getApplicationDirectory() + "/filaments").listFiles(xmlFilter);

            for (File xmlFile : filamentFileArray) {
                try {
                    fileInStream = new FileInputStream(xmlFile);
                    fileOutStream = new FileOutputStream(filamentsFolder + "/" + xmlFile.getName());
                    inStream = new BufferedInputStream(fileInStream);
                    outStream = new BufferedOutputStream(fileOutStream);

                    while ((length = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, length);
                    }
                } catch (FileNotFoundException ex) {
                    // do nothing
                } catch (IOException ex) {
                    // do nothing
                } finally {
                    try {
                        inStream.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                    try {
                        outStream.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }

        }

    }

    private void updateFilaments() {
        final FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        final Git git;
        final Status repoStatus;
        final RemoteAddCommand remoteAddCmd;
        final ResetCommand resetCmd;
        final CheckoutCommand checkoutCmd;
        final String currentBranch, newBranch;
        final List<Ref> branchList;
        Repository filamentsRepo;
        boolean branchFoundLocally = false;
        RevCommit stash = null;

        repoBuilder.setMustExist(true);
        repoBuilder.setGitDir(new File(FILAMENTS_REPO_PATH + ".git"));

        try {
            try {
                Base.writeLog("Attempting to open repo at " + FILAMENTS_REPO_PATH, this.getClass());
                filamentsRepo = repoBuilder.build();
            } catch (RepositoryNotFoundException ex) {
                try {
                    Base.writeLog("Repository wasn't initialized, initializing it to the given URL: " + FILAMENTS_REPO_URL, this.getClass());
                    repoBuilder.setMustExist(false);
                    filamentsRepo = repoBuilder.build();
                    filamentsRepo.create();
                } catch (IOException ex1) {
                    Base.writeLog("IOException while attempting to initialize repository, not updating filaments", this.getClass());
                    return;
                }
            }

            currentBranch = filamentsRepo.getBranch();

        } catch (IOException ex) {
            Base.writeLog("IOException while attempting to open repository, not updating filaments", this.getClass());
            return;
        }

        git = new Git(filamentsRepo);

        try {
            // it should be only 1, but it shortens the code needed, as the call()
            // method returns an iterable
            for (RevCommit commit : git.log().setMaxCount(1).call()) {
                Base.writeLog("Current commit hash: " + commit, this.getClass());
            }
        } catch (GitAPIException ex) {
            Base.writeLog("GitAPIException while attempting to get current commit's hash. Not a critical error, so proceeding with update", this.getClass());
        }

        try {
            remoteAddCmd = git.remoteAdd();
            remoteAddCmd.setName("origin");
            remoteAddCmd.setUri(new URIish(FILAMENTS_REPO_URL));
            remoteAddCmd.call();
        } catch (URISyntaxException ex) {
            Base.writeLog("Invalid git filament repo remote URL!", this.getClass());
            return;
        } catch (GitAPIException ex) {
            Base.writeLog("GitAPIException thrown when adding remote to git filament repo", this.getClass());
            return;
        }

        try {

            if (currentBranch.equals(FILAMENTS_REPO_BRANCH) == false) {
                Base.writeLog("Repo branch is " + currentBranch + " and it should be " + FILAMENTS_REPO_BRANCH + ", searching for it", this.getClass());
                checkoutCmd = git.checkout();
                checkoutCmd.setName(FILAMENTS_REPO_BRANCH);

                branchList = git.branchList().call();

                for (Ref ref : branchList) {
                    if (ref.getName().contains(FILAMENTS_REPO_BRANCH)) {
                        Base.writeLog("Correct branch was found locally", this.getClass());
                        branchFoundLocally = true;
                        break;
                    }
                }

                if (branchFoundLocally == false) {
                    Base.writeLog("No correct branch was found locally, attempting to checkout a new branch tracking the remote", this.getClass());
                    checkoutCmd.setCreateBranch(true);
                    checkoutCmd.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
                    checkoutCmd.setStartPoint("origin/" + FILAMENTS_REPO_BRANCH);
                    git.fetch().call();
                }

                RevCommit backup = null;
                if (git.status().call().isClean() == false) {
                    git.add().addFilepattern(".").call();
                    backup = git.commit().setMessage("local backup of user modifications").call();
                }

                newBranch = checkoutCmd.call().getName();

                if (newBranch.contains(FILAMENTS_REPO_BRANCH) == false) {
                    Base.writeLog("Unable to change to correct branch, aborting update", this.getClass());
                    return;
                } else {
                    Base.writeLog("Changed to correct branch, " + newBranch, this.getClass());
                }

                try {
                    for (RevCommit commit : git.log().setMaxCount(1).call()) {
                        Base.writeLog("Commit hash after branch change: " + commit, this.getClass());

                    }
                } catch (GitAPIException ex) {
                    // we don't want all the process to stop just because we couldn't acquire the hash here,
                    // hence this catch
                    Base.writeLog("GitAPIException while attempting to get current commit's hash, after changing branch. Not a critical error, so proceeding with update", this.getClass());
                }

                if (backup != null) {
                    // TODO: restore backup of user modifications
                    //git.cherryPick().setNoCommit(true).include(backup).call();
                }
            }

            repoStatus = git.status().call();
            if (repoStatus.hasUncommittedChanges()) {
                Base.writeLog("Repo has uncommited changes, stashing and pulling...", this.getClass());
                stash = git.stashCreate().call();
                git.pull().call();
                git.stashApply().call();        // will apply the last stash made
                git.stashDrop().call();         // remove the last stash made
            } else {
                Base.writeLog("Repo has no uncommited changes, a simple pull will suffice", this.getClass());
                git.pull().call();
            }

            Base.writeLog("Filament update concluded successfully!", this.getClass());

            try {
                for (RevCommit commit : git.log().setMaxCount(1).call()) {
                    Base.writeLog("Commit hash after update process finished: " + commit, this.getClass());

                }
            } catch (GitAPIException ex) {
                // we don't want all the process to stop just because we couldn't acquire the hash here,
                // hence this catch
                Base.writeLog("GitAPIException while attempting to get current commit's hash, after the process finished with success. Not a critical error, so proceeding with update", this.getClass());
            }
        } catch (GitAPIException ex) {
            Base.writeLog("GitAPIException while attempting to update filaments, aborting update", this.getClass());
            try {
                resetCmd = git.reset();
                resetCmd.setMode(ResetType.HARD);
                resetCmd.call();

                if (stash != null) {
                    git.stashApply().call();
                    git.stashDrop().call();
                }

            } catch (GitAPIException ex1) {
                Base.writeLog("GitAPIException while attempting to reset after an error, uh oh...", this.getClass());
            }
        }

    }

    private void setFont() {
        jLabel1.setFont(GraphicDesignComponents.getSSProLight("33"));
        jLabel2.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bCancel.setFont(GraphicDesignComponents.getSSProRegular("12"));
        bDownload.setFont(GraphicDesignComponents.getSSProRegular("12"));

    }

    private void setTextLanguage() {
        jLabel2.setText(Languager.getTagValue("Other", "NotSupported"));
        bCancel.setText(Languager.getTagValue("OptionPaneButtons", "Line3"));
        bDownload.setText(Languager.getTagValue("Other", "Download"));
    }

    private void evaluateInitialConditions() {
        copyFilamentProfiles();
        new Thread(this::checkForUpdates).start();        
        new Thread(this::updateFilaments).start();
    }

    public void checkForUpdates() {
        downloadUpdateFile();
        if (isUpdateStableAvailable()) {
            setMessage("AvailableStable");
            setAlwaysOnTop(true);
            setVisible(true);
        } else if (isUpdateBetaAvailable()) {
            setMessage("AvailableBeta");
            setAlwaysOnTop(true);
            setVisible(true);
        }
    }
    
    public void downloadUpdateFile() {
        updateStableAvailable = false;
        updateBetaAvailable = false;
        filenameToDownload = null;
        fileFromServer = getFileFromServer();
        if (fileFromServer != null) {
            if (seekUpdates()) {
                bDownload.setEnabled(true);
            } else {
                setMessage("NotAvailable");
                bDownload.setEnabled(false);
            }
            fileFromServer.delete();
        } else {
            setMessage("NoAccess");
            bDownload.setEnabled(false);
        }
    }

    public boolean isUpdateStableAvailable() {
        return updateStableAvailable;
    }

    public boolean isUpdateBetaAvailable() {
        return updateBetaAvailable;
    }

    private boolean seekUpdates() {
        String softVersionString, softServerVersionString, softServerBetaVersionString;
        Version currentSoftwareVersion, softwareFromServerVersion, softwareBetaFromServerVersion;
        int localBetaVersion, remoteBetaVersion;
        boolean thisIsBeta;

        softVersionString = Base.VERSION_BEESOFT.split("-")[0];
        thisIsBeta = Base.VERSION_BEESOFT.contains("beta");
        softServerVersionString = getTagValue("Version");
        softServerBetaVersionString = getTagValue("Version_beta");
        currentSoftwareVersion = new Version();
        softwareFromServerVersion = new Version();
        softwareBetaFromServerVersion = new Version();
        currentSoftwareVersion.setVersionFromString(softVersionString);
        softwareFromServerVersion.setVersionFromString(softServerVersionString);
        softwareBetaFromServerVersion.setVersionFromString(softServerBetaVersionString);

        // stable updates
        if (currentSoftwareVersion.compareTo(softwareFromServerVersion) < 0
                || (currentSoftwareVersion.compareTo(softwareFromServerVersion) == 0 && thisIsBeta)) {
            Base.writeLog("New version, " + softwareFromServerVersion + ", available!", this.getClass());
            updateStableAvailable = true;
            grabFilenames();
            return true;
        } else if (currentSoftwareVersion.compareTo(softwareBetaFromServerVersion) == 0 && thisIsBeta) {  // beta updates
            localBetaVersion = getBetaVersion(Base.VERSION_BEESOFT);
            remoteBetaVersion = getBetaVersion(softServerBetaVersionString);

            if (localBetaVersion != -1 && remoteBetaVersion != -1) {
                if (remoteBetaVersion > localBetaVersion) {
                    updateBetaAvailable = true;
                    grabFilenames();
                    return true;
                }
            }
        }
        return false;
    }

    private int getBetaVersion(String betaVersionString) {
        String re1, re2, re3, re4;
        Pattern pattern;
        Matcher matcher;

        re1 = ".*?";
        re2 = "(beta)";
        re3 = "(\\.)";
        re4 = "(\\d+)";

        pattern = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        matcher = pattern.matcher(betaVersionString);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(3));
        }

        return -1;
    }

    private void grabFilenames() {
        if (updateStableAvailable) {
            if (Base.isWindows()) {
                if (useWin10Driver()) {
                    filenameToDownload = getTagValue("FilenameWin10");
                } else {
                    filenameToDownload = getTagValue("FilenameWin");
                }
            } else if (Base.isMacOS()) {
                filenameToDownload = getTagValue("FilenameMac");
            } else {
                filenameToDownload = getTagValue("FilenameTux");
            }
        } else if (updateBetaAvailable) {
            if (Base.isWindows()) {
                if (useWin10Driver()) {
                    filenameToDownload = getTagValue("FilenameWin10Beta");
                } else {
                    filenameToDownload = getTagValue("FilenameWinBeta");
                }
            } else if (Base.isMacOS()) {
                filenameToDownload = getTagValue("FilenameMacBeta");
            } else {
                filenameToDownload = getTagValue("FilenameTuxBeta");
            }
        }
    }

    private boolean useWin10Driver() {
        try {
            // BEESOFT 32 bits on a 64 bit system; suspected to be the most common scenario
            return Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\BEESOFT\\", "Win10Driver");
        } catch (Win32Exception ex) {
            try {
                // if the operating system is actually 32 bits, try this
                return Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, "Software\\BEESOFT\\", "Win10Driver");
            } catch (Win32Exception ex2) {
                Base.writeLog("Failed in obtaining from registry which driver to use, will give the standard version to user.", this.getClass());
                return false;
            }
        }
    }

    public void setMessage(String message) {
        jLabel2.setText("<html>" + Languager.getTagValue("Updates", message) + "</html>");
    }

    private File getFileFromServer() {
        final URL url;
        final File tempFile;
        final BufferedReader br;
        final BufferedWriter bw;
        final FileWriter fw;

        try {
            // get URL content
            url = new URL(SERVER_URL + VERSION_FILE);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;

            //save to this filename
            tempFile = new File(Base.getAppDataDirectory() + "/" + "updates.xml");

            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            //use FileWriter to write file
            fw = new FileWriter(tempFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            while ((inputLine = br.readLine()) != null) {
                bw.write(inputLine);
            }

            bw.close();
            br.close();

        } catch (MalformedURLException e) {
            setMessage("NoConnection");
            Base.writeLog("Cant read update xml from server  " + e.getMessage(), this.getClass());
            return null;
        } catch (IOException e) {
            setMessage("NoConnection");
            Base.writeLog("Cant read update xml from server  " + e.getMessage(), this.getClass());
            return null;
        }
        return tempFile;
    }

    private String getTagValue(String subTag) {

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
                org.w3c.dom.Element element = (org.w3c.dom.Element) rootNode;
                NodeList nodeList = element.getChildNodes(); // NodeList
                Node child;

                for (int i = 0; i < nodeList.getLength(); ++i) { //Each NodeSubList
                    child = nodeList.item(i);
                    if (child.getNodeName().equals(subTag)) // Found subTag
                    {
                        return child.getAttributes().getNamedItem("value").getNodeValue();
                    }
                }

            }
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            Base.writeLog(pce.getMessage(), this.getClass());
        }

        return null;
    }

    private void openURL(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                /* TODO: error handling */ }
        } else {
            /* TODO: error handling */ }
    }

    private void doExit() {
        dispose();
        fileFromServer.delete();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bCancel = new javax.swing.JLabel();
        bDownload = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(350, 180));
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 248, 248));

        jLabel1.setText("BEESOFT");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jPanel4.setBackground(new java.awt.Color(248, 248, 248));
        jPanel4.setMinimumSize(new java.awt.Dimension(62, 26));
        jPanel4.setRequestFocusEnabled(false);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_pressed_9.png"))); // NOI18N
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
                .addGap(40, 40, 40)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        bCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bCancel.setText("Cancel");
        bCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bCancelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bCancelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bCancelMousePressed(evt);
            }
        });

        bDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/panels/b_simple_15.png"))); // NOI18N
        bDownload.setText("Download");
        bDownload.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/replicatorg/app/ui/mainWindow/b_disabled_15.png"))); // NOI18N
        bDownload.setEnabled(false);
        bDownload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bDownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bDownloadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bDownloadMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bDownloadMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bDownload)
                .addGap(12, 12, 12)
                .addComponent(bCancel)
                .addGap(12, 12, 12))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancel)
                    .addComponent(bDownload))
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

    private void bCancelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseEntered
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bCancelMouseEntered

    private void bCancelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMouseExited
        bCancel.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bCancelMouseExited

    private void bDownloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bDownloadMouseEntered
        bDownload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_hover_15.png")));
    }//GEN-LAST:event_bDownloadMouseEntered

    private void bDownloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bDownloadMouseExited
        bDownload.setIcon(new ImageIcon(GraphicDesignComponents.getImage("panels", "b_simple_15.png")));
    }//GEN-LAST:event_bDownloadMouseExited

    private void bCancelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCancelMousePressed
        if (bCancel.isEnabled()) {
            doExit();
        }
    }//GEN-LAST:event_bCancelMousePressed

    private void bDownloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bDownloadMousePressed
        if (bDownload.isEnabled()) {
            fileFromServer.delete();
            try {
                if (Base.isLinux()) {
                    openURL(new URI(SERVER_URL + filenameToDownload));
                } else if (Base.isMacOS()) {
                    openURL(new URI(SERVER_URL + filenameToDownload));
                } else {
                    openURL(new URI(SERVER_URL + filenameToDownload));
                }
            } catch (URISyntaxException ex) {
                Base.writeLog("Searching for new software version. Cant connect to internet", this.getClass());
            }
            dispose();
            //Closes BEESOFT
            System.exit(0);
        }
    }//GEN-LAST:event_bDownloadMousePressed

    private void jLabel15MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MousePressed
        if (jLabel15.isEnabled()) {
            doExit();
        }
    }//GEN-LAST:event_jLabel15MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bCancel;
    private javax.swing.JLabel bDownload;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
