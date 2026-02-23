package com.scalebackend.dto;

import java.util.ArrayList;
import java.util.List;

public class TableSchemaDTO {
    public String rowMode;
    public List<String> defaultRows = new ArrayList<>();
    public List<TableColumnDTO> columns = new ArrayList<>();
}
