package pt.beeverycreative.beesoft.filaments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dpacheco
 */
@XmlRootElement(name = "filament")
@XmlAccessorType(XmlAccessType.FIELD)
public class Filament implements Comparable {

    public enum Material {
        PLA, TPU, PET, NYLON;
    }

    @XmlElement(name = "version")
    private String version;

    @XmlElementWrapper(name = "defaults")
    @XmlElement(name = "parameter")
    private List<SlicerParameter> defaultParameters;

    @XmlElement(name = "printer")
    private List<SlicerConfig> supportedPrinters;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "material")
    private Material material;
    
    @XmlElement(name = "UnloadTemperature")
    private String UnloadTemperature;
    
    @XmlElement(name = "UnloadType")
    private String UnloadType;

    public Filament() {
        this.supportedPrinters = new ArrayList<>();
    }

    public Filament(String name) {
        this.name = name;
        this.supportedPrinters = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getDefaultParametersMap() {
        HashMap<String, String> result = new HashMap<>();

        if (defaultParameters.isEmpty() == false) {
            defaultParameters.stream().forEach((parameter) -> {
                result.put(parameter.getName(), parameter.getValue());
            });
        }

        return result;
    }

    public List<SlicerParameter> getDefaultParameters() {
        return defaultParameters;
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

    public Material getMaterial() {
        return material;
    }
    
    public int getUnloadTemperature() {
        return Integer.parseInt(UnloadTemperature);
    }
    
    public int getUnloadType() {
        return Integer.parseInt(UnloadType);
    }

    public List<Resolution> getSupportedResolutions(String printer, int nozzleMicrons) {
        for (SlicerConfig sc : supportedPrinters) {
            if (sc.getPrinterName().equalsIgnoreCase(printer)) {
                for (Nozzle noz : sc.getNozzles()) {
                    if (noz.getSizeInMicrons() == nozzleMicrons) {
                        return noz.getResolutions();
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int compareTo(Object o) {
        Filament f = (Filament) o;
        return this.name.compareTo(f.name);
    }

    @Override
    public boolean equals(Object o) {
        Filament f;

        if (o == null || o instanceof Filament == false) {
            return false;
        }

        f = (Filament) o;
        return this.name.equalsIgnoreCase(f.name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
