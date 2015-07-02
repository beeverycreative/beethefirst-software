package pt.beeverycreative.beesoft.filaments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import replicatorg.app.Base;

/**
 *
 * @author dpacheco
 */
public class FilamentManager {
    
    private static FilamentManager instance;

    private static final String filamentsDir = Base.getApplicationDirectory() + "/filaments/";
    
    /**
     * Private constructor for singleton instance
     */
    private FilamentManager() {        
    }
    
    public static FilamentManager getInstance() {
        if (instance == null) {
            instance = new FilamentManager();
        }        
        return instance;
    }
    
    public List<Filament> getFilaments() {
        
        // get all the files from a directory
        File directory = new File(filamentsDir);
        File[] fList = directory.listFiles();
        
        if (fList != null ) {
            List<File> filamentFiles = new ArrayList<File>();

            for (File file : fList) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    filamentFiles.add(file);
                } 
            }

            List<Filament> availableFilaments = new ArrayList<Filament>();
            
            JAXBContext jc;
            Unmarshaller unmarshaller;
            try {
                jc = JAXBContext.newInstance(Filament.class);
                unmarshaller = jc.createUnmarshaller();   

                //Parses all the files
                for (File ff : filamentFiles) {                               

                    Filament fil;
                    try {
                        fil = (Filament) unmarshaller.unmarshal(ff);                    
                        availableFilaments.add(fil);
                    } catch (JAXBException ex) {
                        Logger.getLogger(FilamentManager.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }            
            } catch (JAXBException ex) {
                Logger.getLogger(FilamentManager.class.getName()).log(Level.SEVERE, null, ex);                                            
            }
                       
            return availableFilaments;
        }
        
        return null;
    }            
}
