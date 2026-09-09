package com.example.gaokao.controller;

import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.entity.User;
import com.example.gaokao.entity.VolunteerList;
import com.example.gaokao.entity.VolunteerPlan;
import com.example.gaokao.repository.UserRepository;
import com.example.gaokao.repository.VolunteerListRepository;
import com.example.gaokao.repository.VolunteerPlanRepository;
import com.example.gaokao.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final VolunteerPlanRepository volunteerPlanRepository;
    private final VolunteerListRepository volunteerListRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/check/{planId}")
    public ApiResponse<Map<String, Object>> preCheck(@PathVariable Long planId, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user == null) return ApiResponse.error(401, "未登录");

        VolunteerPlan plan = volunteerPlanRepository.findById(planId).orElse(null);
        if (plan == null) return ApiResponse.error(404, "方案不存在");

        List<VolunteerList> items = volunteerListRepository.findByUserIdOrderByPriority(user.getId());

        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        if (items.isEmpty()) {
            errors.add(Map.of("type", "EMPTY", "message", "志愿表为空，无法提交"));
        }

        long chongCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.CHONG).count();
        long wenCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.WEN).count();
        long baoCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.BAO).count();

        if (baoCount == 0) {
            warnings.add(Map.of("type", "NO_SAFETY", "message", "没有保底志愿，建议至少添加2个保底院校"));
        }
        if (chongCount > wenCount + baoCount) {
            warnings.add(Map.of("type", "TOO_AGGRESSIVE", "message", "冲的志愿过多，滑档风险较高"));
        }

        Set<String> seen = new HashSet<>();
        for (VolunteerList item : items) {
            String key = (item.getCollege() != null ? item.getCollege().getId() : "") + "-" +
                         (item.getMajor() != null ? item.getMajor().getId() : "");
            if (!seen.add(key)) {
                warnings.add(Map.of("type", "DUPLICATE", "message",
                    "存在重复志愿：" + (item.getCollege() != null ? item.getCollege().getName() : "") +
                    " " + (item.getMajor() != null ? item.getMajor().getName() : "")));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("canSubmit", errors.isEmpty());
        result.put("warnings", warnings);
        result.put("errors", errors);
        result.put("totalItems", items.size());
        result.put("gradient", Map.of("chong", chongCount, "wen", wenCount, "bao", baoCount));

        return ApiResponse.success(result);
    }

    @PostMapping("/submit/{planId}")
    public ApiResponse<Map<String, Object>> submit(
            @PathVariable Long planId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        User user = getUserFromRequest(request);
        if (user == null) return ApiResponse.error(401, "未登录");

        VolunteerPlan plan = volunteerPlanRepository.findById(planId).orElse(null);
        if (plan == null) return ApiResponse.error(404, "方案不存在");

        String verifyCode = (String) body.getOrDefault("verifyCode", "");
        if (!"123456".equals(verifyCode)) {
            return ApiResponse.error(400, "验证码错误，请输入正确的验证码");
        }

        int submitCount = plan.getChongCount() + plan.getWenCount() + plan.getBaoCount();
        String submitNo = "GAOKAO-2026-" + System.currentTimeMillis() % 1000000;
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> result = new HashMap<>();
        result.put("submitNo", submitNo);
        result.put("submitTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("planName", plan.getName());
        result.put("totalVolunteers", submitCount);
        result.put("studentName", user.getName());
        result.put("studentPhone", user.getPhone());
        result.put("admissionTicket", "SC2026" + String.format("%06d", user.getId()));
        result.put("remainingSubmissions", 2);
        result.put("status", "SUBMITTED");

        return ApiResponse.success("志愿提交成功", result);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring(7);
        String phone = jwtUtil.extractUsername(token);
        if (phone == null) return null;
        return userRepository.findByPhone(phone).orElse(null);
    }
}
