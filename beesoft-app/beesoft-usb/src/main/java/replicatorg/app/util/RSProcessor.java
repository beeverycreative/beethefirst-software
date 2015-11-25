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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import replicatorg.app.Base;

import replicatorg.app.ui.mainWindow.ModelsOperationCenterRotate;
import replicatorg.app.ui.mainWindow.ModelsOperationCenterScale;
import replicatorg.app.ui.panels.PrintPanel;

/**
 *
 * @author Dev
 */
public class RSProcessor extends Thread implements Runnable {

    private final JFrame frame = new JFrame();
    private final RSJointView JView = new RSJointView();

    private Process scannerExe = null;

    private boolean rsSessionActive;
    private boolean externalRsScanRequest;
    private boolean readyToRunAgain;
    private final boolean scanFirst;

    private PrintPanel tempPrintPanel;

    public RSProcessor() throws Exception {
        super();
        this.rsSessionActive = true;
        this.scanFirst = false;
        this.externalRsScanRequest = false;
        this.readyToRunAgain = false;
        this.frame.add(JView);
        this.frame.setSize(200, 200);
        this.frame.setResizable(false);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();

        int y = (int) rect.getMaxY() - this.frame.getHeight() - 100;
        frame.setLocation(0, y);
        
    }

    public boolean isReadyToRunAgain() {
        return this.readyToRunAgain;
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
        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
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
        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
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
            double gestureStartTime = 0;

            boolean modelScaledToMax = false;

            //frame.setVisible(true);
            while (rsSessionActive) {
                if (!frame.isActive()) {
                    frame.setVisible(true);
                }
                nframes++;
                sts = senseMgr.AcquireFrame(true);
                handData.Update();

                if (!sts.isSuccessful()) {
                    break;
                }
                if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
                    break;
                }

                PXCMHandData.JointData[][] nodes = new PXCMHandData.JointData[][]{new PXCMHandData.JointData[0x20], new PXCMHandData.JointData[0x20]};
                PXCMHandData.IHand handJoin = new PXCMHandData.IHand();
                PXCMHandData.JointData joint = new PXCMHandData.JointData();

                try {

                    for (int i = 0; i < handData.QueryNumberOfHands(); i++) {

                        handData.QueryHandData(PXCMHandData.AccessOrderType.ACCESS_ORDER_BY_ID, i, handJoin);

                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_WRIST, joint);
                        nodes[i][0] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_CENTER, joint);
                        nodes[i][1] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_THUMB_BASE, joint);
                        nodes[i][2] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_THUMB_JT1, joint);
                        nodes[i][3] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_THUMB_JT2, joint);
                        nodes[i][4] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_THUMB_TIP, joint);
                        nodes[i][5] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_INDEX_BASE, joint);
                        nodes[i][6] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_INDEX_JT1, joint);
                        nodes[i][7] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_INDEX_JT2, joint);
                        nodes[i][8] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_INDEX_TIP, joint);
                        nodes[i][9] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_MIDDLE_BASE, joint);
                        nodes[i][10] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_MIDDLE_JT1, joint);
                        nodes[i][11] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_MIDDLE_JT2, joint);
                        nodes[i][12] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_MIDDLE_TIP, joint);
                        nodes[i][13] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_RING_BASE, joint);
                        nodes[i][14] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_RING_JT1, joint);
                        nodes[i][15] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_RING_JT2, joint);
                        nodes[i][16] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_RING_TIP, joint);
                        nodes[i][17] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_PINKY_BASE, joint);
                        nodes[i][18] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_PINKY_JT1, joint);
                        nodes[i][19] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_PINKY_JT2, joint);
                        nodes[i][20] = joint;
                        joint = new PXCMHandData.JointData();
                        handJoin.QueryTrackedJoint(PXCMHandData.JointType.JOINT_PINKY_TIP, joint);
                        nodes[i][21] = joint;

                    }

                } catch (Exception ex) {

                }
                //if(handData.QueryNumberOfHands() > 0){
                JView.SetCoordJoint(nodes, handData.QueryNumberOfHands());
                //}
                PXCMCapture.Sample sample = senseMgr.QueryHandSample();

                // Query and Display Joint of Hand or Palm
                // poll for gestures
                try {
                    PXCMHandData.GestureData gestData = new PXCMHandData.GestureData();

                    if (handData.IsGestureFired("tap", gestData)) {
                        // handle tap gesture
                        Base.getMainWindow().showCustomMessage("TAP detected! Scaling the model to maximum size.");

                        if (Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter() == null) {
                            Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScale());
                        }
                        Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter().scaleToHalf();

                    }

                    if (handData.IsGestureFired("click", gestData)) {
                        if ((System.currentTimeMillis() - gestureStartTime) > 500) { //Prevents double gestures
                            Base.getMainWindow().showCustomMessage("CLICK detected! Reducing model size.");

                            if (Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter() == null) {
                                Base.getMainWindow().updateModelsOperationCenter(new ModelsOperationCenterScale());
                            }
                            Base.getMainWindow().getCanvas().getControlTool(3).getModelsScaleCenter().scaleToQuarter();

                        }

                        gestureStartTime = System.currentTimeMillis();
                    }

                    if (handData.IsGestureFired("swipe_right", gestData)) {

                        if ((System.currentTimeMillis() - gestureStartTime) > 500) { //Prevents double gestures

                            Base.getMainWindow().showCustomMessage("SWIPE RIGHT detected! Rotating model.");

                            if (Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationRotate() == null) {
                                ModelsOperationCenterRotate mCOR = new ModelsOperationCenterRotate();
                                Base.getMainWindow().getCanvas().getControlTool(3).setModelsOperationRotate(mCOR);
                                Base.getMainWindow().updateModelsOperationCenter(mCOR);
                            }

                            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationRotate().rotate2TheRight();
                        }

                        gestureStartTime = System.currentTimeMillis();
                    }

                    if (handData.IsGestureFired("swipe_left", gestData)) {
                        if ((System.currentTimeMillis() - gestureStartTime) > 500) { //Prevents double gestures
                            Base.getMainWindow().showCustomMessage("SWIPE LEFT detected! Rotating model.");

                            if (Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationRotate() == null) {
                                ModelsOperationCenterRotate mCOR = new ModelsOperationCenterRotate();
                                Base.getMainWindow().getCanvas().getControlTool(3).setModelsOperationRotate(mCOR);
                                Base.getMainWindow().updateModelsOperationCenter(mCOR);
                            }

                            Base.getMainWindow().getCanvas().getControlTool(3).getModelsOperationRotate().rotate2TheLeft();

                        }

                        gestureStartTime = System.currentTimeMillis();
                    }

                    if (handData.IsGestureFired("thumb_down", gestData)) {
                        if ((System.currentTimeMillis() - gestureStartTime) > 500) { //Prevents double gestures
                            Base.getMainWindow().showCustomMessage("THUMB DOWM detected! Rotating model.");

                        }

                        gestureStartTime = System.currentTimeMillis();
                    }

                    if (handData.IsGestureFired("two_fingers_pinch_open", gestData)) {
                        if ((System.currentTimeMillis() - gestureStartTime) > 500) {
                            Base.getMainWindow().showCustomMessage("TWO FINGERS PINCH! Starting 3D scan...");
                            Base.getMainWindow().handleNew(true);
                            session.close();
                            senseMgr.close();
                            this.startScannerApp();
                            gestureStartTime = System.currentTimeMillis();
                            this.readyToRunAgain = true;
                            break;

                        }
                    }

                    if (this.externalRsScanRequest) {
                        session.close();
                        senseMgr.close();
                        this.startScannerApp();
                        this.readyToRunAgain = true;

                        break;
                    }

                    if (handData.IsGestureFired("thumb_up", gestData)) {
                        if ((System.currentTimeMillis() - gestureStartTime) > 1000) { //Prevents double gestures
                            Base.getMainWindow().showCustomMessage("THUMBS UP detected! Starting to print...");

                            if (this.tempPrintPanel == null) {
                                this.tempPrintPanel = Base.getMainWindow().getButtons().startPrint();
                            } else {
                                this.tempPrintPanel.startPrint();
                                this.tempPrintPanel = null;
                            }
                        }
                        gestureStartTime = System.currentTimeMillis();
                    }

                } catch (Exception ex) {
                    System.err.println("Unexpected exception detected: " + ex.getMessage());
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
            this.frame.setVisible(false);
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

    public class PrintPanelHelperThread extends Thread implements Runnable {

        private PrintPanel ppanel;

        @Override
        public void run() {

            this.ppanel = Base.getMainWindow().getButtons().startPrint();
        }

        public void confirmPrint() {
            if (this.ppanel != null) {
                this.ppanel.startPrint();
                this.ppanel.dispose();
                this.ppanel = null;
            }
        }
    }

}
