package com.scalebackend.dto;

import java.util.ArrayList;
import java.util.List;

public class PartTemplateMappingDTO {
    public String partId;
    public List<Integer> inputTemplateIds = new ArrayList<>();
    public List<Integer> outputTemplateIds = new ArrayList<>();
    public String updatedAt;
}
