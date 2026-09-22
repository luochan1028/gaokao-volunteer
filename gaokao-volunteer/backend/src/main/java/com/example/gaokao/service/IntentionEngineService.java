package com.example.gaokao.service;

import com.example.gaokao.data.HollandQuestions;
import com.example.gaokao.data.ZhangXuefengQuestions;
import com.example.gaokao.dto.response.IntentionReport;
import com.example.gaokao.dto.response.RecommendationItem;
import com.example.gaokao.entity.*;
import com.example.gaokao.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * V2.0 志愿生成引擎 - PRD 3.6节
 * 初筛 -> 黑白名单 -> 现实约束加权 -> 性格匹配 -> 城市过滤 -> 冲突检测 -> 冲稳保分层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentionEngineService {

    private final StudentProfileRepository profileRepo;
    private final SchoolMajorGroupRepository schoolRepo;
    private final MajorCareerNodeRepository majorRepo;
    private final CollegeRepository collegeRepo;
    private final EnrollmentScoreRepository scoreRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntentionReport generateReport(Long userId) {
        StudentProfile profile = profileRepo.findByUserId(userId).orElse(null);
        if (profile == null) {
            log.error("StudentProfile not found for userId={}", userId);
            return null;
        }

        // 获取该省份+批次的院校专业组
        String province = profile.getProvince() != null ? profile.getProvince() : "四川";
        String batch = "本科批";
        List<SchoolMajorGroup> groups = schoolRepo.findBySchoolProvinceAndBatch(province, batch);
        if (groups.isEmpty()) {
            groups = collegeRepo.findAll().stream().limit(20).map(c -> {
                int base = c.getIs985() ? 600 : (c.getIs211() ? 550 : 480);
                return SchoolMajorGroup.builder()
                    .schoolName(c.getName())
                    .schoolProvince(c.getProvince())
                    .schoolTier(c.getIs985() ? "985" : c.getIs211() ? "211" : "普通本科")
                    .majorGroupName("通用专业组")
                    .majorList("[\"计算机科学与技术\",\"软件工程\",\"电子信息\"]")
                    .subjectRequirement("物理+化学")
                    .batch("本科批")
                    .admissionLine2024(base)
                    .admissionLine2023(base - 5)
                    .admissionLine2022(base - 10)
                    .hasMasterPoint(true)
                    .tuitionFee(5000)
                    .isDiscontinued(false)
                    .isChanged(false)
                    .dataInsufficient(false)
                    .build();
            }).collect(Collectors.toList());
        }

        // 1. 初筛: 分数±范围
        int score = profile.getTotalScore() != null ? profile.getTotalScore() : 500;
        List<ScoredGroup> scored = new ArrayList<>();
        for (SchoolMajorGroup g : groups) {
            if (Boolean.TRUE.equals(g.getIsDiscontinued())) continue;

            int avgLine = calcAvgLine(g);
            if (avgLine < score - 25 || avgLine > score + 20) continue;

            ScoredGroup sg = new ScoredGroup(g, avgLine);
            scored.add(sg);
        }

        // 2. 黑白名单过滤 + 加权
        applyMajorFilters(profile, scored);

        // 3. 现实约束加权
        applyRealityWeights(profile, scored);

        // 4. 性格匹配加权
        applyPersonalityWeights(profile, scored);

        // 5. 城市偏好过滤
        applyCityFilter(profile, scored);

        // 6. 排序
        scored.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));

        // 7. 冲稳保分层 - 按分数线差合理分配，确保每栏都有数据
        List<RecommendationItem> chong = new ArrayList<>();
        List<RecommendationItem> wen = new ArrayList<>();
        List<RecommendationItem> bao = new ArrayList<>();

        // 先按分数线差分类
        for (ScoredGroup sg : scored) {
            RecommendationItem item = buildRecommendationItem(sg, profile, score);
            int diff = sg.avgLine - score;

            String tier;
            if (diff > 8) tier = "冲";
            else if (diff >= -8) tier = "稳";
            else tier = "保";

            item.setAdmissionProb(tier);
            if (tier.equals("冲")) chong.add(item);
            else if (tier.equals("稳")) wen.add(item);
            else bao.add(item);
        }

        // 保底：如果某栏为空或不足2个，从相邻栏借调，确保每栏都有内容
        int total = scored.size();
        if (total > 0) {
            // 如果冲为空/不足，从稳中借
            if (chong.size() < 2 && wen.size() > 2) {
                int need = 2 - chong.size();
                int borrow = Math.min(need, wen.size() - 2);
                for (int i = 0; i < borrow; i++) {
                    RecommendationItem item = wen.remove(0);
                    item.setAdmissionProb("冲");
                    chong.add(item);
                }
            }
            // 如果保为空/不足，从稳中借
            if (bao.size() < 2 && wen.size() > 2) {
                int need = 2 - bao.size();
                int borrow = Math.min(need, wen.size() - 2);
                for (int i = 0; i < borrow; i++) {
                    RecommendationItem item = wen.remove(wen.size() - 1);
                    item.setAdmissionProb("保");
                    bao.add(0, item);
                }
            }
            // 如果稳为空/不足，从冲和保中各借
            if (wen.size() < 2) {
                int need = 2 - wen.size();
                if (chong.size() > 1) {
                    int borrow = Math.min((need + 1) / 2, chong.size() - 1);
                    for (int i = 0; i < borrow; i++) {
                        RecommendationItem item = chong.remove(chong.size() - 1);
                        item.setAdmissionProb("稳");
                        wen.add(item);
                    }
                }
                need = 2 - wen.size();
                if (bao.size() > 1 && need > 0) {
                    int borrow = Math.min(need, bao.size() - 1);
                    for (int i = 0; i < borrow; i++) {
                        RecommendationItem item = bao.remove(0);
                        item.setAdmissionProb("稳");
                        wen.add(item);
                    }
                }
            }

            // 如果总数少，确保每栏至少1个
            if (total <= 4) {
                if (chong.isEmpty() && wen.size() > 1) {
                    RecommendationItem item = wen.remove(0);
                    item.setAdmissionProb("冲");
                    chong.add(item);
                }
                if (bao.isEmpty() && wen.size() > 1) {
                    RecommendationItem item = wen.remove(wen.size() - 1);
                    item.setAdmissionProb("保");
                    bao.add(0, item);
                }
            }

            // 每栏最多6个
            if (chong.size() > 6) chong = chong.subList(0, 6);
            if (wen.size() > 6) wen = wen.subList(0, 6);
            if (bao.size() > 6) bao = bao.subList(0, 6);
        }

        // 构建意向书
        IntentionReport report = new IntentionReport();
        report.setVersion(profile.getReportVersion() != null ? profile.getReportVersion() + 1 : 1);
        report.setTimestamp(new Date().toString());
        report.setDataVersion("2026-四川-v1");
        report.setDisclaimer("本意向书由AI生成，仅供参考，不作为录取依据。" + ZhangXuefengQuestions.SYSTEM_DISCLAIMER);

        report.setStudentSummary(buildStudentSummary(profile));
        report.setFamilyConclusion(buildFamilyConclusion(profile));
        report.setPersonalityResult(buildPersonalityResult(profile));
        report.setRecommendedMajors(scored.stream().limit(10).map(sg -> buildRecommendationItem(sg, profile, score)).collect(Collectors.toList()));
        report.setCautiousMajors(buildCautiousMajors(profile));
        report.setChongPlans(chong);
        report.setWenPlans(wen);
        report.setBaoPlans(bao);
        report.setCityAnalysis(buildCityAnalysis(profile));
        report.setConflictPoints(buildConflictPoints(profile));
        report.setPendingQuestions(buildPendingQuestions(profile));

        // 保存
        try {
            profile.setReportData(objectMapper.writeValueAsString(report));
            profile.setReportVersion(report.getVersion());
            profileRepo.save(profile);
        } catch (Exception e) {
            log.error("Failed to save report data", e);
        }

        return report;
    }

    // === 加权逻辑 ===
    private void applyMajorFilters(StudentProfile p, List<ScoredGroup> scored) {
        for (ScoredGroup sg : scored) {
            List<String> majors = parseJsonList(sg.group.getMajorList());
            // 黑名单
            if (isBlacklisted(p, majors)) {
                sg.totalScore *= 0.3;
                sg.eliminated = true;
            }
            // 白名单
            if (isWhitelisted(p, majors)) {
                sg.totalScore *= 1.5;
            }
        }
        scored.removeIf(sg -> sg.eliminated);
    }

    private void applyRealityWeights(StudentProfile p, List<ScoredGroup> scored) {
        for (ScoredGroup sg : scored) {
            if ("承压型".equals(p.getEconomyType()) && sg.group.getTuitionFee() != null && sg.group.getTuitionFee() > 10000) {
                sg.totalScore *= 0.5;
            }
            if ("无资源型".equals(p.getResourceType())) {
                sg.totalScore *= 1.0;
            }
            if ("考研导向".equals(p.getPostgradIntent()) && Boolean.TRUE.equals(sg.group.getHasMasterPoint())) {
                sg.totalScore *= 1.2;
            }
            if ("兜底导向".equals(p.getStrategyType())) {
                sg.totalScore *= 1.1;
            }
        }
    }

    private void applyPersonalityWeights(StudentProfile p, List<ScoredGroup> scored) {
        String hollandPrimary = p.getHollandPrimary();
        if (hollandPrimary == null) return;
        List<String> fitMajors = HollandQuestions.TYPE_MAJORS.getOrDefault(hollandPrimary, Collections.emptyList());

        for (ScoredGroup sg : scored) {
            List<String> majors = parseJsonList(sg.group.getMajorList());
            boolean highMatch = majors.stream().anyMatch(fitMajors::contains);
            if (highMatch) sg.totalScore *= 1.3;
            else sg.totalScore *= 0.7;
        }
    }

    private void applyCityFilter(StudentProfile p, List<ScoredGroup> scored) {
        String pref = p.getCityPreference();
        if (pref == null || "city_open".equals(pref)) return;

        for (ScoredGroup sg : scored) {
            String schoolProvince = sg.group.getSchoolProvince();
            if (pref.equals("一线") && !isTier1City(sg.group.getSchoolName())) {
                sg.totalScore *= 0.6;
            }
            if (pref.equals("老家") && !p.getProvince().equals(schoolProvince)) {
                sg.totalScore *= 0.6;
            }
        }
    }

    // === 辅助 ===
    private int calcAvgLine(SchoolMajorGroup g) {
        int sum = 0, count = 0;
        if (g.getAdmissionLine2024() != null) { sum += g.getAdmissionLine2024(); count++; }
        if (g.getAdmissionLine2023() != null) { sum += g.getAdmissionLine2023(); count++; }
        if (g.getAdmissionLine2022() != null) { sum += g.getAdmissionLine2022(); count++; }
        return count > 0 ? sum / count : 500;
    }

    private boolean isBlacklisted(StudentProfile p, List<String> majors) {
        if (majors == null) return false;
        for (String m : majors) {
            if ("黑名单".equals(p.getMedical()) && m.contains("医")) return true;
            if ("黑名单".equals(p.getAgriculture()) && m.contains("农")) return true;
            if ("黑名单".equals(p.getNormal()) && (m.contains("师范") || m.contains("教育"))) return true;
            if ("排除".equals(p.getChemistryRelated()) && m.contains("化学")) return true;
            if ("排除".equals(p.getPhysicsRelated()) && m.contains("物理")) return true;
            if ("黑名单".equals(p.getMathMajor()) && (m.contains("数学") || m.contains("统计"))) return true;
        }
        return false;
    }

    private boolean isWhitelisted(StudentProfile p, List<String> majors) {
        if (majors == null) return false;
        for (String m : majors) {
            if ("白名单".equals(p.getMedical()) && m.contains("医")) return true;
            if ("白名单".equals(p.getAgriculture()) && m.contains("农")) return true;
            if ("白名单".equals(p.getNormal()) && (m.contains("师范") || m.contains("教育"))) return true;
            if ("白名单".equals(p.getMathMajor()) && (m.contains("数学") || m.contains("统计") || m.contains("计算机"))) return true;
        }
        return false;
    }

    private boolean isTier1City(String schoolName) {
        return schoolName != null && (schoolName.contains("北京") || schoolName.contains("上海") ||
            schoolName.contains("广州") || schoolName.contains("深圳"));
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return Collections.singletonList(json);
        }
    }

    private RecommendationItem buildRecommendationItem(ScoredGroup sg, StudentProfile p, int score) {
        RecommendationItem item = new RecommendationItem();
        item.setSchoolName(sg.group.getSchoolName());
        item.setMajorGroup(sg.group.getMajorGroupName());
        item.setMajorList(parseJsonList(sg.group.getMajorList()));
        item.setMatchScore((int) Math.min(100, sg.totalScore * 100));

        // 现实理由
        StringBuilder reality = new StringBuilder();
        if ("承压型".equals(p.getEconomyType()) && sg.group.getTuitionFee() != null && sg.group.getTuitionFee() > 10000) {
            reality.append("学费较高，家庭经济承压需注意。");
        }
        if ("无资源型".equals(p.getResourceType())) {
            reality.append("无行业资源，优先选择就业面广的专业。");
        }
        if ("考研导向".equals(p.getPostgradIntent()) && Boolean.TRUE.equals(sg.group.getHasMasterPoint())) {
            reality.append("该校有硕士点，方便考研。");
        }
        item.setRealityReason(reality.toString());

        // 性格理由
        String hollandP = p.getHollandPrimary();
        if (hollandP != null) {
            List<String> fitMajors = HollandQuestions.TYPE_MAJORS.getOrDefault(hollandP, Collections.emptyList());
            List<String> majors = parseJsonList(sg.group.getMajorList());
            boolean match = majors.stream().anyMatch(fitMajors::contains);
            item.setPersonalityReason(match ? "专业与你的霍兰德类型(" + hollandP + ")高度匹配" : "专业与性格类型匹配度一般");
        }

        // 职业理由
        item.setCareerReason("该专业组就业方向广泛，具体岗位和薪资可查看职业规划详情。");

        // 风险
        List<String> risks = new ArrayList<>();
        if (sg.avgLine > score + 5) risks.add("录取分数偏高，属于冲刺方案");
        if (Boolean.TRUE.equals(sg.group.getDataInsufficient())) risks.add("该院校数据不完整，建议谨慎");
        item.setRiskWarnings(risks);

        // 一句话
        item.setOneSentence(sg.group.getSchoolName() + "的" + sg.group.getMajorGroupName() + "，" +
            (sg.avgLine > score ? "需要冲刺" : "录取概率较大"));

        return item;
    }

    private String buildStudentSummary(StudentProfile p) {
        StringBuilder sb = new StringBuilder();
        sb.append("省份：").append(p.getProvince() != null ? p.getProvince() : "未填写").append("\n");
        sb.append("年份：").append(p.getExamYear() != null ? p.getExamYear() : "2026").append("\n");
        sb.append("选科：").append(p.getSubjectComb() != null ? p.getSubjectComb() : "未填写").append("\n");
        sb.append("总分：").append(p.getTotalScore() != null ? p.getTotalScore() : "未填写").append("\n");
        sb.append("位次：").append(p.getRank() != null && p.getRank() > 0 ? p.getRank() : "未填写").append("\n");
        sb.append("批次：").append(p.getBatch() != null ? p.getBatch() : "本科批");
        return sb.toString();
    }

    private String buildFamilyConclusion(StudentProfile p) {
        StringBuilder sb = new StringBuilder();
        sb.append("经济条件：").append(p.getEconomyType() != null ? p.getEconomyType() : "未填写").append("\n");
        sb.append("行业资源：").append(p.getResourceType() != null ? p.getResourceType() : "未填写").append("\n");
        sb.append("策略导向：").append(p.getStrategyType() != null ? p.getStrategyType() : "未填写").append("\n");
        sb.append("考研意向：").append(p.getPostgradIntent() != null ? p.getPostgradIntent() : "未填写").append("\n");
        sb.append("城市偏好：").append(p.getCityPreference() != null ? p.getCityPreference() : "未填写").append("\n");
        sb.append("家长看重：").append(p.getParentValue() != null ? p.getParentValue() : "未填写");
        sb.append("\n\n").append(ZhangXuefengQuestions.ZHANGXUEFENG_DISCLAIMER);
        return sb.toString();
    }

    private String buildPersonalityResult(StudentProfile p) {
        StringBuilder sb = new StringBuilder();
        String primary = p.getHollandPrimary();
        String secondary = p.getHollandSecondary();
        if (primary != null) {
            sb.append("主导类型：").append(HollandQuestions.TYPE_NAMES.getOrDefault(primary, primary)).append("\n");
            sb.append("次导类型：").append(HollandQuestions.TYPE_NAMES.getOrDefault(secondary, secondary)).append("\n");
            sb.append("置信度：").append("high".equals(p.getHollandConfidence()) ? "高" : "低").append("\n");
            sb.append("适配专业簇：").append(String.join("、", HollandQuestions.TYPE_MAJORS.getOrDefault(primary, Collections.emptyList())));
        } else {
            sb.append("性格测评未完成");
        }
        return sb.toString();
    }

    private List<String> buildCautiousMajors(StudentProfile p) {
        List<String> cautious = new ArrayList<>();
        if ("谨慎".equals(p.getMathMajor())) cautious.add("数学/统计类专业（你表示勉强接受，需谨慎考虑）");
        if ("待定".equals(p.getMedical())) cautious.add("医学类专业（你表示犹豫，建议进一步了解后再决定）");
        if ("待定".equals(p.getAgriculture())) cautious.add("农学类专业（你表示犹豫，建议进一步了解后再决定）");
        if ("待定".equals(p.getNormal())) cautious.add("师范类专业（你表示犹豫，建议进一步了解后再决定）");
        if ("承压型".equals(p.getEconomyType())) cautious.add("高学费/长学制专业（家庭经济承压型需特别谨慎）");
        return cautious;
    }

    private String buildCityAnalysis(StudentProfile p) {
        String pref = p.getCityPreference();
        if (pref == null) return "城市偏好未填写";
        switch (pref) {
            case "一线": return "你倾向于一线城市。一线城市资源丰富但竞争激烈，建议关注北京、上海、广州、深圳的院校。";
            case "新一线": return "你倾向于新一线城市。成都、杭州、武汉等城市性价比高，推荐优先考虑。";
            case "老家": return "你倾向于留在老家。就近选择可以减少生活成本，建议关注本省院校。";
            case "city_open": return "你对城市持开放态度。建议综合考虑分数匹配度和专业实力，不设城市限制。";
            default: return "城市偏好：" + pref;
        }
    }

    private List<String> buildConflictPoints(StudentProfile p) {
        List<String> conflicts = new ArrayList<>();
        if (p.getParentValue() != null && p.getStrategyType() != null) {
            if (p.getParentValue().equals("稳定") && p.getStrategyType().equals("名校导向")) {
                conflicts.add("家长看重稳定，但学生倾向冲名校。建议：兼顾保底方案中选择有稳定就业的院校。");
            }
            if (p.getParentValue().equals("高薪") && p.getCityPreference() != null && p.getCityPreference().equals("老家")) {
                conflicts.add("家长希望高薪，但学生想留老家。建议：高薪岗位多在一线，留老家可能薪资受限。");
            }
        }
        return conflicts;
    }

    private List<String> buildPendingQuestions(StudentProfile p) {
        List<String> pending = new ArrayList<>();
        if (p.getRank() == null || p.getRank() == 0) pending.add("省排名位次尚未确认");
        if (p.getMedical() != null && p.getMedical().equals("待定")) pending.add("医学专业意向待定");
        if (p.getAgriculture() != null && p.getAgriculture().equals("待定")) pending.add("农学专业意向待定");
        if (p.getNormal() != null && p.getNormal().equals("待定")) pending.add("师范专业意向待定");
        if (p.getHollandConfidence() != null && p.getHollandConfidence().equals("low")) pending.add("性格测评置信度低，建议补做");
        return pending;
    }

    // === 内部类 ===
    private static class ScoredGroup {
        SchoolMajorGroup group;
        double totalScore = 1.0;
        int avgLine;
        boolean eliminated = false;

        ScoredGroup(SchoolMajorGroup group, int avgLine) {
            this.group = group;
            this.avgLine = avgLine;
        }
    }
}
