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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final VolunteerListRepository volunteerListRepository;
    private final VolunteerPlanRepository volunteerPlanRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/assess")
    public ApiResponse<Map<String, Object>> assessRisk(
            @RequestParam(required = false) Long planId,
            HttpServletRequest request) {

        User user = getUserFromRequest(request);
        if (user == null) return ApiResponse.error(401, "未登录");

        List<VolunteerList> items;
        if (planId != null) {
            VolunteerPlan plan = volunteerPlanRepository.findById(planId).orElse(null);
            if (plan == null) return ApiResponse.error(404, "方案不存在");
            items = volunteerListRepository.findByUserIdOrderByPriority(user.getId());
        } else {
            items = volunteerListRepository.findByUserIdOrderByPriority(user.getId());
        }

        long total = items.size();
        long chongCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.CHONG).count();
        long wenCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.WEN).count();
        long baoCount = items.stream().filter(i -> i.getGradientType() == VolunteerList.GradientType.BAO).count();

        double chongPct = total > 0 ? (double) chongCount / total * 100 : 0;
        double wenPct = total > 0 ? (double) wenCount / total * 100 : 0;
        double baoPct = total > 0 ? (double) baoCount / total * 100 : 0;

        List<Map<String, Object>> riskItems = new ArrayList<>();

        if (baoCount == 0) {
            riskItems.add(Map.of(
                "type", "SLIP", "level", "HIGH", "title", "滑档风险",
                "description", "没有保底志愿，如果冲刺志愿全部落空将导致滑档",
                "suggestion", "建议至少添加2-3个录取概率85%以上的保底院校"
            ));
        } else if (baoCount < 2) {
            riskItems.add(Map.of(
                "type", "SLIP", "level", "MEDIUM", "title", "保底不足",
                "description", "保底志愿仅" + baoCount + "个，保障力度不够",
                "suggestion", "建议增加保底志愿至总志愿的30%"
            ));
        }

        if (chongPct > 60) {
            riskItems.add(Map.of(
                "type", "GRADIENT", "level", "HIGH", "title", "梯度失衡",
                "description", String.format("冲的志愿占%.0f%%，比例过高", chongPct),
                "suggestion", "建议调整为冲20%·稳50%·保30%的健康梯度"
            ));
        } else if (chongPct > 50) {
            riskItems.add(Map.of(
                "type", "GRADIENT", "level", "MEDIUM", "title", "冲刺偏多",
                "description", String.format("冲的志愿占%.0f%%，略偏高", chongPct),
                "suggestion", "建议适当增加稳和保的志愿比例"
            ));
        }

        Set<String> seen = new HashSet<>();
        for (VolunteerList item : items) {
            String key = (item.getCollege() != null ? item.getCollege().getId() : "0") + "-" +
                         (item.getMajor() != null ? item.getMajor().getId() : "0");
            if (!seen.add(key)) {
                riskItems.add(Map.of(
                    "type", "DUPLICATE", "level", "MEDIUM", "title", "重复志愿",
                    "description", "检测到重复的院校专业组合",
                    "suggestion", "请删除重复志愿，每个院校专业组合只需保留一个"
                ));
                break;
            }
        }

        if (user.getSelectedSubjects() != null && !user.getSelectedSubjects().isEmpty()) {
            for (VolunteerList item : items) {
                if (item.getMajor() != null && item.getMajor().getSubjectRequirements() != null) {
                    String req = item.getMajor().getSubjectRequirements();
                    String selected = user.getSelectedSubjects();
                    if (req.contains("物理") && !selected.contains("物理")) {
                        riskItems.add(Map.of(
                            "type", "SUBJECT", "level", "HIGH", "title", "选科不符",
                            "description", item.getMajor().getName() + "要求选考物理，但您未选考物理",
                            "suggestion", "请删除此志愿或更换为符合选科要求的专业"
                        ));
                    }
                }
            }
        }

        String overallLevel;
        long highCount = riskItems.stream().filter(r -> "HIGH".equals(r.get("level"))).count();
        long mediumCount = riskItems.stream().filter(r -> "MEDIUM".equals(r.get("level"))).count();
        if (highCount > 0) {
            overallLevel = "HIGH";
        } else if (mediumCount > 0) {
            overallLevel = "MEDIUM";
        } else if (total > 0) {
            overallLevel = "LOW";
        } else {
            overallLevel = "EMPTY";
        }

        Map<String, Object> gradient = new LinkedHashMap<>();
        gradient.put("chong", chongCount);
        gradient.put("wen", wenCount);
        gradient.put("bao", baoCount);
        gradient.put("chongPct", Math.round(chongPct));
        gradient.put("wenPct", Math.round(wenPct));
        gradient.put("baoPct", Math.round(baoPct));
        gradient.put("ideal", Map.of("chong", 20, "wen", 50, "bao", 30));

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("overallLevel", overallLevel);
        dashboard.put("totalVolunteers", total);
        dashboard.put("riskCount", riskItems.size());
        dashboard.put("highRiskCount", highCount);
        dashboard.put("mediumRiskCount", mediumCount);
        dashboard.put("slipProbability", overallLevel.equals("HIGH") ? 75 : (overallLevel.equals("MEDIUM") ? 40 : 10));
        dashboard.put("gradient", gradient);
        dashboard.put("riskItems", riskItems);
        dashboard.put("suggestions", riskItems.stream()
                .map(r -> r.get("suggestion")).collect(Collectors.toList()));

        return ApiResponse.success(dashboard);
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
