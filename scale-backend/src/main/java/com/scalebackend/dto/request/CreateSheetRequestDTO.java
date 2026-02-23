package com.scalebackend.dto.request;

import java.util.HashMap;
import java.util.Map;

public class CreateSheetRequestDTO {
    public Integer itemId;
    public Integer templateId;
    public String sheetDate;
    public Integer parentSheetId;
    public Integer outputTemplateId;
    public Map<String, Object> values = new HashMap<>();
}
