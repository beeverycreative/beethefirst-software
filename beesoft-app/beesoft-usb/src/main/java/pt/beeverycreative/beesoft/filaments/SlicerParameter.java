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
    private Double value;    

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }      
}
