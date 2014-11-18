package replicatorg.app.ui.mainWindow;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;
import replicatorg.app.Base;
import replicatorg.app.Languager;
import replicatorg.app.ui.GraphicDesignComponents;

/**
* Copyright (c) 2013 BEEVC - Electronic Systems
* This file is part of BEESOFT software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by the 
* Free Software Foundation, either version 3 of the License, or (at your option)
* any later version. BEESOFT is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
* for more details. You should have received a copy of the GNU General
* Public License along with BEESOFT. If not, see <http://www.gnu.org/licenses/>.
*/
public class MessagesPopUp extends javax.swing.JFrame {

    public MessagesPopUp() {
        initComponents();
        setFont();
        setTextLanguage();
    }

    private void setFont()
    {
        jLabel1.setFont(GraphicDesignComponents.getSSProRegular("12"));
        jLabel3.setFont(GraphicDesignComponents.getSSProBold("12"));
    }
    
    private void setTextLanguage()
    {
        jLabel1.setText(Languager.getTagValue(1,"FeedbackLabel", "StatusMessage"));
        jLabel3.setText(Languager.getTagValue(1,"Tour", "DefaultMessage"));
    }
    
    private void visibility(boolean vsB)
    {
        this.setVisible(vsB);
        dispose();
        this.setSize(new Dimension(550,57));
        this.setPreferredSize(new Dimension(550,57));
        this.validate();
    }
    
    public boolean getActiveness()
    {
        return isVisible();
    }
    
    private void autoHide()
    {
        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                visibility(false);
            }
        }, 5000);
        
    }
    
    private void setLocation()
    {
        GraphicsEnvironment ge = GraphicsEnvironment
                        .getLocalGraphicsEnvironment();
        Rectangle screenRect = ge.getMaximumWindowBounds();
        int posX = 0, posY = 0;
        boolean exception = false;
        
        try{
            posY = Base.getMainWindow().getLocationOnScreen().y + Base.getMainWindow().getContentPane().getHeight()-50;
            posX = Base.getMainWindow().getLocationOnScreen().x + 220;
        }catch(Exception e)
        {
            exception = true;
        }
        
        if(!exception)
        {
            if(Base.getMainWindow().getContentPane().getWidth() > 1050)
            {
                int witdh = Base.getMainWindow().getWidth()-450;
                this.setSize(new Dimension(this.getWidth()+(witdh-this.getWidth()),this.getHeight()));
                this.setPreferredSize(new Dimension(this.getWidth()+(witdh-this.getWidth()),this.getHeight()));
                this.validate();
            }

            this.setLocation(posX,posY);
            setVisible(true);
        }
        
    }
    
    public void setMessage(String message)
    {
        if(message.equals("collision"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageCollision"));
        if(message.equals("notInBed"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageNotInBed"));
        if(message.equals("maxVolume"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageMaxVolume"));
        if(message.equals("outOfBounds"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageOutOfBounds"));
        if(message.equals("importModel"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageImportModel"));
        if(message.equals("saveScene"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageSaveScene"));
        if(message.equals("loadScene"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageLocadScene"));  
        if(message.equals("notSaveScene"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageNotSaveScene"));
        if(message.equals("notLoadScene"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageNotLocadScene"));
        if(message.equals("firstTime"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageFirstTime"));
        if(message.equals("modelNotPicked"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MessageModelNotPicked"));
        if(message.equals("gcodeGeneration"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "GCodeGeneration"));     
        if(message.equals("btfDisconnect"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "BTF_Disconnected"));    
        if(message.equals("btfPrinting"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "BTF_Printing"));         
        if(message.equals("moving"))
            jLabel3.setText(Languager.getTagValue(1,"FeedbackLabel", "MovingMessage2")); 
        if(message.equals("modelMeshError"))
            jLabel3.setText(Languager.getTagValue(1,"StatusMessages", "MeshError")); 
        
        autoHide();
        setLocation();
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFocusable(false);
        setFocusableWindowState(false);
        setMinimumSize(new java.awt.Dimension(550, 57));
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(240, 243, 244));
        jPanel1.setForeground(new java.awt.Color(35, 31, 32));

        jLabel1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(145, 145, 145));
        jLabel1.setText("Status Message");
        jLabel1.setName("statusMessageTitle"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 12)); // NOI18N
        jLabel2.setName("statusMessageLabel"); // NOI18N

        jLabel3.setText("AAAAAAAAAA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 44, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(0, 7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
