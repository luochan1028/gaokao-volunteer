
package com.example.gaokao.repository;

import com.example.gaokao.entity.College;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByName(String name);

    List<College> findByProvince(String province);

    List<College> findByType(College.CollegeType type);

    List<College> findByIs985True();

    List<College> findByIs211True();

    List<College> findByIsDoubleFirstClassTrue();

    @Query("SELECT c FROM College c WHERE c.name LIKE %:keyword% OR c.shortName LIKE %:keyword%")
    Page<College> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM College c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:province IS NULL OR c.province = :province) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:level IS NULL OR c.level = :level) AND " +
            "(:is985 IS NULL OR c.is985 = :is985) AND " +
            "(:is211 IS NULL OR c.is211 = :is211) AND " +
            "(:isDoubleFirstClass IS NULL OR c.isDoubleFirstClass = :isDoubleFirstClass)")
    Page<College> searchWithFilters(
            @Param("name") String name,
            @Param("province") String province,
            @Param("type") College.CollegeType type,
            @Param("level") College.CollegeLevel level,
            @Param("is985") Boolean is985,
            @Param("is211") Boolean is211,
            @Param("isDoubleFirstClass") Boolean isDoubleFirstClass,
            Pageable pageable);

    @Query("SELECT DISTINCT c.province FROM College c ORDER BY c.province")
    List<String> findAllProvinces();
}
