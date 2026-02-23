package com.scalebackend.service.impl;

import com.scalebackend.dao.*;
import com.scalebackend.dto.*;
import com.scalebackend.dto.request.CreateSheetRequestDTO;
import com.scalebackend.dto.request.CreateTemplateVersionRequestDTO;
import com.scalebackend.dto.request.SaveFieldsVersionRequestDTO;
import com.scalebackend.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScaleServiceImpl implements ScaleService {

    @Autowired
    private TypeDao typeDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SheetDao sheetDao;
    @Autowired
    private TemplateDao templateDao;
    @Autowired
    private PartTemplateMappingDao partTemplateMappingDao;

    @Override
    @Transactional
    public TypeDTO createType(TypeDTO dto) {
        return typeDao.insert(dto);
    }

    @Override
    public List<ItemTreeNodeDTO> getItemsTree() {
        return itemDao.findItemTree();
    }

    @Override
    public ItemDTO getItemById(Integer itemId) {
        return itemDao.findById(itemId);
    }

    @Override
    public List<String> listPartIds() {
        return itemDao.listPartIds();
    }

    @Override
    @Transactional
    public SheetDTO createSheet(CreateSheetRequestDTO request) {
        return sheetDao.createSheet(request);
    }

    @Override
    public SheetDetailDTO getSheet(Integer sheetId) {
        return sheetDao.getSheet(sheetId);
    }

    @Override
    public List<SheetDTO> listItemSheets(Integer itemId, String kind) {
        return sheetDao.listItemSheets(itemId, kind);
    }

    @Override
    public List<SheetDTO> listInputOutputs(Integer inputSheetId) {
        return sheetDao.listInputOutputs(inputSheetId);
    }

    @Override
    public List<TemplateDTO> getTemplates(String kind) {
        return templateDao.listTemplates(kind);
    }

    @Override
    public List<FieldDefinitionDTO> getTemplateFields(Integer templateId) {
        return templateDao.listTemplateFields(templateId);
    }

    @Override
    @Transactional
    public TemplateDTO createTemplateVersion(CreateTemplateVersionRequestDTO request) {
        return templateDao.createTemplateVersion(request);
    }

    @Override
    @Transactional
    public TemplateDTO saveFieldsAsNewVersion(SaveFieldsVersionRequestDTO request) {
        return templateDao.saveFieldsAsNewVersion(request);
    }

    @Override
    public List<PartTemplateMappingDTO> listPartTemplateMappings() {
        return partTemplateMappingDao.listMappings();
    }

    @Override
    public TemplatesForPartDTO getTemplatesForPart(String partId) {
        return partTemplateMappingDao.getTemplatesForPart(partId);
    }

    @Override
    @Transactional
    public PartTemplateMappingDTO savePartTemplateMapping(PartTemplateMappingDTO dto) {
        return partTemplateMappingDao.saveMapping(dto);
    }
}
