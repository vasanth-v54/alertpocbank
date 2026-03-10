package com.poc.alerts.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class TemplateMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public TemplateMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getTemplateParams(String template_id) {

        String sql =
        "SELECT template_params FROM event_template_mapping WHERE template_id=?";

        String json = jdbcTemplate.queryForObject(sql,String.class,template_id);

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json,new TypeReference<List<String>>() {});
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}