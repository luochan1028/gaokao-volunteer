
package com.example.gaokao.repository;

import com.example.gaokao.entity.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {

    List<Major> findByCollegeId(Long collegeId);

    List<Major> findByCategory(Major.MajorCategory category);

    List<Major> findByIsHotTrue();

    List<Major> findByIsNationalKeyTrue();

    @Query("SELECT m FROM Major m WHERE m.name LIKE %:keyword% OR m.subject LIKE %:keyword%")
    Page<Major> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Major m WHERE " +
            "(:name IS NULL OR m.name LIKE %:name%) AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:collegeId IS NULL OR m.college.id = :collegeId) AND " +
            "(:isHot IS NULL OR m.isHot = :isHot) AND " +
            "(:isNationalKey IS NULL OR m.isNationalKey = :isNationalKey)")
    Page<Major> searchWithFilters(
            @Param("name") String name,
            @Param("category") Major.MajorCategory category,
            @Param("collegeId") Long collegeId,
            @Param("isHot") Boolean isHot,
            @Param("isNationalKey") Boolean isNationalKey,
            Pageable pageable);

    @Query("SELECT DISTINCT m.category FROM Major m ORDER BY m.category")
    List<String> findAllCategories();

    @Query("SELECT m FROM Major m WHERE m.subjectRequirements LIKE %:subject%")
    List<Major> findBySubjectRequirement(@Param("subject") String subject);
}
