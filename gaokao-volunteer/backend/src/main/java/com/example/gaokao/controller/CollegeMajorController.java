
package com.example.gaokao.controller;

import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.entity.College;
import com.example.gaokao.entity.EnrollmentScore;
import com.example.gaokao.entity.Major;
import com.example.gaokao.service.CollegeMajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/colleges")
@RequiredArgsConstructor
@Slf4j
public class CollegeMajorController {

    private final CollegeMajorService collegeMajorService;

    @GetMapping
    public ApiResponse<Page<College>> searchColleges(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) College.CollegeType type,
            @RequestParam(required = false) College.CollegeLevel level,
            @RequestParam(required = false) Boolean is985,
            @RequestParam(required = false) Boolean is211,
            @RequestParam(required = false) Boolean isDoubleFirstClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<College> colleges = collegeMajorService.searchColleges(name, province, type, level, is985, is211, isDoubleFirstClass, page, size);
        return ApiResponse.success(colleges);
    }

    @GetMapping("/{id}")
    public ApiResponse<College> getCollegeById(@PathVariable Long id) {
        College college = collegeMajorService.getCollegeById(id);
        return ApiResponse.success(college);
    }

    @GetMapping("/{id}/majors")
    public ApiResponse<List<Major>> getMajorsByCollege(@PathVariable Long id) {
        List<Major> majors = collegeMajorService.getMajorsByCollege(id);
        return ApiResponse.success(majors);
    }

    @GetMapping("/{id}/scores")
    public ApiResponse<List<EnrollmentScore>> getEnrollmentScores(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2024") String year) {
        List<EnrollmentScore> scores = collegeMajorService.getEnrollmentScores(id, year);
        return ApiResponse.success(scores);
    }

    @GetMapping("/provinces")
    public ApiResponse<List<String>> getAllProvinces() {
        List<String> provinces = collegeMajorService.getAllProvinces();
        return ApiResponse.success(provinces);
    }

    @GetMapping("/years")
    public ApiResponse<List<String>> getAllYears() {
        List<String> years = collegeMajorService.getAllYears();
        return ApiResponse.success(years);
    }

    @PostMapping("/compare")
    public ApiResponse<List<Map<String, Object>>> compareColleges(@RequestBody List<Long> collegeIds) {
        List<Map<String, Object>> result = collegeMajorService.compareColleges(collegeIds);
        return ApiResponse.success(result);
    }

    @GetMapping("/public/majors")
    public ApiResponse<Page<Major>> searchMajors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Major.MajorCategory category,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Boolean isHot,
            @RequestParam(required = false) Boolean isNationalKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Major> majors = collegeMajorService.searchMajors(name, category, collegeId, isHot, isNationalKey, page, size);
        return ApiResponse.success(majors);
    }

    @GetMapping("/public/majors/{id}")
    public ApiResponse<Major> getMajorById(@PathVariable Long id) {
        Major major = collegeMajorService.getMajorById(id);
        return ApiResponse.success(major);
    }

    @PostMapping("/public/majors/compare")
    public ApiResponse<List<Map<String, Object>>> compareMajors(@RequestBody List<Long> majorIds) {
        List<Map<String, Object>> result = collegeMajorService.compareMajors(majorIds);
        return ApiResponse.success(result);
    }

    @GetMapping("/public/scores")
    public ApiResponse<List<EnrollmentScore>> getScoresByProvince(
            @RequestParam String province,
            @RequestParam(defaultValue = "2024") String year,
            @RequestParam(defaultValue = "SCIENCE") String scienceOrArts) {
        List<EnrollmentScore> scores = collegeMajorService.getScoresByProvince(province, year, scienceOrArts);
        return ApiResponse.success(scores);
    }
}
