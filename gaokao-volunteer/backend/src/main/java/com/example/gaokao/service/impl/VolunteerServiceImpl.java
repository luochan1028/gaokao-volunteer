
package com.example.gaokao.service.impl;

import com.example.gaokao.entity.*;
import com.example.gaokao.repository.CollegeRepository;
import com.example.gaokao.repository.MajorRepository;
import com.example.gaokao.repository.VolunteerListRepository;
import com.example.gaokao.repository.VolunteerPlanRepository;
import com.example.gaokao.service.RecommendationService;
import com.example.gaokao.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerListRepository volunteerListRepository;
    private final VolunteerPlanRepository volunteerPlanRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final RecommendationService recommendationService;

    @Override
    @Transactional
    public VolunteerList addToVolunteerList(User user, Long collegeId, Long majorId, String name) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("院校不存在"));

        Major major = null;
        if (majorId != null) {
            major = majorRepository.findById(majorId)
                    .orElseThrow(() -> new RuntimeException("专业不存在"));
        }

        VolunteerList volunteerList = VolunteerList.builder()
                .user(user)
                .college(college)
                .major(major)
                .name(name != null ? name : college.getName() + (major != null ? "-" + major.getName() : ""))
                .isSelected(true)
                .build();

        return volunteerListRepository.save(volunteerList);
    }

    @Override
    @Transactional
    public void removeFromVolunteerList(Long id) {
        if (!volunteerListRepository.existsById(id)) {
            throw new RuntimeException("志愿项不存在");
        }
        volunteerListRepository.deleteById(id);
    }

    @Override
    public List<VolunteerList> getUserVolunteerList(Long userId) {
        return volunteerListRepository.findByUserIdOrderByPriority(userId);
    }

    @Override
    @Transactional
    public VolunteerPlan savePlan(User user, String name, String description, String selectionMode,
                                   List<Long> volunteerListIds) {
        VolunteerPlan plan = VolunteerPlan.builder()
                .user(user)
                .name(name)
                .description(description)
                .selectionMode(VolunteerPlan.SelectionMode.valueOf(selectionMode.toUpperCase()))
                .totalCount(volunteerListIds.size())
                .build();

        plan = volunteerPlanRepository.save(plan);

        List<PlanItem> planItems = new ArrayList<>();
        int order = 1;
        for (Long vlId : volunteerListIds) {
            VolunteerList vl = volunteerListRepository.findById(vlId).orElse(null);
            if (vl != null) {
                PlanItem item = PlanItem.builder()
                        .plan(plan)
                        .volunteerList(vl)
                        .itemOrder(order++)
                        .gradientType(vl.getGradientType())
                        .probability(vl.getProbability())
                        .build();
                planItems.add(item);
            }
        }
        plan.setPlanItems(planItems);
        plan = volunteerPlanRepository.save(plan);

        calculatePlanStats(plan);

        return plan;
    }

    @Override
    @Transactional
    public VolunteerPlan updatePlan(Long planId, String name, String description) {
        VolunteerPlan plan = volunteerPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("志愿方案不存在"));

        plan.setName(name);
        plan.setDescription(description);

        return volunteerPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        if (!volunteerPlanRepository.existsById(planId)) {
            throw new RuntimeException("志愿方案不存在");
        }
        volunteerPlanRepository.deleteById(planId);
    }

    @Override
    public List<VolunteerPlan> getUserPlans(Long userId) {
        return volunteerPlanRepository.findByUserId(userId);
    }

    @Override
    public VolunteerPlan getPlanById(Long planId) {
        return volunteerPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("志愿方案不存在"));
    }

    @Override
    public String generateReport(User user, VolunteerPlan plan) {
        StringBuilder report = new StringBuilder();

        report.append("========== 我的志愿报告 ==========\n\n");
        report.append("一、考生情况分析\n");
        report.append("----------------------------------\n");
        report.append("姓名：").append(user.getName() != null ? user.getName() : "未填写").append("\n");
        report.append("省份：").append(user.getProvince() != null ? user.getProvince() : "未填写").append("\n");
        report.append("考试年份：").append(user.getExamYear() != null ? user.getExamYear() : "未填写").append("\n");
        report.append("选考科目：").append(user.getSelectedSubjects() != null ? user.getSelectedSubjects() : "未填写").append("\n");
        report.append("总分：").append(user.getTotalScore() != null ? user.getTotalScore() : "未填写").append("\n");
        report.append("全省位次：").append(user.getRank() != null ? user.getRank() : "未填写").append("\n");
        report.append("科类：").append(user.getScienceOrArts() != null ? user.getScienceOrArts() : "未填写").append("\n\n");

        report.append("二、填报策略设计\n");
        report.append("----------------------------------\n");
        report.append("志愿方案名称：").append(plan.getName()).append("\n");
        report.append("筛选模式：").append(plan.getSelectionMode() == VolunteerPlan.SelectionMode.COLLEGE_FIRST ? "院校优先" : "专业优先").append("\n");
        report.append("方案描述：").append(plan.getDescription() != null ? plan.getDescription() : "无").append("\n\n");

        report.append("三、志愿表解读\n");
        report.append("----------------------------------\n");
        report.append("总志愿数：").append(plan.getTotalCount()).append("\n");
        report.append("冲：").append(plan.getChongCount() != null ? plan.getChongCount() : 0).append("个\n");
        report.append("稳：").append(plan.getWenCount() != null ? plan.getWenCount() : 0).append("个\n");
        report.append("保：").append(plan.getBaoCount() != null ? plan.getBaoCount() : 0).append("个\n\n");

        report.append("志愿详情：\n");
        for (PlanItem item : plan.getPlanItems()) {
            VolunteerList vl = item.getVolunteerList();
            if (vl != null && vl.getCollege() != null) {
                report.append(String.format("%2d. %s %s %s %s\n",
                        item.getItemOrder(),
                        vl.getCollege().getName(),
                        vl.getMajor() != null ? "-" + vl.getMajor().getName() : "",
                        vl.getGradientType() != null ? "(" + getGradientTypeName(vl.getGradientType()) + ")" : "",
                        vl.getProbability() != null ? "录取概率: " + String.format("%.1f%%", vl.getProbability() * 100) : ""));
            }
        }
        report.append("\n");

        report.append("四、风险提示\n");
        report.append("----------------------------------\n");
        report.append("滑档概率：").append(plan.getSlipProbability() != null ? String.format("%.1f%%", plan.getSlipProbability() * 100) : "未计算").append("\n");
        report.append("浪费分数指数：").append(plan.getWasteScoreIndex() != null ? String.format("%.1f%%", plan.getWasteScoreIndex() * 100) : "未计算").append("\n");
        report.append("风险等级：").append(plan.getRiskTips() != null ? plan.getRiskTips() : "未评估").append("\n\n");

        report.append("五、策略建议\n");
        report.append("----------------------------------\n");
        report.append(plan.getStrategySuggestion() != null ? plan.getStrategySuggestion() : "暂无建议").append("\n\n");

        report.append("===================================\n");
        report.append("生成时间：").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        report.append("===================================\n");

        return report.toString();
    }

    @Override
    @Transactional
    public VolunteerPlan copyPlan(Long planId, String newName) {
        VolunteerPlan original = volunteerPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("志愿方案不存在"));

        VolunteerPlan copy = VolunteerPlan.builder()
                .user(original.getUser())
                .name(newName)
                .description(original.getDescription())
                .selectionMode(original.getSelectionMode())
                .chongCount(original.getChongCount())
                .wenCount(original.getWenCount())
                .baoCount(original.getBaoCount())
                .totalCount(original.getTotalCount())
                .slipProbability(original.getSlipProbability())
                .wasteScoreIndex(original.getWasteScoreIndex())
                .analysisReport(original.getAnalysisReport())
                .riskTips(original.getRiskTips())
                .strategySuggestion(original.getStrategySuggestion())
                .isDefault(false)
                .build();

        copy = volunteerPlanRepository.save(copy);

        List<PlanItem> planItems = new ArrayList<>();
        for (PlanItem item : original.getPlanItems()) {
            PlanItem newItem = PlanItem.builder()
                    .plan(copy)
                    .volunteerList(item.getVolunteerList())
                    .itemOrder(item.getItemOrder())
                    .gradientType(item.getGradientType())
                    .probability(item.getProbability())
                    .build();
            planItems.add(newItem);
        }
        copy.setPlanItems(planItems);

        return volunteerPlanRepository.save(copy);
    }

    @Override
    @Transactional
    public void setDefaultPlan(Long planId) {
        VolunteerPlan plan = volunteerPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("志愿方案不存在"));

        List<VolunteerPlan> userPlans = volunteerPlanRepository.findByUserId(plan.getUser().getId());
        for (VolunteerPlan p : userPlans) {
            p.setIsDefault(false);
        }

        plan.setIsDefault(true);
        volunteerPlanRepository.saveAll(userPlans);
        volunteerPlanRepository.save(plan);
    }

    private void calculatePlanStats(VolunteerPlan plan) {
        int chongCount = 0, wenCount = 0, baoCount = 0;
        double slipProbability = 1.0;

        for (PlanItem item : plan.getPlanItems()) {
            if (item.getGradientType() == VolunteerList.GradientType.CHONG) {
                chongCount++;
            } else if (item.getGradientType() == VolunteerList.GradientType.WEN) {
                wenCount++;
            } else if (item.getGradientType() == VolunteerList.GradientType.BAO) {
                baoCount++;
                if (item.getProbability() != null) {
                    slipProbability *= (1 - item.getProbability());
                }
            }
        }

        slipProbability = Math.min(0.5, slipProbability);

        plan.setChongCount(chongCount);
        plan.setWenCount(wenCount);
        plan.setBaoCount(baoCount);
        plan.setSlipProbability(Math.round(slipProbability * 100.0) / 100.0);

        String riskLevel;
        String suggestions;
        if (slipProbability < 0.1 && baoCount >= 4) {
            riskLevel = "安全";
            suggestions = "志愿梯度设置合理，建议确认专业偏好后提交";
        } else if (slipProbability < 0.2 && baoCount >= 2) {
            riskLevel = "较安全";
            suggestions = "建议增加保底志愿数量，降低滑档风险";
        } else if (slipProbability < 0.4) {
            riskLevel = "中等风险";
            suggestions = "滑档风险较高，建议调整志愿梯度，增加保底院校";
        } else {
            riskLevel = "高风险";
            suggestions = "滑档风险极高，请大幅增加保底志愿或调整目标院校";
        }

        plan.setRiskTips(riskLevel);
        plan.setStrategySuggestion(suggestions);
    }

    private String getGradientTypeName(VolunteerList.GradientType type) {
        return switch (type) {
            case CHONG -> "冲";
            case WEN -> "稳";
            case BAO -> "保";
            case SAFE -> "安全";
        };
    }
}
