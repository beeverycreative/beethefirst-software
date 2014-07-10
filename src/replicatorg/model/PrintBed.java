package replicatorg.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Shape3D;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.vecmath.Tuple2d;
import replicatorg.app.Base;
import replicatorg.app.ProperDefault;
import replicatorg.app.ui.modeling.EditingModel;
import replicatorg.plugin.toolpath.ToolpathGenerator;
import replicatorg.util.Tuple;

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
public class PrintBed implements Serializable {

    private static final long serialVersionUID = 7526471155622776147L;
    private String name, description;
    private String lastResolution, lastDensity;
    private boolean lasRaft, lastSupport;
    private StringBuffer gcode;
    private File printBedFile;
    private int nModels;
    private ArrayList<Model> printBed_Models;
    private ArrayList<Model> pickedModels;
    private String category;
    private boolean gcodeOK;
    private boolean isSceneDifferent;
    private boolean lastAutonomous;
    private String estimatedTime;
    private final transient String NOT_AVAILABLE = "NA";
    private final transient String UNTITLED = "Untitled";

    public PrintBed(File newScene) {
        if (newScene == null) {
            name = UNTITLED;
            category = UNTITLED;
            estimatedTime = NOT_AVAILABLE;
            description = NOT_AVAILABLE;
            lastResolution = "LOW";
            lastDensity = "LOW";
            lasRaft = false;
            lastSupport = false;
            lastAutonomous = false;
            gcodeOK = false;
            isSceneDifferent = false;
            gcode = new StringBuffer("M637");
            printBedFile = newScene;
            nModels = 0;
            printBed_Models = new ArrayList<Model>();
            pickedModels = new ArrayList<Model>();

        } /**
         * No need for else. All of this bellow its not necessary cause we init
         * a PrintBed on readObject serializable method at MainWindow and then
         * this.updateBed() is called
         */
        else {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(newScene.getAbsolutePath()));
                PrintBed tmpScene = (PrintBed) stream.readObject();
                this.name = tmpScene.name;
                this.description = tmpScene.description;
                this.printBedFile = tmpScene.printBedFile;
                this.nModels = tmpScene.nModels;
                reloadModels();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getLastResolution() {
        return lastResolution;
    }

    public void setLastResolution(String lastResolution) {
        this.lastResolution = lastResolution;
    }

    public String getLastDensity() {
        return lastDensity;
    }

    public void setLastDensity(String lastDensity) {
        this.lastDensity = lastDensity;
    }

    public boolean isLasRaft() {
        return lasRaft;
    }

    public void setLasRaft(boolean lasRaft) {
        this.lasRaft = lasRaft;
    }

    public boolean isLastSupport() {
        return lastSupport;
    }

    public void setLastSupport(boolean lastSupport) {
        this.lastSupport = lastSupport;
    }

    public boolean isLastAutonomous() {
        return lastAutonomous;
    }

    public void setLastAutonomous(boolean autonomous) {
        this.lastAutonomous = autonomous;
    }

    public StringBuffer getGcode() {
        return gcode;
    }

    public boolean isGcodeOK() {
        return this.gcodeOK;
    }

    public void setGcodeOK(boolean gOK) {
        this.gcodeOK = gOK;
    }

    public boolean isSceneDifferent() {
        return isSceneDifferent;
    }

    public void setSceneDifferent(boolean isDifferent) {
        Base.getMainWindow().setOktoGoOnSave(false);
        this.isSceneDifferent = isDifferent;
    }

    public void setGcode(StringBuffer gC) {
        // Cleans previous GCode and stores it
        this.gcode.setLength(0);
        this.gcode = gC;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public Tuple getBedCenter() {
        int minX = 500;
        int maxX = -500;
        int minY = 500;
        int maxY = -500;

        for (int i = 0; i < nModels; i++) {
            EditingModel modelEditer = getModel(i).getEditer();

            double minModelX = modelEditer.getLowerPoint3D().x;
            double maxModelX = modelEditer.getHigherPoint3D().x;
            double minModelY = modelEditer.getLowerPoint3D().y;
            double maxModelY = modelEditer.getHigherPoint3D().y;

            minX = (int) Math.ceil(Math.min(minX, minModelX));
            minY = (int) Math.ceil(Math.min(minY, minModelY));
            maxX = (int) Math.ceil(Math.max(maxX, maxModelX));
            maxY = (int) Math.ceil(Math.max(maxY, maxModelY));

        }

        int centerX = (int) Math.ceil(((maxX + minX) / 2));
        int centerY = (int) Math.ceil(((maxY + minY) / 2));

        return new Tuple(centerX * 1000, centerY * 1000);
    }

    public boolean isEmpty() {
        return nModels == 0;
    }

    public static PrintBed makePrintBed(File sFile) {

        if (sFile == null) {
            return new PrintBed(null);
        }

        if (isSTL(sFile)) {
            PrintBed newScene = new PrintBed(null);
            newScene.addSTL(sFile);
            return newScene;
        }
        if (isScene(sFile)) {
            return new PrintBed(sFile);
        }

        return new PrintBed(null);
    }

    public void addPickedModel(Model mdl) {
//        pickedModels.clear();
        pickedModels.add(mdl);
    }

    public void removePickedModel(Model mdl) {
        pickedModels.remove(mdl);

    }

    public Model getFirstPickedModel() {
        Model m = null;
        if (pickedModels.size() > 0) {
            m = pickedModels.get(0);
        }

        return m;
    }

    public ArrayList<Model> getPickedModels() {
        return pickedModels;
    }

    public ArrayList<Model> getUnPickedModelList() {
        ArrayList<Model> unPickedModels = new ArrayList<Model>();

        for (int i = 0; i < printBed_Models.size(); i++) {
            if (!pickedModels.contains(printBed_Models.get(i))) {
                unPickedModels.add(printBed_Models.get(i));
            }
        }
        return unPickedModels;
    }

    public void saveModelsPositions() {
        for (int i = 0; i < nModels; i++) {
            printBed_Models.get(i).updateScale();
            printBed_Models.get(i).saveModelPositions();
        }
    }

    public void updateScale() {
        for (int i = 0; i < nModels; i++) {
            printBed_Models.get(i).updateScale();
        }
    }

    public void reloadModels() {
        pickedModels = new ArrayList<Model>();
    }

    public void undoTransformation() {
        getFirstPickedModel().getUndoManager().undo();
        Base.getMainWindow().getBed().setSceneDifferent(true);
    }

    public void redoTransformation() {
        getFirstPickedModel().getUndoManager().redo();
        Base.getMainWindow().getBed().setSceneDifferent(true);
    }

    public void resetTransformation() {
        if (getNumberPickedModels() > 0) {
            getFirstPickedModel().resetPosition();
        }
    }

    public void addSTL(File STL_File) {
        printBed_Models.add(new Model(STL_File));
        nModels++;
    }

    public void removeModel() {
//        System.out.println(pickedModels.size());
        while (pickedModels.size() > 0) {
            for (int i = 0; i < pickedModels.size(); i++) {
//                System.out.println("Picked models "+pickedModels.size() );
                Model modelToRemove = pickedModels.get(i);

//                System.out.println("Model to remove "+modelToRemove.getShape().getUserData());
                pickedModels.remove(i);
                printBed_Models.remove(modelToRemove);
                nModels--;
            }
        }
//        System.out.println(pickedModels.size());
    }

    public void duplicateModel() {
        if (pickedModels.size() > 0) {
            Model modelToClone = pickedModels.get(0).clone();
            Model newModel = modelToClone;
            int index = printBed_Models.indexOf(pickedModels.get(0));
            File stl = generateSTL(index);
            addSTL(stl);
            int lastModel = getNumberModels() - 1;
            printBed_Models.get(lastModel).setXscale(modelToClone.getXscalePercentage() / 100);
            printBed_Models.get(lastModel).setYscale(modelToClone.getYscalePercentage() / 100);
            printBed_Models.get(lastModel).setZscale(modelToClone.getZscalePercentage() / 100);
            getModel(lastModel).getEditer().centerAndToBed();
            Base.cleanDirectoryTempFiles(Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER+"/");
        }
    }

    private File generateSTL(int index) {

        File stl = new File(Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER+"/temp.stl");
        PrintWriter pw = null;
        String code = null;

        // Create physical GCODE File
        try {
            pw = new PrintWriter(stl);
            pw.write("");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ToolpathGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        code = new String(printBed_Models.get(index).getStream());
        // write to file
        pw.write(code);

        // Close writer
        pw.close();

        return stl;
    }

    public static byte[] StlToByteArray(File stl) {
        InputStream is = null;
        ByteArrayOutputStream buffer = null;
        int nRead;
        byte[] bytes = new byte[16384];
 
        try {
            is = new FileInputStream(stl);
            buffer = new ByteArrayOutputStream();
            while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                buffer.write(bytes, 0, nRead);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return buffer.toByteArray();
    }

    public static File toFile(byte[] bytes) {
        File stl = new File(Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER+"/" + "temp.stl");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(stl.getAbsolutePath(), false);

            for (int i = 0; i < bytes.length; i += 8192) {
                // Writes progressively 8k for better memory load
                fos.write(bytes, i, Math.min(bytes.length - i, 8192));
            }
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stl;
    }
    
    public static File toFile(byte[] bytes, String fileName) {
        File stl = new File(Base.getAppDataDirectory() + "/"+Base.MODELS_FOLDER+"/" + fileName);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(stl.getAbsolutePath(), false);

            for (int i = 0; i < bytes.length; i += 8192) {
                // Writes progressively 8k for better memory load
                fos.write(bytes, i, Math.min(bytes.length - i, 8192));
            }
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stl;
    }

    public Model getModel(int index) {
        return printBed_Models.get(index);
    }

    public ArrayList<Model> getModels() {
        return printBed_Models;
    }

    public int getNumberModels() {
        return nModels;
    }

    public int getNumberPickedModels() {
        return pickedModels.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public File getPrintBedFile() {
        return printBedFile;
    }

    public Model getModel(Shape3D shape) {
        for (int i = 0; i < printBed_Models.size(); i++) {
            if (printBed_Models.get(i).getShape().getUserData().equals(shape.getUserData())) {
                return printBed_Models.get(i);
            }
        }
        return null;
    }

    public File save(boolean force) {
        if (printBedFile == null) {

            // If save dialog canceled
            if (!saveAs(force)) {
                return null;
            }
        } else {
            if (printBedFile.canRead() && printBedFile.canWrite()) {
                return printBedFile;
            } else {
                return null;
            }
        }
        return printBedFile;
    }

    public boolean saveAs(boolean force) {

        String newName = "Untitled.bee";

        final JFileChooser fileChooser = new JFileChooser();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        File scene = null;
        fileChooser.setDialogTitle("Save Current Scene");
        fileChooser.setCurrentDirectory(new File(ProperDefault.get("defaultSceneDir")));
//        fileChooser.showOpenDialog(Base.getMainWindow());
        //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = 0;
        fileChooser.setVisible(true);
        
        if(!force)
        {
            userSelection = fileChooser.showSaveDialog(Base.getMainWindow());
            scene = fileChooser.getSelectedFile();
        }
        else
        {
            scene = new File(Base.getAppDataDirectory()+"/"+Base.MODELS_FOLDER+"/"+"UntitledScene " + dateFormat.format(date) + ".bee");
        }
        fileChooser.setSelectedFile(scene);
        
//        System.out.println("fileChooser.setSelectedFile: "+fileChooser.getSelectedFile().getAbsolutePath());
        
        if (userSelection == JFileChooser.APPROVE_OPTION ) {
            newName = fileChooser.getSelectedFile().getName();
//
//            System.out.println("fileChooser.getSelectedFile().getAbsolutePath(): "+fileChooser.getSelectedFile().getAbsolutePath());
            
//            System.out.println(fileChooser.getSelectedFile().exists());
//            System.out.println(fileChooser.getSelectedFile().canRead());
//            System.out.println(fileChooser.getSelectedFile().canWrite());
//            System.out.println(force);
//            
            if (fileChooser.getSelectedFile().exists()
                    && fileChooser.getSelectedFile().canRead()
                    && fileChooser.getSelectedFile().canWrite()
                    && !force) {
                
                userSelection = JOptionPane.showConfirmDialog(Base.getMainWindow(),
                        "Replace existing file?");
                // may need to check for cancel option as well
                if (userSelection == JOptionPane.YES_OPTION) {
                    if (newName.contains(".bee")) {
                        newName = fileChooser.getSelectedFile().getName();
                    } else {
                        newName = fileChooser.getSelectedFile().getName().concat(".bee");
                    }

                } else if (userSelection != JOptionPane.YES_OPTION) {
                    return false;
                }
                this.name = newName;
                this.printBedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
                return true;
            } else {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                String folderPath = path.substring(0, path.lastIndexOf(File.separator) + 1);
                String fileName = fileChooser.getSelectedFile().getAbsolutePath().split(".bee")[0];

                if (newName.contains(".bee")) {
                    newName = fileChooser.getSelectedFile().getName();
                } else {
                    newName = newName.concat(".bee");
                }

                this.printBedFile = new File(folderPath.concat(newName));
                
                this.name = printBedFile.getName();
                return true;
            }

        } else // Cancel operation for first save
        {
            Base.getMainWindow().setEnabled(true);
        }

        return false;
    }

    public static boolean isSTL(File fil) {
        String suffix = "";
        String filePath = fil.getAbsolutePath();
        int lastIdx = filePath.lastIndexOf('.');
        if (lastIdx > 0) {
            suffix = filePath.substring(lastIdx + 1);
        }

        if ("stl".equalsIgnoreCase(suffix)) {
            return true;
        }

        return false;
    }

    public static boolean isScene(File fil) {
        String suffix = "";
        String filePath = fil.getAbsolutePath();
        int lastIdx = filePath.lastIndexOf('.');
        if (lastIdx > 0) {
            suffix = filePath.substring(lastIdx + 1);
        }

        if ("bee".equalsIgnoreCase(suffix)) {
            return true;
        }

        return false;
    }

    private void writeObject(ObjectOutputStream aOutputStream) {
        try {
            aOutputStream.defaultWriteObject();
            aOutputStream.writeObject(this);
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readObject(ObjectInputStream aInputStream) {
        try {
            aInputStream.defaultReadObject();
        } catch (IOException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrintBed.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setEstimatedTime(String esTime) {
        this.estimatedTime = esTime;
    }
    
    public String getEstimatedTime()
    {
        /**
         * New versions of BEESOFT can open old scene files where EstimatedTime is not implemented.
         * Getter gives null pointer in that situations and crashes print. This ensures print goes on!
         */
        if(this.estimatedTime != null) {
            return this.estimatedTime;
        } else
        {
            return NOT_AVAILABLE;
        }
        
    }
}
