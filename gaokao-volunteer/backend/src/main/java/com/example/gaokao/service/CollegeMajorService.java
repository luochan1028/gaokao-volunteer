
package com.example.gaokao.service;

import com.example.gaokao.entity.College;
import com.example.gaokao.entity.EnrollmentScore;
import com.example.gaokao.entity.Major;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CollegeMajorService {

    Page<College> searchColleges(String name, String province, College.CollegeType type,
                                  College.CollegeLevel level, Boolean is985, Boolean is211,
                                  Boolean isDoubleFirstClass, int page, int size);

    College getCollegeById(Long id);

    List<Major> getMajorsByCollege(Long collegeId);

    Page<Major> searchMajors(String name, Major.MajorCategory category, Long collegeId,
                              Boolean isHot, Boolean isNationalKey, int page, int size);

    Major getMajorById(Long id);

    List<EnrollmentScore> getEnrollmentScores(Long collegeId, String year);

    List<EnrollmentScore> getScoresByProvince(String province, String year, String scienceOrArts);

    List<String> getAllProvinces();

    List<String> getAllYears();

    List<Map<String, Object>> compareColleges(List<Long> collegeIds);

    List<Map<String, Object>> compareMajors(List<Long> majorIds);
}
