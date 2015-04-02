package replicatorg.util;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dpacheco
 */
public class AppProperties {
        
    private Properties properties;
    
    public AppProperties() {

        properties = new Properties();
        try {
            properties.load(AppProperties.class.getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException ex) {
            Logger.getLogger(AppProperties.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }
    
    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }
}
