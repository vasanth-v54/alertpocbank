package com.template.repository;

import com.template.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateMaster, Long> {

    List<TemplateMaster> findByTemplateIdAndIsActive(String templateId, String isActive);

    List<TemplateMaster> findByIsActiveTrue();

}