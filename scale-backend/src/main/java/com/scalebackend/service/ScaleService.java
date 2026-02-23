package com.scalebackend.service;

import com.scalebackend.dto.*;
import com.scalebackend.dto.request.CreateSheetRequestDTO;
import com.scalebackend.dto.request.CreateTemplateVersionRequestDTO;
import com.scalebackend.dto.request.SaveFieldsVersionRequestDTO;

import java.util.List;

public interface ScaleService {
    TypeDTO createType(TypeDTO dto);

    List<ItemTreeNodeDTO> getItemsTree();
    ItemDTO getItemById(Integer itemId);
    List<String> listPartIds();

    SheetDTO createSheet(CreateSheetRequestDTO request);
    SheetDetailDTO getSheet(Integer sheetId);
    List<SheetDTO> listItemSheets(Integer itemId, String kind);
    List<SheetDTO> listInputOutputs(Integer inputSheetId);

    List<TemplateDTO> getTemplates(String kind);
    List<FieldDefinitionDTO> getTemplateFields(Integer templateId);
    TemplateDTO createTemplateVersion(CreateTemplateVersionRequestDTO request);
    TemplateDTO saveFieldsAsNewVersion(SaveFieldsVersionRequestDTO request);

    List<PartTemplateMappingDTO> listPartTemplateMappings();
    TemplatesForPartDTO getTemplatesForPart(String partId);
    PartTemplateMappingDTO savePartTemplateMapping(PartTemplateMappingDTO dto);
}
