package pt.beeverycreative.beesoft.filaments;

import java.util.HashMap;
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
public class Resolution {

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "parameter")
    private List<SlicerParameter> parameters;

    /**
     * Returns a map with all the parameters and respective values ready for the
     * slicer engine
     *
     * @return
     */
    public HashMap<String, String> getParametersMap() {

        HashMap<String, String> result = new HashMap<String, String>();

        if (parameters != null && parameters.isEmpty() == false) {
            for (SlicerParameter parameter : parameters) {
                result.put(parameter.getName(), parameter.getValue());
            }
        }

        return result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<SlicerParameter> getParameters() {
        return parameters;
    }
}
