package pt.beeverycreative.beesoft.filaments;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author jgrego
 */
//@XmlAccessorType(XmlAccessType.FIELD)
public class Nozzle implements Comparable {

    private int sizeInMicrons;

    @XmlElement(name = "resolution")
    private List<Resolution> resolutions;
    
    public Nozzle() {
    }
    
    public Nozzle(int type) {
        this.sizeInMicrons = type;
    }

    @XmlAttribute(name = "type")
    public void setType(String type) {
        sizeInMicrons = Integer.parseInt(type);
    }

    public int getSizeInMicrons() {
        return sizeInMicrons;
    }

    public List<Resolution> getResolutions() {
        return resolutions;
    }

    @Override
    public int compareTo(Object o) {
        Nozzle noz = (Nozzle) o;
        
        if(sizeInMicrons > noz.sizeInMicrons) {
            return 1;
        } else if (sizeInMicrons == noz.sizeInMicrons) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        Nozzle nozzle;

        if (o == null || o instanceof Nozzle == false) {
            return false;
        }

        nozzle = (Nozzle) o;
        return this.sizeInMicrons == nozzle.sizeInMicrons;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.sizeInMicrons;
        return hash;
    }

    @Override
    public String toString() {
        // return in milimetres
        return Float.toString(this.sizeInMicrons / 1000.0f);
    }
}
