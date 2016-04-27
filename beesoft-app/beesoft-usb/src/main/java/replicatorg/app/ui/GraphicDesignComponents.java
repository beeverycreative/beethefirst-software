package replicatorg.app.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import replicatorg.app.Base;

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
public class GraphicDesignComponents {
    
    /** Paths **/
    private static final String RESOURCES_PATH = Base.getApplicationDirectory()+"/resources/mainWindow/";
    private static final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final boolean isHebrew = false;  // remove this, unnecessary
    
    private static final Color machineTextColor = Base.getColorPref("", "#231f20");
    private static final Color buttonsTextColor = Base.getColorPref("", "#231f20");
    private static final Color printButtonTextColor = Base.getColorPref("", "#ffffff");
    
    public static JLabel newButton(String text, String bckImage)
    {
        JLabel label = new JLabel(new ImageIcon(RESOURCES_PATH+bckImage));
        // Gets text depending on language
        if(text.equals("machine_status"))
        {
            label.setText(text);
            label.setFont(getSSProLight("18"));
            label.setForeground(machineTextColor);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setToolTipText("");
        }
        else if(!text.equals("Print"))
        {
            label.setText(text);
            label.setFont(getSSProLight("13"));
            label.setForeground(buttonsTextColor);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setToolTipText("");
        }
        else
        {
            label.setText(text);
            label.setFont(getSSProRegular("13"));
            label.setForeground(printButtonTextColor);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setToolTipText("");
        }
            
        return label;
    }
    
    public static ImageIcon getMenuItemIcon()
    {
        return new ImageIcon(RESOURCES_PATH+"arrrow_menu.png");
    }
    
    
    private static URL getFont(String name)
    {
        URL url;
        
        if(!isHebrew)
        {
            url = ClassLoader.getSystemResource("Source_Sans_Pro/"+name);
        }
        else
        {
            url = ClassLoader.getSystemResource("DejaVu_Sans/"+name);
        }
        
        return url;
    }
    
    public static URL getFile(String name)
    {    
        URL url = ClassLoader.getSystemResource("images/"+name);
        return url;  
    }
    
    public static URL getImage(String folder, String file)
    {
        URL url = ClassLoader.getSystemResource(folder+"/"+file);
        return url;
    }
    
    public static Font getSSProBold (String size)
    {
        Font ssProBold = null;
        
        if(!isHebrew)
        {
            try {
                ssProBold = Font.createFont(Font.TRUETYPE_FONT, getFont("SourceSansPro-Bold.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                ssProBold = Font.createFont(Font.TRUETYPE_FONT, getFont("DejaVuSans-Bold.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
        env.registerFont(ssProBold);
        return ssProBold.deriveFont(Float.parseFloat(size));
    }
    
    public static Font getSSProItalic(String size)
    {
        Font ssProItalic = null;
        
        if(!isHebrew)
        {        
            try {
                ssProItalic = Font.createFont(Font.TRUETYPE_FONT, getFont("SourceSansPro-Italic.ttf").openStream()); 
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                ssProItalic = Font.createFont(Font.TRUETYPE_FONT, getFont("DejaVuSans-Italic.ttf").openStream()); 
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        env.registerFont(ssProItalic);
        return ssProItalic.deriveFont(Float.parseFloat(size));
    }
        
    public static Font getSSProLight(String size)
    {
        Font ssProLight = null;
        
        if(!isHebrew)
        { 
            try {
                ssProLight = Font.createFont(Font.TRUETYPE_FONT, getFont("SourceSansPro-Light.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
             try {
                ssProLight = Font.createFont(Font.TRUETYPE_FONT, getFont("DejaVuSans-Regular.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }           
        }
        
        env.registerFont(ssProLight);
        return ssProLight.deriveFont(Float.parseFloat(size));
    }
            
    public static Font getSSProRegular(String size)
    {
        Font ssProRegular = null;
        
        if(!isHebrew)
        { 
            try {
                ssProRegular = Font.createFont(Font.TRUETYPE_FONT, getFont("SourceSansPro-Regular.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                ssProRegular = Font.createFont(Font.TRUETYPE_FONT, getFont("DejaVuSans-Regular.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }        
        }
        
        env.registerFont(ssProRegular);
        return ssProRegular.deriveFont(Float.parseFloat(size));
    }
                
    public static Font getSSProSemiBold(String size)
    {
        Font ssProSemiBuild = null;
        
        if(!isHebrew)
        { 
            try {
                ssProSemiBuild = Font.createFont(Font.TRUETYPE_FONT, getFont("SourceSansPro-Semibold.ttf").openStream());
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(GraphicDesignComponents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            // NO SEMIBOLD FONT for DejaVuSans
        }
        
        env.registerFont(ssProSemiBuild);
        return ssProSemiBuild.deriveFont(Float.parseFloat(size));
    }
    
    
}
