package com.scalebackend.dto;

import java.util.ArrayList;
import java.util.List;

public class TableColumnDTO {
    public String key;
    public String label;
    public String type;
    public String unitMode;
    public List<String> unitOptions = new ArrayList<>();
    public List<String> dropdownOptions = new ArrayList<>();
}
