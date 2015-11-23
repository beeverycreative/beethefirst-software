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
    
    @XmlAttribute(name="name")
    private String name;
    
    @XmlAttribute(name="value")
    private String value;    
    
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }      
}
