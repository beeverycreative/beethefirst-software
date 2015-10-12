package replicatorg.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import replicatorg.app.Base;

/**
 *
 * @author dpacheco
 */
public class ConfigProperties {
        
    private Properties filamentProperties, buildProperties;
    private static final String FIRMWARE_PROPERTIES_PATH = Base.getApplicationDirectory() + "/firmware/firmware.properties";
    private static final String BUILD_PROPERTIES_PATH = "build.properties";
    
    public ConfigProperties() {

        filamentProperties = new Properties();
        buildProperties = new Properties();
        try {
            filamentProperties.load(new FileInputStream(FIRMWARE_PROPERTIES_PATH));
            buildProperties.load(ConfigProperties.class.getClassLoader().getResourceAsStream(BUILD_PROPERTIES_PATH));
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigProperties.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }
    
    public String getFirmwareProperty(String name) {
        return this.filamentProperties.getProperty(name);
    }
    
    
    public String getBuildProperty(String name) {
        return this.buildProperties.getProperty(name);
    }
    
}
