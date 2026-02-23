package com.scalebackend.dto;

import java.util.ArrayList;
import java.util.List;

public class ItemTreeNodeDTO {
    public String key;
    public String label;
    public String type;
    public Integer itemId;
    public List<ItemTreeNodeDTO> children = new ArrayList<>();
}
