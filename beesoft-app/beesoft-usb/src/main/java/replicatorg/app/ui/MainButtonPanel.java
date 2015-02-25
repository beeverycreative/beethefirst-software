/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology
 Copyright (c) 2013 BEEVC - Electronic Systems

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package replicatorg.app.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.Languager;
import replicatorg.app.ui.panels.PrintPanel;
import replicatorg.machine.MachineInterface;
import replicatorg.machine.MachineListener;
import replicatorg.machine.MachineProgressEvent;
import replicatorg.machine.MachineState;
import replicatorg.machine.MachineStateChangeEvent;
import replicatorg.machine.MachineToolStatusEvent;

public class MainButtonPanel extends BGPanel implements MachineListener, MouseListener {

    private static final long serialVersionUID = 1L;
    MainWindow editor;
    int width, height;
    Color bgcolor;
    final static Color BACK_COLOR = new Color(0xff, 0xff, 0xff);
    
    /**
     * ***************************************************************************************
     */
    private boolean models_pressed,maintenance_pressed,quickGuide_pressed,print_pressed;
    /**
     * ***************************************************************************************
     */
    private JLabel logo, machine_info, models, maintenance, quick_guide, print;
    /**
     * ***************************************************************************************
     */
    private BGPanel buttons;

    /**
     * ***************************************************************************************
     */
    public MainButtonPanel(MainWindow editor) {
        setLayout(new MigLayout("flowy,fillx"));
        this.editor = editor;
        setBackground(BACK_COLOR);
        buttons = new BGPanel();
        buttons.setOpaque(false);
        buttons.setBackground(BACK_COLOR);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        buttons.setPreferredSize(new Dimension(d.width-220,50));  
        
        logo = GraphicDesignComponents.newButton("", "logo_beethefirst.png");
        machine_info = new JLabel("is not connected");
        models = GraphicDesignComponents.newButton("Models", "b_disabled_7.png");
        maintenance = GraphicDesignComponents.newButton("Maintenance", "b_disabled_7.png");
        quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_disabled_7.png");
        print = GraphicDesignComponents.newButton("Print", "b_disabled_2.png");
        
//        buttons.add(logo);
//        buttons.add(Box.createHorizontalStrut(5));
//        buttons.add(machine_info);
//        buttons.add(Box.createHorizontalStrut(122));
//        buttons.add(models);
//        buttons.add(maintenance);
//        buttons.add(quick_guide);
//        buttons.add(print);

        models.addMouseListener(this);
        maintenance.addMouseListener(this);
        quick_guide.addMouseListener(this);
        print.addMouseListener(this);
        add(buttons);

        models_pressed=maintenance_pressed=quickGuide_pressed=print_pressed=false;
        
        // Update initial state
        machineStateChangedInternal(new MachineStateChangeEvent(null, new MachineState(MachineState.State.NOT_ATTACHED)));
    }

    private void setTextLanguage()
    {
        models.setText(Languager.getTagValue(1,"", ""));
        maintenance.setText(Languager.getTagValue(1,"", ""));
        quick_guide.setText(Languager.getTagValue(1,"", ""));
        print.setText(Languager.getTagValue(1,"", ""));       
    }
       
    public void setMessage(String message)
    {
        //machine_info.setText(Languager.getTagValue(1,"", ""));
        machine_info.setText(message);
    }
    
    public void changeIconsLayout(ComponentEvent e) {
        if (e.getComponent().getWidth() <= 1000) {
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
            buttons.add(Box.createHorizontalStrut(122));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);
            this.add(buttons);
            this.revalidate();
            this.repaint();
        } else {
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
            buttons.add(Box.createHorizontalStrut(e.getComponent().getWidth() - 6 * (models.getWidth()+9)));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);
            this.add(buttons);
            this.revalidate();
            this.repaint();
        }
    }
    public void changeIconsLayout(int width) {
        if (width <= 1000) {
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
            buttons.add(Box.createHorizontalStrut(122));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);
            this.add(buttons);
            this.revalidate();
            this.repaint();
        } else {
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
            buttons.add(Box.createHorizontalStrut(width - 6 * (models.getWidth()+9)));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);
            this.add(buttons);
            this.revalidate();
            this.repaint();
        }
    }

    public void machineStateChanged(MachineStateChangeEvent evt) {
        final MachineStateChangeEvent e = evt;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                machineStateChangedInternal(e);
            }
        });
    }
    
    private void updatePressedStateButton(String button)
    {
        if(button.equals("models"))
        {
            if(models_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_pressed_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons); 
//                this.revalidate();
//                this.repaint();
            }
            else if (!models_pressed)
            {
                
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons); 
//                this.revalidate();
//                this.repaint();
            }

        } else if(button.equals("maintenance"))
        {
            if(maintenance_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_pressed_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
//                this.revalidate();
//                this.repaint(); 
            }  else if (!maintenance_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
//                this.revalidate();
//                this.repaint(); 
            }
  
        } else if(button.equals("quick_guide"))
        {
            if(quickGuide_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_pressed_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
//                this.revalidate();
//                this.repaint();        
            } else if(!quickGuide_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
//                this.revalidate();
//                this.repaint();  
            }
        } else if(button.equals("print"))
        {
            if(print_pressed)
            {
                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_pressed_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
                //this.revalidate();
                //this.repaint();        
            } else if(!print_pressed)
            {
                                models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
                maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
                quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
                print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");

                buttons.removeAll();
                buttons.add(logo);
                buttons.add(Box.createHorizontalStrut(5));
                buttons.add(machine_info);
                buttons.add(Box.createHorizontalStrut(122));
                buttons.add(models);
                buttons.add(maintenance);
                buttons.add(quick_guide);
                buttons.add(print);
                models.addMouseListener(this);
                maintenance.addMouseListener(this);
                quick_guide.addMouseListener(this);
                print.addMouseListener(this);
                this.add(buttons);
                //this.revalidate();
                //this.repaint();  
            }
        }
        editor.setSize(editor.getWidth()+1, editor.getHeight());
        editor.setSize(editor.getWidth()-1, editor.getHeight());
    }
    
    private void updateFromState(final MachineState s, final MachineInterface machine) {
   
        if(s.isConnected())
        {
            models = GraphicDesignComponents.newButton("Models", "b_simple_7.png");
            maintenance = GraphicDesignComponents.newButton("Maintenance", "b_simple_7.png");
            quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_simple_7.png");
            print = GraphicDesignComponents.newButton("Print", "b_simple_2.png");
            
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
//            machine_info.setText(Languager.getTagValue(1,"", ""));
            buttons.add(Box.createHorizontalStrut(122));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);
            models.addMouseListener(this);
            maintenance.addMouseListener(this);
            quick_guide.addMouseListener(this);
            print.addMouseListener(this);
            this.add(buttons);
//            this.revalidate();
//            this.repaint();
        }
        else 
        {
            models = GraphicDesignComponents.newButton("Models", "b_disabled_7.png");
            maintenance = GraphicDesignComponents.newButton("Maintenance", "b_disabled_7.png");
            quick_guide = GraphicDesignComponents.newButton("Quick Guide", "b_disabled_7.png");
            print = GraphicDesignComponents.newButton("Print", "b_disabled_2.png");
            
            buttons.removeAll();
            buttons.add(logo);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(machine_info);
//            machine_info.setText(Languager.getTagValue(1,"", ""));            
            buttons.add(Box.createHorizontalStrut(122));
            buttons.add(models);
            buttons.add(maintenance);
            buttons.add(quick_guide);
            buttons.add(print);        
            models.addMouseListener(this);
            maintenance.addMouseListener(this);
            quick_guide.addMouseListener(this);
            print.addMouseListener(this);
            this.add(buttons);
//            this.revalidate();
//            this.repaint();
        }
        editor.setSize(editor.getWidth()+1, editor.getHeight());
        editor.setSize(editor.getWidth()-1, editor.getHeight());
    }

    public void updateFromMachine(final MachineInterface machine) {
        MachineState s = new MachineState(MachineState.State.NOT_ATTACHED);
        if (machine != null) {
            s = machine.getMachineState();
        }
        updateFromState(s, machine);
    }

    public void machineStateChangedInternal(final MachineStateChangeEvent evt) {
        MachineState s = evt.getState();
        MachineInterface machine = evt.getSource();
        updateFromState(s, machine);
    }

    public void machineProgress(MachineProgressEvent event) {
    }

    public void toolStatusChanged(MachineToolStatusEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource() == models )
        {
          models_pressed = !models_pressed;  
          updatePressedStateButton("models");          
          editor.handleGallery();
        } else if (e.getSource() == maintenance)
        {
          maintenance_pressed = !maintenance_pressed;  
          updatePressedStateButton("maintenance"); 
          editor.handleMaintenance();
        } else if (e.getSource() == quick_guide)
        {
            quickGuide_pressed = !quickGuide_pressed;
            updatePressedStateButton("quick_guide");            
            editor.handleQuickStartWizard();
        } else if (e.getSource() == print)
        {
            print_pressed = !print_pressed;
            updatePressedStateButton("print");
            PrintPanel p = new PrintPanel();
            p.setVisible(true);
//            editor.handleGenBuild();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        return;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        return;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        return;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        return;
    }
}
