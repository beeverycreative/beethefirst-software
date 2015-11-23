package pt.beeverycreative.beesoft.filaments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dpacheco
 */    
@XmlRootElement(name="filament")
@XmlAccessorType(XmlAccessType.FIELD)
public class Filament implements Comparable {
    
    @XmlElement(name="version")
    private String version;
    
    @XmlElementWrapper(name="printers")
    @XmlElement(name="printer")    
    private List<SlicerConfig> supportedPrinters;
    
    @XmlElement(name="name")
    private String name;

    public Filament() {
        this.supportedPrinters = new ArrayList<SlicerConfig>();
    }        

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<SlicerConfig> getSupportedPrinters() {
        return supportedPrinters;
    }

    public void setSupportedPrinters(List<SlicerConfig> supportedPrinters) {
        this.supportedPrinters = supportedPrinters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 
    
    @Override
    public int compareTo(Object o) {
        Filament f = (Filament) o;
        return this.name.compareTo(f.name);
    }
}
