package com.poc.alerts.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VariableMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public VariableMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String,String> getVariablePaths(List<String> variables){

        String sql = """
            SELECT variable_name,json_path
            FROM template_variable_mapping
            WHERE variable_name IN (:variables)
            """;

        Map<String,String> result = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {

            result.put(
                    rs.getString("variable_name"),
                    rs.getString("json_path")
            );

        });

        return result;
    }
}