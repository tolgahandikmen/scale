package com.scalebackend.dao;

import com.scalebackend.dto.ItemDTO;
import com.scalebackend.dto.ItemTreeNodeDTO;

import java.util.List;

public interface ItemDao {
    List<ItemTreeNodeDTO> findItemTree();
    ItemDTO findById(Integer id);
    List<String> listPartIds();
}
