package pt.beeverycreative.beesoft.filaments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author dpacheco
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SlicerConfig {
    
    @XmlAttribute(name="type")
    private String printerName;
    
    @XmlElementWrapper(name="resolutions")
    @XmlElement(name="resolution")    
    private List<Resolution> resolutions;

    public SlicerConfig() {
        this.resolutions = new ArrayList<Resolution>();
    }   
    
    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public List<Resolution> getResolutions() {
        return resolutions;
    }

    public void setResolutions(List<Resolution> resolutions) {
        this.resolutions = resolutions;
    }        
}
