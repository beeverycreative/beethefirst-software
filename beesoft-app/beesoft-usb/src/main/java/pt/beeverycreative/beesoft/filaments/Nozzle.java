/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.beeverycreative.beesoft.filaments;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author jgrego
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Nozzle {

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "resolution")
    private List<Resolution> resolutions;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Resolution> getResolutions() {
        return resolutions;
    }
}
