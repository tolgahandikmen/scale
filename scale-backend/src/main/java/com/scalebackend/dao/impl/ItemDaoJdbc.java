package com.scalebackend.dao.impl;

import com.scalebackend.dao.ItemDao;
import com.scalebackend.dto.ItemDTO;
import com.scalebackend.dto.ItemTreeNodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemDaoJdbc implements ItemDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ItemTreeNodeDTO> findItemTree() {
        final String sql = "SELECT id, part_id, part_sn, bridge_name FROM \"1scale\".item_master WHERE is_active = TRUE ORDER BY part_id, part_sn, bridge_name";

        List<ItemDTO> rows = jdbcTemplate.query(sql, (rs, i) -> {
            ItemDTO dto = new ItemDTO();
            dto.id = rs.getInt("id");
            dto.partId = rs.getString("part_id");
            dto.partSn = rs.getString("part_sn");
            dto.bridgeName = rs.getString("bridge_name");
            return dto;
        });

        Map<String, ItemTreeNodeDTO> partMap = new LinkedHashMap<>();
        Map<String, ItemTreeNodeDTO> serialMap = new LinkedHashMap<>();

        for (ItemDTO row : rows) {
            ItemTreeNodeDTO partNode = partMap.computeIfAbsent(row.partId, k -> {
                ItemTreeNodeDTO n = new ItemTreeNodeDTO();
                n.key = "part-" + k;
                n.label = "Part " + k;
                n.type = "PART";
                return n;
            });

            String serialKey = row.partId + "__" + row.partSn;
            ItemTreeNodeDTO serialNode = serialMap.computeIfAbsent(serialKey, k -> {
                ItemTreeNodeDTO n = new ItemTreeNodeDTO();
                n.key = "sn-" + row.partSn;
                n.label = row.partSn;
                n.type = "SERIAL";
                partNode.children.add(n);
                return n;
            });

            ItemTreeNodeDTO bridge = new ItemTreeNodeDTO();
            bridge.key = "bridge-" + row.id;
            bridge.label = row.bridgeName;
            bridge.type = "BRIDGE";
            bridge.itemId = row.id;
            serialNode.children.add(bridge);
        }

        return new ArrayList<>(partMap.values());
    }

    @Override
    public ItemDTO findById(Integer id) {
        final String sql = "SELECT id, part_id, part_sn, bridge_name FROM \"1scale\".item_master WHERE id = ?";
        List<ItemDTO> rows = jdbcTemplate.query(sql, new Object[]{id}, (rs, i) -> {
            ItemDTO dto = new ItemDTO();
            dto.id = rs.getInt("id");
            dto.partId = rs.getString("part_id");
            dto.partSn = rs.getString("part_sn");
            dto.bridgeName = rs.getString("bridge_name");
            return dto;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<String> listPartIds() {
        final String sql = "SELECT DISTINCT part_id FROM \"1scale\".item_master WHERE is_active = TRUE ORDER BY part_id";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getString(1));
    }
}

