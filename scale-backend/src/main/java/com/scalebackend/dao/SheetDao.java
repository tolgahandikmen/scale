package com.scalebackend.dao;

import com.scalebackend.dto.SheetDTO;
import com.scalebackend.dto.SheetDetailDTO;
import com.scalebackend.dto.request.CreateSheetRequestDTO;

import java.util.List;

public interface SheetDao {
    SheetDTO createSheet(CreateSheetRequestDTO request);
    List<SheetDTO> listItemSheets(Integer itemId, String kind);
    List<SheetDTO> listInputOutputs(Integer inputSheetId);
    SheetDetailDTO getSheet(Integer sheetId);
}
