package replicatorg.app.util;

import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMCaptureManager;
import intel.rssdk.PXCMHandConfiguration;
import intel.rssdk.PXCMHandData;
import intel.rssdk.PXCMHandModule;
import intel.rssdk.PXCMPoint3DF32;
import intel.rssdk.PXCMPointF32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.pxcmStatus;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import replicatorg.app.Base;
import replicatorg.app.ui.mainWindow.ModelsOperationCenterScale;

/**
 *
 * @author Dev
 */
public class RSProcessor extends Thread implements Runnable {

    private Process scannerExe = null;

    private boolean rsSessionActive;
    private boolean externalRsScanRequest;
    
    public RSProcessor() {
        super();
        this.rsSessionActive = true;
        this.externalRsScanRequest = false;
    }
    
    @Override
    public void run() {
        
	// Create session
	PXCMSession session = PXCMSession.CreateInstance();
	if (session == null) {
            System.out.print("Failed to create a session instance\n");
            return;
	}
        
        PXCMSenseManager senseMgr = session.CreateSenseManager();
        if (senseMgr == null) {
            System.out.print("Failed to create a SenseManager instance\n");
            return;
	}
        
        PXCMCaptureManager captureMgr = senseMgr.QueryCaptureManager();
        captureMgr.FilterByDeviceInfo("RealSense", null, 0);
        
        pxcmStatus sts = senseMgr.EnableHand(null);
        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR)<0) {
            System.out.print("Failed to enable HandAnalysis\n");
            return;
	}
        
        /*
        PXCMHandModule handModule = senseMgr.QueryHand(); 
        PXCMHandConfiguration handConfig = handModule.CreateActiveConfiguration(); 
        handConfig.EnableAllGestures();
        handConfig.EnableAllAlerts();
        handConfig.ApplyChanges();
        handConfig.Update();
        */
        
        sts = senseMgr.Init();
        //if(sts.isError())
            //System.out.println("Init failed and we get " + sts.toString());
        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR)>=0) {
            PXCMHandModule handModule = senseMgr.QueryHand(); 
            PXCMHandConfiguration handConfig = handModule.CreateActiveConfiguration(); 
            handConfig.EnableAllGestures();
            handConfig.EnableAllAlerts();
            //handConfig.EnableTrackedJoints(true);
            //handConfig.EnableNormalizedJoints(true);
            handConfig.ApplyChanges();
            handConfig.Update();
        
            PXCMHandData handData = handModule.CreateOutput();
            
            int nframes = 0;
            boolean modelScaledToMax = false;
            while(rsSessionActive) {
                nframes++;
                sts = senseMgr.AcquireFrame(true);
                
                if (!sts.isSuccessful()) break;
                if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR)<0) break;
                
                
                PXCMCapture.Sample sample = senseMgr.QueryHandSample();
                
                // Query and Display Joint of Hand or Palm
                handData.Update(); 
                
                // poll for gestures
                PXCMHandData.GestureData gestData=new PXCMHandData.GestureData();
                if (handData.IsGestureFired("tap", gestData)) {
                   // handle tap gesture
                   System.out.println("TAP DETECTED");                                      
                                       
                }

                if (handData.IsGestureFired("thumb_up", gestData)) {
                    if (Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter() == null) {
                        Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScale());
                    }
                    Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter().scaleToMax();
                    
                }
                
                if (handData.IsGestureFired("spreadfingers", gestData) || this.externalRsScanRequest) {                    

                    session.close();
                    senseMgr.close();

                    this.startScannerApp();

                    break;                                                           
                }
                
                if (handData.IsGestureFired("thumb_down", gestData)) {
                    Base.getMainWindow().getCanvas().getModelsPanel().resetModel();
                }
                //System.out.println ("Frame # " + nframes + " Hands: " + handData.QueryNumberOfHands());
            
                PXCMHandData.IHand hand = new PXCMHandData.IHand(); 
                sts = handData.QueryHandData(PXCMHandData.AccessOrderType.ACCESS_ORDER_NEAR_TO_FAR, 0, hand);
                
                //if (sts.isError())
                    //continue;
                
                PXCMHandData.JointData data = new PXCMHandData.JointData();                
                                
                if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
                    hand.QueryTrackedJoint(PXCMHandData.JointType.JOINT_CENTER, data);
                    PXCMPointF32 image = hand.QueryMassCenterImage();
                    PXCMPoint3DF32 world = hand.QueryMassCenterWorld();
                
                    //System.out.println("Palm Center at frame " + nframes + ": ");
                    //System.out.print("   Image Position: (" + image.x + "," +image.y + ")");
                    //System.out.println("   World Position: (" + world.x + "," + world.y + "," + world.z + ")");
                }
            
                // alerts
                int nalerts = handData.QueryFiredAlertsNumber();
                //System.out.println("# of alerts at frame " + nframes + " is " + nalerts);
            
                // gestures
                int ngestures = handData.QueryFiredGesturesNumber();
                //System.out.println("# of gestures at frame " + nframes + " is " + ngestures);

                senseMgr.ReleaseFrame();
            }
 
            session.close();
            senseMgr.close();
        }
    } 
    
    /**
     * Stops the session with the Realsense hardware
     */
    public void stopRSSession() {
                                   
        this.rsSessionActive = false;
    }
    
    public void requestExternalRSScan() {
        this.externalRsScanRequest = true;
    }
    
    /**
     * Starts the RealSense Scanner app
     */
    public void startScannerApp() {
                
        System.out.println("Opening scanner application...");
        try {
            this.scannerExe = new ProcessBuilder(
                    System.getProperty("user.dir") + "/3dfscan/DF_3DScan.cs.exe").start();
            this.scannerExe.waitFor();
            
        } catch (IOException ex) {
            Logger.getLogger(RSProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(RSProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
