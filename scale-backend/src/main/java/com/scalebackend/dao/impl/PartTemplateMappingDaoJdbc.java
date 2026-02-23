package com.scalebackend.dao.impl;

import com.scalebackend.dao.PartTemplateMappingDao;
import com.scalebackend.dto.PartTemplateMappingDTO;
import com.scalebackend.dto.TemplatesForPartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class PartTemplateMappingDaoJdbc implements PartTemplateMappingDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    public List<PartTemplateMappingDTO> listMappings() {
        final String sql = "SELECT part_id, input_template_ids, output_template_ids, updated_at::text AS updated_at FROM \"1scale\".part_template_mapping ORDER BY part_id";
        return jdbcTemplate.query(sql, (rs, i) -> {
            PartTemplateMappingDTO dto = new PartTemplateMappingDTO();
            dto.partId = rs.getString("part_id");
            dto.inputTemplateIds = toIntegerList(rs.getArray("input_template_ids"));
            dto.outputTemplateIds = toIntegerList(rs.getArray("output_template_ids"));
            dto.updatedAt = rs.getString("updated_at");
            return dto;
        });
    }

    @Override
    public TemplatesForPartDTO getTemplatesForPart(String partId) {
        final String sql = "SELECT input_template_ids, output_template_ids FROM \"1scale\".part_template_mapping WHERE part_id = ?";
        List<TemplatesForPartDTO> list = jdbcTemplate.query(sql, new Object[]{partId}, (rs, i) -> {
            TemplatesForPartDTO dto = new TemplatesForPartDTO();
            dto.inputTemplateIds = toIntegerList(rs.getArray("input_template_ids"));
            dto.outputTemplateIds = toIntegerList(rs.getArray("output_template_ids"));
            return dto;
        });
        if (list.isEmpty()) {
            return new TemplatesForPartDTO();
        }
        return list.get(0);
    }

    @Override
    public PartTemplateMappingDTO saveMapping(PartTemplateMappingDTO mapping) {
        final String sql = "INSERT INTO \"1scale\".part_template_mapping(part_id, input_template_ids, output_template_ids, updated_at) " +
                "VALUES (?, ?, ?, NOW()) " +
                "ON CONFLICT (part_id) DO UPDATE SET input_template_ids = EXCLUDED.input_template_ids, output_template_ids = EXCLUDED.output_template_ids, updated_at = NOW()";

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, mapping.partId);
            ps.setArray(2, createIntArray(conn, mapping.inputTemplateIds));
            ps.setArray(3, createIntArray(conn, mapping.outputTemplateIds));
            return ps;
        });

        final String select = "SELECT part_id, input_template_ids, output_template_ids, updated_at::text AS updated_at FROM \"1scale\".part_template_mapping WHERE part_id = ?";
        return jdbcTemplate.queryForObject(select, new Object[]{mapping.partId}, (rs, i) -> {
            PartTemplateMappingDTO dto = new PartTemplateMappingDTO();
            dto.partId = rs.getString("part_id");
            dto.inputTemplateIds = toIntegerList(rs.getArray("input_template_ids"));
            dto.outputTemplateIds = toIntegerList(rs.getArray("output_template_ids"));
            dto.updatedAt = rs.getString("updated_at");
            return dto;
        });
    }

    private Array createIntArray(Connection conn, List<Integer> data) throws java.sql.SQLException {
        Integer[] arr = (data == null ? Collections.<Integer>emptyList() : data).toArray(new Integer[0]);
        return conn.createArrayOf("integer", arr);
    }

    private List<Integer> toIntegerList(Array sqlArray) throws java.sql.SQLException {
        if (sqlArray == null) {
            return new ArrayList<>();
        }
        Object raw = sqlArray.getArray();
        if (raw instanceof Integer[]) {
            Integer[] vals = (Integer[]) raw;
            List<Integer> result = new ArrayList<>();
            Collections.addAll(result, vals);
            return result;
        }
        if (raw instanceof Object[]) {
            Object[] vals = (Object[]) raw;
            List<Integer> result = new ArrayList<>();
            for (Object o : vals) {
                if (o != null) {
                    result.add(Integer.valueOf(String.valueOf(o)));
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}

