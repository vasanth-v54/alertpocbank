package com.template.repository;

import com.template.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {


    @Modifying
    @Query("""
    UPDATE TemplateMaster t 
    SET t.isActive = false,
        t.modifiedBy = :modifiedBy,
        t.modifiedDate = CURRENT_TIMESTAMP
    WHERE t.id = :id
      AND t.isActive = true
""")
    int deactivateById(@Param("id") Long id,
                       @Param("modifiedBy") String modifiedBy);

    @Modifying
    @Query("""
    UPDATE TemplateMaster t 
    SET t.isActive = true,
        t.modifiedBy = :modifiedBy,
        t.modifiedDate = CURRENT_TIMESTAMP
    WHERE t.id = :id
      AND t.isActive = false
""")
    int activateById(@Param("id") Long id,
                     @Param("modifiedBy") String modifiedBy);


    @Modifying
    @Query("""
    UPDATE TemplateMaster t 
    SET t.isActive = false,
        t.modifiedBy = :modifiedBy,
        t.modifiedDate = CURRENT_TIMESTAMP
    WHERE t.templateName = :templateName 
      AND t.messageType = :messageType
      AND t.isActive = true
""")
    int deactivateOthersByTemplateNameAndMessageType(@Param("templateName") String templateName,
                                                     @Param("messageType") TemplateMaster.Channel messageType,
                                                     @Param("modifiedBy") String modifiedBy);

    List<TemplateMaster> findAllByTemplateNameAndMessageTypeAndIsActive(String templateName, TemplateMaster.Channel messageType, Boolean isActive);
}
