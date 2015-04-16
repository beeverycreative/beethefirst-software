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
public class AppProperties {
        
    private Properties properties;
    private static final String APP_PROPERTIES_PATH = Base.getApplicationDirectory() + "/config/app.properties";
    
    public AppProperties() {

        properties = new Properties();
        try {
            properties.load(new FileInputStream(APP_PROPERTIES_PATH));
        } catch (IOException ex) {
            Logger.getLogger(AppProperties.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }
    
    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }
}
