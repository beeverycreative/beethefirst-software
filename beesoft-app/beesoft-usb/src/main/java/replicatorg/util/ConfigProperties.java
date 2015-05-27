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
        
    private Properties appProperties, buildProperties;
    private static final String APP_PROPERTIES_PATH = Base.getApplicationDirectory() + "/config/app.properties";
    private static final String BUILD_PROPERTIES_PATH = "build.properties";
    
    public ConfigProperties() {

        appProperties = new Properties();
        buildProperties = new Properties();
        try {
            appProperties.load(new FileInputStream(APP_PROPERTIES_PATH));
            buildProperties.load(ConfigProperties.class.getClassLoader().getResourceAsStream(BUILD_PROPERTIES_PATH));
            
        } catch (IOException ex) {
            Logger.getLogger(ConfigProperties.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }
    
    public String getAppProperty(String name) {
        return this.appProperties.getProperty(name);
    }
    
    public String getBuildProperty(String name) {
        return this.buildProperties.getProperty(name);
    }
}
