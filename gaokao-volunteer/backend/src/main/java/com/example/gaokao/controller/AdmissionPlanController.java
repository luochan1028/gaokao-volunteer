package com.example.gaokao.controller;

import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.entity.AdmissionPlan;
import com.example.gaokao.repository.AdmissionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/admission-plans")
@RequiredArgsConstructor
public class AdmissionPlanController {

    private final AdmissionPlanRepository admissionPlanRepository;

    @GetMapping
    public ApiResponse<Map<String, Object>> searchPlans(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) AdmissionPlan.Batch batch,
            @RequestParam(required = false) String scienceOrArts,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) String majorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("college.name").ascending());
        Page<AdmissionPlan> plans = admissionPlanRepository.searchWithFilters(
                province, year, batch, scienceOrArts, collegeId, pageable);

        List<Map<String, Object>> content = plans.getContent().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("year", p.getYear());
            map.put("province", p.getProvince());
            map.put("batch", p.getBatch());
            map.put("scienceOrArts", p.getScienceOrArts());
            map.put("collegeCode", p.getCollegeCode());
            map.put("majorCode", p.getMajorCode());
            map.put("majorName", p.getMajorName());
            map.put("enrollmentCount", p.getEnrollmentCount());
            map.put("schoolSystem", p.getSchoolSystem());
            map.put("tuition", p.getTuition());
            map.put("subjectRequirements", p.getSubjectRequirements());
            map.put("isSimulated", p.getIsSimulated());
            if (p.getCollege() != null) {
                Map<String, Object> college = new HashMap<>();
                college.put("id", p.getCollege().getId());
                college.put("name", p.getCollege().getName());
                college.put("province", p.getCollege().getProvince());
                college.put("city", p.getCollege().getCity());
                college.put("is985", p.getCollege().getIs985());
                college.put("is211", p.getCollege().getIs211());
                map.put("college", college);
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalElements", plans.getTotalElements());
        result.put("totalPages", plans.getTotalPages());
        result.put("page", page);
        result.put("size", size);
        result.put("simulated", true);

        return ApiResponse.success(result);
    }

    @GetMapping("/batches")
    public ApiResponse<List<String>> getBatches() {
        return ApiResponse.success(List.of("BATCH_A", "BATCH_B", "BATCH_C", "SPECIAL_BATCH", "EARLY_BATCH"));
    }
}
