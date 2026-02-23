package com.scalebackend.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalebackend.dao.SheetDao;
import com.scalebackend.dto.SheetDTO;
import com.scalebackend.dto.SheetDetailDTO;
import com.scalebackend.dto.request.CreateSheetRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class SheetDaoJdbc implements SheetDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SheetDTO createSheet(CreateSheetRequestDTO request) {
        final String sql = "INSERT INTO \"1scale\".sheet_instance(item_id, template_id, created_at, sheet_date, parent_sheet_id, output_template_id, kind, values_json) " +
                "VALUES (?, ?, NOW(), ?, ?, ?, ?, ?::jsonb)";

        String kind = request.parentSheetId == null ? "INPUT" : "OUTPUT";
        String valuesJson;
        try {
            valuesJson = objectMapper.writeValueAsString(request.values == null ? Collections.emptyMap() : request.values);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize sheet values.", e);
        }

        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setInt(i++, request.itemId);
            ps.setInt(i++, request.templateId);

            if (request.sheetDate == null || request.sheetDate.trim().isEmpty()) {
                ps.setNull(i++, Types.DATE);
            } else {
                ps.setDate(i++, java.sql.Date.valueOf(request.sheetDate));
            }

            if (request.parentSheetId == null) {
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, request.parentSheetId);
            }

            if (request.outputTemplateId == null) {
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, request.outputTemplateId);
            }

            ps.setString(i++, kind);
            ps.setString(i, valuesJson);
            return ps;
        }, kh);

        Integer id = kh.getKey() == null ? null : kh.getKey().intValue();
        return mapSheetById(id);
    }

    @Override
    public List<SheetDTO> listItemSheets(Integer itemId, String kind) {
        final String sql = "SELECT id, item_id, template_id, created_at::text AS created_at, sheet_date::text AS sheet_date, parent_sheet_id, output_template_id, kind " +
                "FROM \"1scale\".sheet_instance WHERE item_id = ? AND kind = ? ORDER BY COALESCE(sheet_date, created_at) DESC, id DESC";
        return jdbcTemplate.query(sql, new Object[]{itemId, kind}, (rs, i) -> {
            SheetDTO dto = new SheetDTO();
            dto.id = rs.getInt("id");
            dto.itemId = rs.getInt("item_id");
            dto.templateId = rs.getInt("template_id");
            dto.createdAt = rs.getString("created_at");
            dto.sheetDate = rs.getString("sheet_date");
            dto.parentSheetId = (Integer) rs.getObject("parent_sheet_id");
            dto.outputTemplateId = (Integer) rs.getObject("output_template_id");
            dto.kind = rs.getString("kind");
            return dto;
        });
    }

    @Override
    public List<SheetDTO> listInputOutputs(Integer inputSheetId) {
        final String sql = "SELECT id, item_id, template_id, created_at::text AS created_at, sheet_date::text AS sheet_date, parent_sheet_id, output_template_id, kind " +
                "FROM \"1scale\".sheet_instance WHERE parent_sheet_id = ? AND kind = 'OUTPUT' ORDER BY COALESCE(sheet_date, created_at) DESC, id DESC";
        return jdbcTemplate.query(sql, new Object[]{inputSheetId}, (rs, i) -> {
            SheetDTO dto = new SheetDTO();
            dto.id = rs.getInt("id");
            dto.itemId = rs.getInt("item_id");
            dto.templateId = rs.getInt("template_id");
            dto.createdAt = rs.getString("created_at");
            dto.sheetDate = rs.getString("sheet_date");
            dto.parentSheetId = (Integer) rs.getObject("parent_sheet_id");
            dto.outputTemplateId = (Integer) rs.getObject("output_template_id");
            dto.kind = rs.getString("kind");
            return dto;
        });
    }

    @Override
    public SheetDetailDTO getSheet(Integer sheetId) {
        final String sql = "SELECT id, item_id, template_id, created_at::text AS created_at, sheet_date::text AS sheet_date, parent_sheet_id, output_template_id, kind, values_json::text AS values_json " +
                "FROM \"1scale\".sheet_instance WHERE id = ?";

        List<SheetDetailDTO> rows = jdbcTemplate.query(sql, new Object[]{sheetId}, (rs, i) -> {
            SheetDetailDTO detail = new SheetDetailDTO();
            SheetDTO dto = new SheetDTO();
            dto.id = rs.getInt("id");
            dto.itemId = rs.getInt("item_id");
            dto.templateId = rs.getInt("template_id");
            dto.createdAt = rs.getString("created_at");
            dto.sheetDate = rs.getString("sheet_date");
            dto.parentSheetId = (Integer) rs.getObject("parent_sheet_id");
            dto.outputTemplateId = (Integer) rs.getObject("output_template_id");
            dto.kind = rs.getString("kind");
            detail.sheet = dto;

            try {
                String valuesText = rs.getString("values_json");
                if (valuesText != null) {
                    detail.values = objectMapper.readValue(valuesText, new TypeReference<Map<String, Object>>(){});
                }
            } catch (Exception ex) {
                throw new RuntimeException("Could not deserialize values_json", ex);
            }
            return detail;
        });

        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    private SheetDTO mapSheetById(Integer id) {
        if (id == null) {
            return null;
        }
        final String sql = "SELECT id, item_id, template_id, created_at::text AS created_at, sheet_date::text AS sheet_date, parent_sheet_id, output_template_id, kind FROM \"1scale\".sheet_instance WHERE id = ?";
        List<SheetDTO> rows = jdbcTemplate.query(sql, new Object[]{id}, (rs, i) -> {
            SheetDTO dto = new SheetDTO();
            dto.id = rs.getInt("id");
            dto.itemId = rs.getInt("item_id");
            dto.templateId = rs.getInt("template_id");
            dto.createdAt = rs.getString("created_at");
            dto.sheetDate = rs.getString("sheet_date");
            dto.parentSheetId = (Integer) rs.getObject("parent_sheet_id");
            dto.outputTemplateId = (Integer) rs.getObject("output_template_id");
            dto.kind = rs.getString("kind");
            return dto;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }
}

