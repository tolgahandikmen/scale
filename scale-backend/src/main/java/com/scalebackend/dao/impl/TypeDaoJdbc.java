package com.scalebackend.dao.impl;

import com.scalebackend.dao.TypeDao;
import com.scalebackend.dto.TypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

@Repository
public class TypeDaoJdbc implements TypeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public TypeDTO insert(TypeDTO dto) {
        final String sql = "INSERT INTO \"1scale\".type(name, is_active) VALUES (?, COALESCE(?, TRUE))";
        KeyHolder kh = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.name == null ? null : dto.name.trim());
            if (dto.active == null) {
                ps.setNull(2, java.sql.Types.BOOLEAN);
            } else {
                ps.setBoolean(2, dto.active);
            }
            return ps;
        }, kh);

        if (kh.getKey() != null) {
            dto.id = kh.getKey().intValue();
        }
        if (dto.active == null) {
            dto.active = true;
        }
        return dto;
    }
}

