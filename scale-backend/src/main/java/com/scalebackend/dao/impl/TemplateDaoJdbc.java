package com.scalebackend.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalebackend.dao.TemplateDao;
import com.scalebackend.dto.FieldDefinitionDTO;
import com.scalebackend.dto.TemplateDTO;
import com.scalebackend.dto.request.CreateTemplateVersionRequestDTO;
import com.scalebackend.dto.request.SaveFieldsVersionRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

@Repository
public class TemplateDaoJdbc implements TemplateDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<TemplateDTO> listTemplates(String kind) {
        final String baseSql = "SELECT id, code, name, kind, version FROM \"1scale\".sheet_template";
        final String order = " ORDER BY code, version DESC";

        if (kind == null || kind.trim().isEmpty()) {
            return jdbcTemplate.query(baseSql + order, (rs, i) -> mapTemplate(rs));
        }

        return jdbcTemplate.query(baseSql + " WHERE kind = ?" + order, new Object[]{kind}, (rs, i) -> mapTemplate(rs));
    }

    @Override
    public List<FieldDefinitionDTO> listTemplateFields(Integer templateId) {
        final String sql = "SELECT id, template_id, key, label, type, required, order_no, group_key, is_multi, multi_keys_json::text AS multi_keys_json, " +
                "unit_mode, unit_options_json::text AS unit_options_json, dropdown_options_json::text AS dropdown_options_json, table_schema_json::text AS table_schema_json " +
                "FROM \"1scale\".field_definition WHERE template_id = ? ORDER BY order_no";

        return jdbcTemplate.query(sql, new Object[]{templateId}, (rs, i) -> {
            FieldDefinitionDTO dto = new FieldDefinitionDTO();
            dto.id = rs.getInt("id");
            dto.templateId = rs.getInt("template_id");
            dto.key = rs.getString("key");
            dto.label = rs.getString("label");
            dto.type = rs.getString("type");
            dto.required = rs.getBoolean("required");
            dto.orderNo = rs.getInt("order_no");
            dto.groupKey = rs.getString("group_key");
            dto.isMulti = rs.getBoolean("is_multi");
            dto.unitMode = rs.getString("unit_mode");

            dto.multiKeys = readStringList(rs.getString("multi_keys_json"));
            dto.unitOptions = readStringList(rs.getString("unit_options_json"));
            dto.dropdownOptions = readStringList(rs.getString("dropdown_options_json"));

            String tableSchemaJson = rs.getString("table_schema_json");
            if (tableSchemaJson != null && !tableSchemaJson.trim().isEmpty()) {
                try {
                    dto.tableSchema = objectMapper.readValue(tableSchemaJson, com.scalebackend.dto.TableSchemaDTO.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not parse table_schema_json", ex);
                }
            }
            return dto;
        });
    }

    @Override
    public TemplateDTO createTemplateVersion(CreateTemplateVersionRequestDTO request) {
        Integer nextVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version), 0) + 1 FROM \"1scale\".sheet_template WHERE code = ?",
                Integer.class,
                request.code
        );

        final String sql = "INSERT INTO \"1scale\".sheet_template(code, name, kind, version) VALUES (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.code);
            ps.setString(2, request.name);
            ps.setString(3, request.kind);
            ps.setInt(4, nextVersion == null ? 1 : nextVersion);
            return ps;
        }, kh);

        Integer newId = kh.getKey() == null ? null : kh.getKey().intValue();

        if (request.baseTemplateId != null && newId != null) {
            String copySql = "INSERT INTO \"1scale\".field_definition(template_id, key, label, type, required, order_no, group_key, is_multi, multi_keys_json, unit_mode, unit_options_json, dropdown_options_json, table_schema_json) " +
                    "SELECT ?, key, label, type, required, order_no, group_key, is_multi, multi_keys_json, unit_mode, unit_options_json, dropdown_options_json, table_schema_json " +
                    "FROM \"1scale\".field_definition WHERE template_id = ?";
            jdbcTemplate.update(copySql, newId, request.baseTemplateId);
        }

        return findTemplateById(newId);
    }

    @Override
    public TemplateDTO saveFieldsAsNewVersion(SaveFieldsVersionRequestDTO request) {
        TemplateDTO current = jdbcTemplate.queryForObject(
                "SELECT id, code, name, kind, version FROM \"1scale\".sheet_template WHERE id = ?",
                (rs, i) -> mapTemplate(rs),
                request.templateId
        );

        CreateTemplateVersionRequestDTO versionReq = new CreateTemplateVersionRequestDTO();
        versionReq.baseTemplateId = null;
        versionReq.code = current.code;
        versionReq.name = current.name;
        versionReq.kind = current.kind;

        TemplateDTO created = createTemplateVersion(versionReq);

        final String insertFieldSql = "INSERT INTO \"1scale\".field_definition(template_id, key, label, type, required, order_no, group_key, is_multi, multi_keys_json, unit_mode, unit_options_json, dropdown_options_json, table_schema_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?::jsonb, ?::jsonb, ?::jsonb)";

        for (int i = 0; i < request.fields.size(); i++) {
            FieldDefinitionDTO f = request.fields.get(i);
            jdbcTemplate.update(insertFieldSql,
                    created.id,
                    f.key,
                    f.label,
                    f.type,
                    Boolean.TRUE.equals(f.required),
                    (i + 1) * 10,
                    f.groupKey,
                    Boolean.TRUE.equals(f.isMulti),
                    writeJson(f.multiKeys),
                    f.unitMode,
                    writeJson(f.unitOptions),
                    writeJson(f.dropdownOptions),
                    writeJson(f.tableSchema)
            );
        }

        return created;
    }

    private TemplateDTO mapTemplate(java.sql.ResultSet rs) throws java.sql.SQLException {
        TemplateDTO t = new TemplateDTO();
        t.id = rs.getInt("id");
        t.code = rs.getString("code");
        t.name = rs.getString("name");
        t.kind = rs.getString("kind");
        t.version = rs.getInt("version");
        return t;
    }

    private TemplateDTO findTemplateById(Integer id) {
        if (id == null) return null;
        List<TemplateDTO> list = jdbcTemplate.query(
                "SELECT id, code, name, kind, version FROM \"1scale\".sheet_template WHERE id = ?",
                new Object[]{id},
                (rs, i) -> mapTemplate(rs)
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private List<String> readStringList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            throw new RuntimeException("Could not parse json list", e);
        }
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o == null ? Collections.emptyList() : o);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize object to json", e);
        }
    }
}

