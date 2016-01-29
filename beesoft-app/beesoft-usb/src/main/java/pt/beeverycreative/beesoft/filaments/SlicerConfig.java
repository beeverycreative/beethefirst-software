package pt.beeverycreative.beesoft.filaments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author dpacheco
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SlicerConfig {
    
    @XmlAttribute(name="type")
    private String printerName;
    
    @XmlElement(name="nozzle")    
    private List<Nozzle> nozzles;

    public SlicerConfig() {
        this.nozzles = new ArrayList<Nozzle>();
    }   
    
    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public List<Nozzle> getNozzles() {
        return nozzles;
    }

    public void setResolutions(List<Nozzle> resolutions) {
        this.nozzles = resolutions;
    }        
}
