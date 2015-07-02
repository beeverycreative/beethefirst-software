package pt.beeverycreative.beesoft.filaments;

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
    
    @XmlAttribute(name="type")
    private String type;
    
    @XmlElement(name="layer_height")
    private SlicerParameter layerHeightValue;
    
    @XmlElement(name="solid_layer_thickness")
    private SlicerParameter solidLayerThicknessValue;
    
    @XmlElement(name="nozzle_size")
    private SlicerParameter nozzleSize;
    
    @XmlElement(name="print_speed")
    private SlicerParameter printSpeed;
    
    @XmlElement(name="infill_speed")
    private SlicerParameter infillSpeed;
    
    @XmlElement(name="inset0_speed")
    private SlicerParameter inset0Speed;
    
    @XmlElement(name="insetx_speed")
    private SlicerParameter insetXSpeed;
    
    @XmlElement(name="filament_flow")
    private SlicerParameter filamentFlow;
    
    @XmlElement(name="cool_min_layer_time")
    private SlicerParameter coolMinLayerTime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SlicerParameter getLayerHeightValue() {
        return layerHeightValue;
    }

    public void setLayerHeightValue(SlicerParameter layerHeightValue) {
        this.layerHeightValue = layerHeightValue;
    }

    public SlicerParameter getSolidLayerThicknessValue() {
        return solidLayerThicknessValue;
    }

    public void setSolidLayerThicknessValue(SlicerParameter solidLayerThicknessValue) {
        this.solidLayerThicknessValue = solidLayerThicknessValue;
    }

    public SlicerParameter getNozzleSize() {
        return nozzleSize;
    }

    public void setNozzleSize(SlicerParameter nozzleSize) {
        this.nozzleSize = nozzleSize;
    }

    public SlicerParameter getPrintSpeed() {
        return printSpeed;
    }

    public void setPrintSpeed(SlicerParameter printSpeed) {
        this.printSpeed = printSpeed;
    }

    public SlicerParameter getInfillSpeed() {
        return infillSpeed;
    }

    public void setInfillSpeed(SlicerParameter infillSpeed) {
        this.infillSpeed = infillSpeed;
    }

    public SlicerParameter getInset0Speed() {
        return inset0Speed;
    }

    public void setInset0Speed(SlicerParameter inset0Speed) {
        this.inset0Speed = inset0Speed;
    }

    public SlicerParameter getInsetXSpeed() {
        return insetXSpeed;
    }

    public void setInsetXSpeed(SlicerParameter insetXSpeed) {
        this.insetXSpeed = insetXSpeed;
    }

    public SlicerParameter getFilamentFlow() {
        return filamentFlow;
    }

    public void setFilamentFlow(SlicerParameter filamentFlow) {
        this.filamentFlow = filamentFlow;
    }

    public SlicerParameter getCoolMinLayerTime() {
        return coolMinLayerTime;
    }

    public void setCoolMinLayerTime(SlicerParameter coolMinLayerTime) {
        this.coolMinLayerTime = coolMinLayerTime;
    }  
}
