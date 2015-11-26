package replicatorg.app.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import replicatorg.app.Base;
import replicatorg.app.ui.MainWindow;
import replicatorg.app.ui.mainWindow.ModelsOperationCenter;
import replicatorg.app.ui.mainWindow.ModelsOperationCenterScale;
import replicatorg.app.ui.mainWindow.SceneDetailsPanel;

/**
 *
 * @author Dev
 */
public class RSMonitor extends Thread implements Runnable {
    
    private RSProcessor processorThread = null;   
    
    public RSMonitor() {
        super();
    }
    
    @Override
    public void run() {
        try {
            // Starts the first time
            this.processorThread = new RSProcessor();
            
            this.processorThread.start();
        } catch (Exception ex) {
                        
            Logger.getLogger(RSMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }                            
        
        while(true) {
            // Checks for running status
            if (this.processorThread.isReadyToRunAgain()) {
                
                // Tries to load the generated STL
                File f = new File(System.getProperty("user.dir") + "/3dfscan/BEESOFT_3DScan.stl");
                if(f.exists() && !f.isDirectory()) { 
                    MainWindow mainWindow = Base.getMainWindow();
                                
                    mainWindow.updateModelsOperationCenter(new ModelsOperationCenter());
                    SceneDetailsPanel sceneDP = new SceneDetailsPanel();
                    sceneDP.updateBed(Base.getMainWindow().getBed());
                    mainWindow.updateDetailsCenter(sceneDP);
                    mainWindow.getCanvas().unPickAll();
                    
                    mainWindow.loadNewModel(f.getPath());
                    
                    // Scales the model to the medium size
                    if (mainWindow.getCanvas().getControlTool(3).getModelsScaleCenter() == null) {
                        mainWindow.updateModelsOperationCenter(new ModelsOperationCenterScale());
                        
                        Base.setRSRotateActive(false);
                        Base.setRSScaleActive(true);
                    }
                    mainWindow.getCanvas().getControlTool(3).getModelsScaleCenter().scaleToHalf();
                }
                
                try {
                    // restarts the processor thread
                    this.processorThread = new RSProcessor();

                    this.processorThread.start();
                } catch (Exception ex) {

                    Logger.getLogger(RSMonitor.class.getName()).log(Level.SEVERE, null, ex);                    
                }
            } 
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RSMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public RSProcessor getRSProcessor() {
        return this.processorThread;
    }    
    
}
