package com.example.gaokao.controller;

import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.entity.PolicyDocument;
import com.example.gaokao.repository.PolicyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyDocumentRepository policyDocumentRepository;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getPolicies(
            @RequestParam(required = false) String province) {

        List<PolicyDocument> docs;
        if (province != null && !province.isEmpty()) {
            docs = policyDocumentRepository.findByProvinceAndIsActiveTrue(province);
            if (docs.isEmpty()) {
                docs = policyDocumentRepository.findByIsActiveTrueOrderByPublishDateDesc();
            }
        } else {
            docs = policyDocumentRepository.findByIsActiveTrueOrderByPublishDateDesc();
        }

        List<Map<String, Object>> result = docs.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("title", d.getTitle());
            map.put("province", d.getProvince());
            map.put("policyType", d.getPolicyType());
            map.put("content", d.getContent());
            map.put("source", d.getSource());
            map.put("publishDate", d.getPublishDate());
            map.put("tags", d.getTags());
            return map;
        }).collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getPolicyById(@PathVariable Long id) {
        PolicyDocument doc = policyDocumentRepository.findById(id).orElse(null);
        if (doc == null) {
            return ApiResponse.error(404, "政策公告不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", doc.getId());
        map.put("title", doc.getTitle());
        map.put("province", doc.getProvince());
        map.put("policyType", doc.getPolicyType());
        map.put("content", doc.getContent());
        map.put("source", doc.getSource());
        map.put("publishDate", doc.getPublishDate());
        map.put("tags", doc.getTags());
        return ApiResponse.success(map);
    }
}
