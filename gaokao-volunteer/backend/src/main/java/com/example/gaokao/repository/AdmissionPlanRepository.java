package com.example.gaokao.repository;

import com.example.gaokao.entity.AdmissionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionPlanRepository extends JpaRepository<AdmissionPlan, Long> {

    List<AdmissionPlan> findByProvinceAndYear(String province, String year);

    List<AdmissionPlan> findByCollegeId(Long collegeId);

    @Query("SELECT a FROM AdmissionPlan a WHERE " +
           "(:province IS NULL OR a.province = :province) AND " +
           "(:year IS NULL OR a.year = :year) AND " +
           "(:batch IS NULL OR a.batch = :batch) AND " +
           "(:scienceOrArts IS NULL OR a.scienceOrArts = :scienceOrArts) AND " +
           "(:collegeId IS NULL OR a.college.id = :collegeId)")
    Page<AdmissionPlan> searchWithFilters(
            @Param("province") String province,
            @Param("year") String year,
            @Param("batch") AdmissionPlan.Batch batch,
            @Param("scienceOrArts") String scienceOrArts,
            @Param("collegeId") Long collegeId,
            Pageable pageable);
}
