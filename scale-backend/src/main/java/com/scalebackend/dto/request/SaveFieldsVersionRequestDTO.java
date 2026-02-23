package com.scalebackend.dto.request;

import com.scalebackend.dto.FieldDefinitionDTO;

import java.util.ArrayList;
import java.util.List;

public class SaveFieldsVersionRequestDTO {
    public Integer templateId;
    public List<FieldDefinitionDTO> fields = new ArrayList<>();
}
