package com.example.gaokao.repository;

import com.example.gaokao.entity.SchoolMajorGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolMajorGroupRepository extends JpaRepository<SchoolMajorGroup, Long> {
    List<SchoolMajorGroup> findBySchoolProvinceAndBatch(String province, String batch);
}
