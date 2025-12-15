package com.java.sms.repository;


import com.java.sms.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    // Find all fee structures that reference a given yearLevelId (in yearLevelIds collection)
    @Query("select f from FeeStructure f join f.yearLevelIds y where y = :yearLevelId")
    List<FeeStructure> findByYearLevelId(@Param("yearLevelId") Long yearLevelId);

    List<FeeStructure> findByMasterFeeId(Long masterFeeId);
}

