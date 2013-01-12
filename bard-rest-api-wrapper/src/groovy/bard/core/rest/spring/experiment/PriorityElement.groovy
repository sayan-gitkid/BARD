package bard.core.rest.spring.experiment


import bard.core.rest.spring.util.JsonUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriorityElement extends ActivityConcentration {

    @JsonProperty("primaryElements")
    private List<ActivityConcentration> primaryElements

    @JsonProperty("primaryElements")
    public List<ActivityConcentration> getPrimaryElements() {
        return primaryElements
    }

    @JsonProperty("primaryElements")
    public void setPrimaryElements(List<ActivityConcentration> primaryElements) {
        this.primaryElements = primaryElements
    }
    public boolean hasPlot(){
        if(this.concentrationResponseSeries|| this.primaryElements){
            return true
        }
        return false
    }
    public Double getSlope(){
        if(hasPlot()&&this.value){
         return new Double(this.value)
        }
        return null
    }
    public boolean hasChildElements(){
        if(this.childElements){
            return true
        }
        return false
    }
    public String toDisplay(){
        String display = ""
        if(this.qualifier && this.qualifier != "="){
           display = display + qualifier
        }
        if(this.value){
            return display +  " " + this.value
        }
       return ""
    }
}
