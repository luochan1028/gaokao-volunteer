
package com.example.gaokao.service.impl;

import com.example.gaokao.entity.College;
import com.example.gaokao.entity.EnrollmentScore;
import com.example.gaokao.entity.Major;
import com.example.gaokao.repository.CollegeRepository;
import com.example.gaokao.repository.EnrollmentScoreRepository;
import com.example.gaokao.repository.MajorRepository;
import com.example.gaokao.service.CollegeMajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeMajorServiceImpl implements CollegeMajorService {

    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final EnrollmentScoreRepository enrollmentScoreRepository;

    @Override
    public Page<College> searchColleges(String name, String province, College.CollegeType type,
                                         College.CollegeLevel level, Boolean is985, Boolean is211,
                                         Boolean isDoubleFirstClass, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "satisfactionScore"));
        return collegeRepository.searchWithFilters(name, province, type, level, is985, is211, isDoubleFirstClass, pageable);
    }

    @Override
    public College getCollegeById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("院校不存在"));
    }

    @Override
    public List<Major> getMajorsByCollege(Long collegeId) {
        return majorRepository.findByCollegeId(collegeId);
    }

    @Override
    public Page<Major> searchMajors(String name, Major.MajorCategory category, Long collegeId,
                                     Boolean isHot, Boolean isNationalKey, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "satisfactionScore"));
        return majorRepository.searchWithFilters(name, category, collegeId, isHot, isNationalKey, pageable);
    }

    @Override
    public Major getMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("专业不存在"));
    }

    @Override
    public List<EnrollmentScore> getEnrollmentScores(Long collegeId, String year) {
        return enrollmentScoreRepository.findByCollegeIdAndYear(collegeId, year);
    }

    @Override
    public List<EnrollmentScore> getScoresByProvince(String province, String year, String scienceOrArts) {
        EnrollmentScore.ScienceOrArts sa = EnrollmentScore.ScienceOrArts.valueOf(scienceOrArts.toUpperCase());
        return enrollmentScoreRepository.findByYearAndProvinceAndScienceOrArts(year, province, sa);
    }

    @Override
    public List<String> getAllProvinces() {
        return collegeRepository.findAllProvinces();
    }

    @Override
    public List<String> getAllYears() {
        return enrollmentScoreRepository.findAllYears();
    }

    @Override
    public List<Map<String, Object>> compareColleges(List<Long> collegeIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long id : collegeIds) {
            College college = collegeRepository.findById(id).orElse(null);
            if (college != null) {
                Map<String, Object> collegeInfo = new LinkedHashMap<>();
                collegeInfo.put("id", college.getId());
                collegeInfo.put("name", college.getName());
                collegeInfo.put("province", college.getProvince());
                collegeInfo.put("city", college.getCity());
                collegeInfo.put("type", college.getType());
                collegeInfo.put("level", college.getLevel());
                collegeInfo.put("is985", college.getIs985());
                collegeInfo.put("is211", college.getIs211());
                collegeInfo.put("isDoubleFirstClass", college.getIsDoubleFirstClass());
                collegeInfo.put("satisfactionScore", college.getSatisfactionScore());
                collegeInfo.put("studentCount", college.getStudentCount());
                collegeInfo.put("teacherCount", college.getTeacherCount());
                collegeInfo.put("hasGraduateProgram", college.getHasGraduateProgram());
                result.add(collegeInfo);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> compareMajors(List<Long> majorIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long id : majorIds) {
            Major major = majorRepository.findById(id).orElse(null);
            if (major != null) {
                Map<String, Object> majorInfo = new LinkedHashMap<>();
                majorInfo.put("id", major.getId());
                majorInfo.put("name", major.getName());
                majorInfo.put("category", major.getCategory());
                majorInfo.put("subject", major.getSubject());
                majorInfo.put("collegeName", major.getCollege() != null ? major.getCollege().getName() : null);
                majorInfo.put("satisfactionScore", major.getSatisfactionScore());
                majorInfo.put("averageSalary", major.getAverageSalary());
                majorInfo.put("employmentRate", major.getEmploymentRate());
                majorInfo.put("isHot", major.getIsHot());
                majorInfo.put("isNationalKey", major.getIsNationalKey());
                result.add(majorInfo);
            }
        }
        return result;
    }
}
