package com.scalebackend.dao;

import com.scalebackend.dto.FieldDefinitionDTO;
import com.scalebackend.dto.TemplateDTO;
import com.scalebackend.dto.request.CreateTemplateVersionRequestDTO;
import com.scalebackend.dto.request.SaveFieldsVersionRequestDTO;

import java.util.List;

public interface TemplateDao {
    List<TemplateDTO> listTemplates(String kind);
    List<FieldDefinitionDTO> listTemplateFields(Integer templateId);
    TemplateDTO createTemplateVersion(CreateTemplateVersionRequestDTO request);
    TemplateDTO saveFieldsAsNewVersion(SaveFieldsVersionRequestDTO request);
}
