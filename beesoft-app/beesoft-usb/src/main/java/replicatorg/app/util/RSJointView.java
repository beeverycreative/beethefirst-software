/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.app.util;

import intel.rssdk.PXCMHandData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;

/**
 *
 * @author Dev
 */
public class RSJointView extends JComponent {

    private static final int WSizeX = 200;

    private PXCMHandData.JointData[][] nodes = new PXCMHandData.JointData[][]{new PXCMHandData.JointData[0x20], new PXCMHandData.JointData[0x20]};
    ;
    private int NBHands;

    public void SetCoordJoint(PXCMHandData.JointData[][] nodes, int NBHands) {
        this.nodes = nodes;
        this.NBHands = NBHands;
        repaint();

    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        BasicStroke strk = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2D.setStroke(strk);

        int scaleFactor = 2;
        int pointCenterX = 0;
        int pointCenterY = 0;
        int pointIndxBaseX = 0;
        int pointIndxBaseY = 0;

        if (NBHands > 0) {
            for (int i = 0; i < NBHands; i++) {
                if (nodes[i][0] == null) {
                    continue;
                }
                int baseX = (int) nodes[i][0].positionImage.x / scaleFactor;
                int baseY = (int) nodes[i][0].positionImage.y / scaleFactor;

                int wristX = (int) nodes[i][0].positionImage.x / scaleFactor;
                int wristY = (int) nodes[i][0].positionImage.y / scaleFactor;
                g2D.setPaint(Color.BLUE);

                for (int j = 1; j < 22; j++) {
                    if (nodes[i][j] == null) {
                        continue;
                    }
                    int x = (int) nodes[i][j].positionImage.x / scaleFactor;
                    int y = (int) nodes[i][j].positionImage.y / scaleFactor;

                    if (nodes[i][j].confidence <= 0) {
                        continue;
                    }

                    if (j == 1) {
                        pointCenterX = x;
                        pointCenterY = y;
                    }
                    if (j == 6) {
                        pointIndxBaseX = x;
                        pointIndxBaseY = y;
                    }

                    if (j == 2 || j == 6 || j == 10 || j == 14 || j == 18) {

                        baseX = x;
                        baseY = y;
                    }
                    if (j == 2 || j == 3 || j == 4 || j == 5) {
                        g2D.drawLine(-baseX + WSizeX, baseY, -x + WSizeX, y);
                    }
                    if (j == 4 || j == 7 || j == 8 || j == 9) {
                        g2D.drawLine(-baseX + WSizeX, baseY, -x + WSizeX, y);
                    }
                    if (j == 10 || j == 11 || j == 12 || j == 13) {
                        g2D.drawLine(-baseX + WSizeX, baseY, -x + WSizeX, y);
                    }
                    if (j == 14 || j == 15 || j == 16 || j == 17) {
                        g2D.drawLine(-baseX + WSizeX, baseY, -x + WSizeX, y);
                    }
                    if (j == 18 || j == 19 || j == 20 || j == 21) {
                        g2D.drawLine(-baseX + WSizeX, baseY, -x + WSizeX, y);
                    }

                    baseX = x;
                    baseY = y;
                }
                for (int j = 0; j < PXCMHandData.NUMBER_OF_JOINTS; j++) {
                    float sz = 4;

                    int x = (int) nodes[i][j].positionImage.x / scaleFactor;
                    int y = (int) nodes[i][j].positionImage.y / scaleFactor;

                    if (nodes[i][j].confidence <= 0) {
                        continue;
                    }

                    //Wrist
                    if (j == 0) {

                    }

                    //Center
                    if (j == 1) {
                        g2D.setPaint(Color.CYAN);
                        sz = (float) Math.sqrt(Math.pow((double) pointIndxBaseX - (double) pointCenterX, 2) + Math.pow((double) pointIndxBaseY - (double) pointCenterY, 2));
                    }

                    //Thumb
                    if (j == 2 || j == 3 || j == 4 || j == 5) {
                        g2D.setPaint(Color.CYAN);
                        sz = 4;
                    }
                    //Index Finger
                    if (j == 4 || j == 7 || j == 8 || j == 9) {
                        g2D.setPaint(Color.CYAN);
                        sz = 4;
                    }
                    //Finger
                    if (j == 10 || j == 11 || j == 12 || j == 13) {
                        g2D.setPaint(Color.CYAN);
                        sz = 4;
                    }
                    //Ring Finger
                    if (j == 14 || j == 15 || j == 16 || j == 17) {
                        g2D.setPaint(Color.CYAN);
                        sz = 4;
                    }
                    //Pinkey
                    if (j == 18 || j == 19 || j == 20 || j == 21) {
                        g2D.setPaint(Color.CYAN);
                        sz = 4;
                    }

                    if (j == 5 || j == 9 || j == 13 || j == 17 || j == 21) {
                        sz = 4;
                        //currnetPen.Width = 1;
                    }

                    g2D.draw(new Ellipse2D.Double(-x + WSizeX - (sz / 2), y - (sz / 2), sz, sz));
                }

            }
        }
    }
}
