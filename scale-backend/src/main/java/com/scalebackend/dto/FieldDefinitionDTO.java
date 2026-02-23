package com.scalebackend.dto;

import java.util.ArrayList;
import java.util.List;

public class FieldDefinitionDTO {
    public Integer id;
    public Integer templateId;
    public String key;
    public String label;
    public String type;
    public Boolean required;
    public Integer orderNo;
    public String groupKey;
    public Boolean isMulti;
    public List<String> multiKeys = new ArrayList<>();
    public String unitMode;
    public List<String> unitOptions = new ArrayList<>();
    public List<String> dropdownOptions = new ArrayList<>();
    public TableSchemaDTO tableSchema;
}
