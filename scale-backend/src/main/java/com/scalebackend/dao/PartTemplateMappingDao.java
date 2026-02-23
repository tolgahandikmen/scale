package com.scalebackend.dao;

import com.scalebackend.dto.PartTemplateMappingDTO;
import com.scalebackend.dto.TemplatesForPartDTO;

import java.util.List;

public interface PartTemplateMappingDao {
    List<PartTemplateMappingDTO> listMappings();
    TemplatesForPartDTO getTemplatesForPart(String partId);
    PartTemplateMappingDTO saveMapping(PartTemplateMappingDTO mapping);
}
