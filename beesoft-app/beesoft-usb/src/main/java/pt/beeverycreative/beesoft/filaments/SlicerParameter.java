package pt.beeverycreative.beesoft.filaments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author dpacheco
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SlicerParameter {
    
    @XmlAttribute(name="value")
    private double value;    

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }      
}
