package com.meta.safepill_be.medicine.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmMedicineResponseDto {
    private String efficacy;
    private String use_method;
    private String precautions;

    @JsonSetter("efficacy")
    public void setEfficacy(Object data) {
        if (data instanceof List) {
            this.efficacy = String.join(" ", (List<String>) data);
        } else {
            this.efficacy = String.valueOf(data);
        }
    }

    @JsonSetter("use_method")
    public void setUse_method(Object data) {
        if (data instanceof List) {
            this.use_method = String.join(" ", (List<String>) data);
        } else {
            this.use_method = String.valueOf(data);
        }
    }

    @JsonAlias({"precaution", "precautions"})
    @JsonSetter("precautions")
    public void setPrecautions(Object data) {
        if (data instanceof List) {
            this.precautions = String.join("\n", (List<String>) data);
        } else {
            this.precautions = String.valueOf(data);
        }
    }
}