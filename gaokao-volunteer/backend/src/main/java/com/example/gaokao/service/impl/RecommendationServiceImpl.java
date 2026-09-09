
package com.example.gaokao.service.impl;

import com.example.gaokao.dto.request.RecommendationRequest;
import com.example.gaokao.dto.response.RecommendationResponse;
import com.example.gaokao.entity.*;
import com.example.gaokao.repository.CollegeRepository;
import com.example.gaokao.repository.EnrollmentScoreRepository;
import com.example.gaokao.repository.MajorRepository;
import com.example.gaokao.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final EnrollmentScoreRepository enrollmentScoreRepository;

    @Override
    public RecommendationResponse generateRecommendations(User user, RecommendationRequest request) {
        String province = request.getProvince();
        String year = request.getYear();
        Integer rank = request.getRank();
        String scienceOrArts = request.getScienceOrArts();

        if (rank == null && request.getScore() != null) {
            rank = convertScoreToRank(province, year, request.getScore(), scienceOrArts);
        }

        if (rank == null) {
            throw new RuntimeException("无法确定全省位次，请提供分数或位次");
        }

        EnrollmentScore.ScienceOrArts sa = EnrollmentScore.ScienceOrArts.valueOf(scienceOrArts.toUpperCase());
        List<EnrollmentScore> allScores = enrollmentScoreRepository.findByYearAndProvinceAndScienceOrArts(year, province, sa);

        List<RecommendationResponse.RecommendationItem> allItems = new ArrayList<>();
        for (EnrollmentScore score : allScores) {
            College college = score.getCollege();
            if (!isCollegeEligible(college, request)) {
                continue;
            }

            Major major = null;
            if (score.getMajorCode() != null) {
                List<Major> majors = majorRepository.findByCollegeId(college.getId()).stream()
                        .filter(m -> m.getCode() != null && m.getCode().equals(score.getMajorCode()))
                        .collect(Collectors.toList());
                if (!majors.isEmpty()) {
                    major = majors.get(0);
                }
            }

            if (!isMajorEligible(major, request)) {
                continue;
            }

            Double probability = calculateAdmissionProbability(rank, score.getMinRank(), score.getMaxRank(), score.getAverageRank());
            String riskLevel = determineRiskLevel(probability);

            allItems.add(RecommendationResponse.RecommendationItem.builder()
                    .collegeId(college.getId())
                    .collegeName(college.getName())
                    .collegeProvince(college.getProvince())
                    .collegeCity(college.getCity())
                    .is985(college.getIs985())
                    .is211(college.getIs211())
                    .isDoubleFirstClass(college.getIsDoubleFirstClass())
                    .majorId(major != null ? major.getId() : null)
                    .majorName(score.getMajorName() != null ? score.getMajorName() : "不限专业")
                    .majorCategory(major != null ? major.getCategory().name() : null)
                    .subjectRequirements(major != null ? major.getSubjectRequirements() : null)
                    .lastYearMinScore(score.getMinScore())
                    .lastYearMaxScore(score.getMaxScore())
                    .lastYearAverageScore(score.getAverageScore())
                    .lastYearMinRank(score.getMinRank())
                    .lastYearMaxRank(score.getMaxRank())
                    .lastYearAverageRank(score.getAverageRank())
                    .probability(probability)
                    .riskLevel(riskLevel)
                    .build());
        }

        allItems.sort(Comparator.comparingDouble(RecommendationResponse.RecommendationItem::getProbability).reversed());

        int chongCount = request.getChongCount() != null ? request.getChongCount() : 6;
        int wenCount = request.getWenCount() != null ? request.getWenCount() : 8;
        int baoCount = request.getBaoCount() != null ? request.getBaoCount() : 6;

        List<RecommendationResponse.RecommendationItem> chongList = filterByProbabilityRange(allItems, 0.35, 0.60).stream()
                .limit(chongCount)
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendationItem> wenList = filterByProbabilityRange(allItems, 0.60, 0.85).stream()
                .limit(wenCount)
                .collect(Collectors.toList());

        List<RecommendationResponse.RecommendationItem> baoList = filterByProbabilityRange(allItems, 0.85, 1.0).stream()
                .limit(baoCount)
                .collect(Collectors.toList());

        int order = 1;
        for (var item : chongList) {
            item.setGradientType("CHONG");
            item.setPriorityOrder(order++);
        }
        for (var item : wenList) {
            item.setGradientType("WEN");
            item.setPriorityOrder(order++);
        }
        for (var item : baoList) {
            item.setGradientType("BAO");
            item.setPriorityOrder(order++);
        }

        RecommendationResponse.RiskAssessment riskAssessment = calculateRiskAssessment(chongList, wenList, baoList, rank);

        return RecommendationResponse.builder()
                .chongList(chongList)
                .wenList(wenList)
                .baoList(baoList)
                .riskAssessment(riskAssessment)
                .build();
    }

    private List<RecommendationResponse.RecommendationItem> filterByProbabilityRange(
            List<RecommendationResponse.RecommendationItem> items, double min, double max) {
        return items.stream()
                .filter(item -> item.getProbability() >= min && item.getProbability() < max)
                .collect(Collectors.toList());
    }

    private boolean isCollegeEligible(College college, RecommendationRequest request) {
        if (college == null) return false;

        if (Boolean.TRUE.equals(college.getIs985()) && !Boolean.TRUE.equals(request.getInclude985())) return false;
        if (Boolean.TRUE.equals(college.getIs211()) && !Boolean.TRUE.equals(request.getInclude211())) return false;
        if (Boolean.TRUE.equals(college.getIsDoubleFirstClass()) && !Boolean.TRUE.equals(request.getIncludeDoubleFirstClass())) return false;

        if (request.getIncludePrivate() != null && !request.getIncludePrivate()) {
            if (college.getType() == College.CollegeType.PRIVATE ||
                    college.getType() == College.CollegeType.INDEPENDENT) {
                return false;
            }
        }

        if (request.getPreferredProvinces() != null && !request.getPreferredProvinces().isEmpty()) {
            return request.getPreferredProvinces().contains(college.getProvince());
        }

        return true;
    }

    private boolean isMajorEligible(Major major, RecommendationRequest request) {
        if (major == null) return true;

        if (request.getPreferredMajors() != null && !request.getPreferredMajors().isEmpty()) {
            return request.getPreferredMajors().stream()
                    .anyMatch(pm -> major.getName().contains(pm) ||
                            major.getCategory().name().contains(pm) ||
                            major.getSubject().contains(pm));
        }

        return true;
    }

    @Override
    public Double calculateAdmissionProbability(Integer userRank, Integer minRank, Integer maxRank, Integer avgRank) {
        if (userRank == null || minRank == null || maxRank == null) {
            return 0.5;
        }

        double prob;
        if (userRank <= minRank) {
            prob = 0.95;
        } else if (userRank >= maxRank) {
            prob = 0.15;
        } else {
            double range = maxRank - minRank;
            double position = userRank - minRank;
            prob = 0.95 - (position / range) * 0.8;
        }

        if (avgRank != null) {
            double avgProb;
            if (userRank <= avgRank) {
                avgProb = 0.85;
            } else {
                double avgRange = maxRank - avgRank;
                double avgPosition = userRank - avgRank;
                avgProb = 0.85 - (avgPosition / avgRange) * 0.7;
            }
            prob = prob * 0.6 + avgProb * 0.4;
        }

        return Math.max(0.05, Math.min(0.99, prob));
    }

    @Override
    public String determineRiskLevel(Double probability) {
        if (probability >= 0.85) return "低风险";
        if (probability >= 0.60) return "中风险";
        if (probability >= 0.35) return "高风险";
        return "极高风险";
    }

    @Override
    public Integer convertScoreToRank(String province, String year, Integer score, String scienceOrArts) {
        return null;
    }

    private RecommendationResponse.RiskAssessment calculateRiskAssessment(
            List<RecommendationResponse.RecommendationItem> chongList,
            List<RecommendationResponse.RecommendationItem> wenList,
            List<RecommendationResponse.RecommendationItem> baoList,
            Integer userRank) {

        int total = chongList.size() + wenList.size() + baoList.size();

        double avgChongProb = chongList.isEmpty() ? 0 :
                chongList.stream().mapToDouble(RecommendationResponse.RecommendationItem::getProbability).average().orElse(0);
        double avgWenProb = wenList.isEmpty() ? 0 :
                wenList.stream().mapToDouble(RecommendationResponse.RecommendationItem::getProbability).average().orElse(0);
        double avgBaoProb = baoList.isEmpty() ? 0 :
                baoList.stream().mapToDouble(RecommendationResponse.RecommendationItem::getProbability).average().orElse(0);

        double slipProbability = 1.0;
        for (var item : baoList) {
            slipProbability *= (1 - item.getProbability());
        }
        slipProbability = Math.min(0.5, slipProbability);

        int lowestRank = Integer.MAX_VALUE;
        for (var item : baoList) {
            if (item.getLastYearMaxRank() != null && item.getLastYearMaxRank() > lowestRank) {
                lowestRank = item.getLastYearMaxRank();
            }
        }
        double wasteScoreIndex = 0;
        if (lowestRank != Integer.MAX_VALUE && userRank != null) {
            wasteScoreIndex = Math.max(0, (lowestRank - userRank) * 1.0 / lowestRank);
        }

        String riskLevel;
        String suggestions;
        if (slipProbability < 0.1 && baoList.size() >= 4) {
            riskLevel = "安全";
            suggestions = "志愿梯度设置合理，建议确认专业偏好后提交";
        } else if (slipProbability < 0.2 && baoList.size() >= 2) {
            riskLevel = "较安全";
            suggestions = "建议增加保底志愿数量，降低滑档风险";
        } else if (slipProbability < 0.4) {
            riskLevel = "中等风险";
            suggestions = "滑档风险较高，建议调整志愿梯度，增加保底院校";
        } else {
            riskLevel = "高风险";
            suggestions = "滑档风险极高，请大幅增加保底志愿或调整目标院校";
        }

        return RecommendationResponse.RiskAssessment.builder()
                .slipProbability(Math.round(slipProbability * 100.0) / 100.0)
                .wasteScoreIndex(Math.round(wasteScoreIndex * 100.0) / 100.0)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .totalRecommended(total)
                .chongCount(chongList.size())
                .wenCount(wenList.size())
                .baoCount(baoList.size())
                .build();
    }
}
