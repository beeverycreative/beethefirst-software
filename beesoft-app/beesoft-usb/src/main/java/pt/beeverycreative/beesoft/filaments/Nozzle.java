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

    private String type;
    private int sizeInMicrons;

    @XmlElement(name = "resolution")
    private List<Resolution> resolutions;

    public String getType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(String type) {
        this.type = type;
        sizeInMicrons = (int) (Float.parseFloat(type) * 1000);
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
        return this.type.compareTo(noz.type);
    }

    @Override
    public boolean equals(Object o) {
        Nozzle nozzle;

        if (o == null || o instanceof Nozzle == false) {
            return false;
        }

        nozzle = (Nozzle) o;
        return this.type.equalsIgnoreCase(nozzle.type);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
