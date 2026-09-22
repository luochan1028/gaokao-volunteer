
package com.example.gaokao.service.impl;

import com.example.gaokao.dto.request.RecommendationRequest;
import com.example.gaokao.entity.College;
import com.example.gaokao.entity.EnrollmentScore;
import com.example.gaokao.entity.Major;
import com.example.gaokao.entity.User;
import com.example.gaokao.repository.CollegeRepository;
import com.example.gaokao.repository.EnrollmentScoreRepository;
import com.example.gaokao.repository.MajorRepository;
import com.example.gaokao.service.RecommendationService;
import com.example.gaokao.dto.response.RecommendationResponse;
import com.example.gaokao.dto.response.RecommendationResponse.RecommendationItem;
import com.example.gaokao.dto.response.RecommendationResponse.RiskAssessment;
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
    private final EnrollmentScoreRepository enrollmentScoreRepository;
    private final MajorRepository majorRepository;

    private static final double BAO_MIN_PROB = 0.70;
    private static final double WEN_MIN_PROB = 0.40;
    private static final double CHONG_MIN_PROB = 0.10;
    private static final double RECOMMEND_MIN_PROB = 0.05;

    @Override
    public RecommendationResponse generateRecommendations(User user, RecommendationRequest request) {
        Integer rank = request.getRank();
        if (rank == null || rank <= 0) {
            throw new RuntimeException("请提供全省位次");
        }

        List<EnrollmentScore> allScores = enrollmentScoreRepository
                .findByYearAndProvinceAndScienceOrArts(
                        request.getYear(), request.getProvince(),
                        EnrollmentScore.ScienceOrArts.valueOf(request.getScienceOrArts()));

        Map<Long, EnrollmentScore> scoreByCollege = new HashMap<>();
        for (EnrollmentScore es : allScores) {
            if (es.getCollege() != null) {
                scoreByCollege.putIfAbsent(es.getCollege().getId(), es);
            }
        }

        List<College> allColleges = collegeRepository.findAll();

        List<String> selectedSubjects = request.getSelectedSubjects();
        Map<Long, Boolean> collegeSubjectMatch = new HashMap<>();
        if (selectedSubjects != null && !selectedSubjects.isEmpty()) {
            List<Major> allMajors = majorRepository.findAll();
            for (College college : allColleges) {
                boolean hasMatch = false;
                for (Major major : allMajors) {
                    if (major.getCollege() != null && major.getCollege().getId().equals(college.getId())) {
                        if (isSubjectEligible(major.getSubjectRequirements(), selectedSubjects)) {
                            hasMatch = true;
                            break;
                        }
                    }
                }
                collegeSubjectMatch.put(college.getId(), hasMatch);
            }
        }

        List<RecommendationItem> allItems = new ArrayList<>();

        for (College college : allColleges) {
            if (!isCollegeEligible(college, request)) continue;

            if (selectedSubjects != null && !selectedSubjects.isEmpty()) {
                Boolean match = collegeSubjectMatch.get(college.getId());
                if (match == null || !match) continue;
            }

            EnrollmentScore latestScore = scoreByCollege.get(college.getId());
            if (latestScore == null) continue;

            Double probability = calculateAdmissionProbability(
                    rank, latestScore.getMinRank(), latestScore.getMaxRank(), latestScore.getAverageRank());

            if (probability < RECOMMEND_MIN_PROB) continue;

            RecommendationItem item = RecommendationItem.builder()
                    .collegeId(college.getId())
                    .collegeName(college.getName())
                    .collegeProvince(college.getProvince())
                    .collegeCity(college.getCity())
                    .is985(college.getIs985())
                    .is211(college.getIs211())
                    .isDoubleFirstClass(college.getIsDoubleFirstClass())
                    .majorName("不限专业")
                    .lastYearMinScore(latestScore.getMinScore())
                    .lastYearMaxScore(latestScore.getMaxScore())
                    .lastYearAverageScore(latestScore.getAverageScore())
                    .lastYearMinRank(latestScore.getMinRank())
                    .lastYearMaxRank(latestScore.getMaxRank())
                    .lastYearAverageRank(latestScore.getAverageRank())
                    .probability(probability)
                    .build();

            allItems.add(item);
        }

        allItems.sort(Comparator.comparingDouble(RecommendationItem::getProbability).reversed());

        int chongCount = request.getChongCount() != null ? request.getChongCount() : 6;
        int wenCount = request.getWenCount() != null ? request.getWenCount() : 8;
        int baoCount = request.getBaoCount() != null ? request.getBaoCount() : 6;

        List<RecommendationItem> baoList = new ArrayList<>();
        List<RecommendationItem> wenList = new ArrayList<>();
        List<RecommendationItem> chongList = new ArrayList<>();

        for (var item : allItems) {
            double p = item.getProbability();
            if (p >= BAO_MIN_PROB) {
                baoList.add(item);
            } else if (p >= WEN_MIN_PROB) {
                wenList.add(item);
            } else if (p >= CHONG_MIN_PROB) {
                chongList.add(item);
            }
        }

        double baoSoftMin = BAO_MIN_PROB * 0.95;
        double wenSoftMin = WEN_MIN_PROB * 0.88;
        double chongSoftMin = CHONG_MIN_PROB;

        if (wenList.size() < wenCount) {
            int fromBaoMax = Math.max(0, baoList.size() - 3);
            while (wenList.size() < wenCount && baoList.size() > fromBaoMax) {
                RecommendationItem item = baoList.get(baoList.size() - 1);
                if (item.getProbability() >= wenSoftMin) {
                    wenList.add(0, baoList.remove(baoList.size() - 1));
                } else {
                    break;
                }
            }
            int fromChongMax = Math.min(chongList.size(), wenCount - wenList.size());
            for (int i = 0; i < fromChongMax; i++) {
                if (chongList.get(i).getProbability() >= wenSoftMin && wenList.size() < wenCount) {
                    wenList.add(chongList.get(i));
                }
            }
            chongList.removeAll(wenList);
        }

        if (baoList.size() < baoCount && !wenList.isEmpty()) {
            int fromWenMax = Math.min(wenList.size(), baoCount - baoList.size());
            for (int i = 0; i < fromWenMax; i++) {
                if (wenList.get(i).getProbability() >= baoSoftMin && baoList.size() < baoCount) {
                    baoList.add(wenList.get(i));
                }
            }
            wenList.removeAll(baoList);
        }

        if (chongList.size() < chongCount && !wenList.isEmpty()) {
            int fromWenMax = Math.min(wenList.size(), chongCount - chongList.size());
            for (int i = wenList.size() - 1; i >= Math.max(0, wenList.size() - fromWenMax); i--) {
                if (wenList.get(i).getProbability() < WEN_MIN_PROB * 1.10) {
                    chongList.add(0, wenList.get(i));
                }
            }
            List<RecommendationItem> keepWen = new ArrayList<>();
            for (var item : wenList) {
                if (!chongList.contains(item)) keepWen.add(item);
            }
            wenList = keepWen;
        }

        chongList = chongList.stream().limit(chongCount).collect(Collectors.toList());
        wenList = wenList.stream().limit(wenCount).collect(Collectors.toList());
        baoList = baoList.stream().limit(baoCount).collect(Collectors.toList());

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

        RiskAssessment riskAssessment = calculateRiskAssessment(chongList, wenList, baoList, rank);

        return RecommendationResponse.builder()
                .chongList(chongList)
                .wenList(wenList)
                .baoList(baoList)
                .riskAssessment(riskAssessment)
                .build();
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

        return true;
    }

    @Override
    public Double calculateAdmissionProbability(Integer userRank, Integer minRank, Integer maxRank, Integer avgRank) {
        if (userRank == null || minRank == null || maxRank == null) {
            return 0.5;
        }

        double prob;
        if (userRank <= minRank) {
            double ratio = (double) userRank / minRank;
            prob = 0.80 + (1 - ratio) * 0.19;
        } else if (userRank >= maxRank) {
            double excessRatio = (double) (userRank - maxRank) / maxRank;
            prob = Math.max(0.02, 0.28 * Math.exp(-excessRatio * 2.5));
        } else {
            double range = maxRank - minRank;
            double position = userRank - minRank;
            prob = 0.82 - (position / range) * 0.50;
        }

        if (avgRank != null && avgRank > 0 && avgRank > minRank && avgRank < maxRank) {
            double avgProb;
            if (userRank <= avgRank) {
                double ratio = (double) userRank / avgRank;
                avgProb = 0.72 + (1 - ratio) * 0.22;
            } else {
                double avgRange = maxRank - avgRank;
                if (avgRange > 0) {
                    double avgPosition = userRank - avgRank;
                    avgProb = Math.max(0.05, 0.72 - (avgPosition / avgRange) * 0.60);
                } else {
                    avgProb = prob;
                }
            }
            prob = prob * 0.6 + avgProb * 0.4;
        }

        return Math.max(0.02, Math.min(0.99, prob));
    }

    @Override
    public String determineRiskLevel(Double probability) {
        if (probability >= 0.70) return "低风险";
        if (probability >= 0.40) return "中风险";
        if (probability >= 0.20) return "较高风险";
        return "高风险";
    }

    @Override
    public Integer convertScoreToRank(String province, String year, Integer score, String scienceOrArts) {
        return null;
    }

    private boolean isSubjectEligible(String requirement, List<String> selectedSubjects) {
        if (requirement == null || requirement.isBlank() || "不限".equals(requirement)) {
            return true;
        }
        if (selectedSubjects == null || selectedSubjects.isEmpty()) {
            return true;
        }

        String req = requirement.trim();
        if (req.contains("或")) {
            String[] parts = req.split("或");
            for (String part : parts) {
                if (selectedSubjects.contains(part.trim())) {
                    return true;
                }
            }
            return false;
        } else {
            return selectedSubjects.contains(req);
        }
    }

    private RiskAssessment calculateRiskAssessment(List<RecommendationItem> chongList,
                                                    List<RecommendationItem> wenList,
                                                    List<RecommendationItem> baoList,
                                                    Integer userRank) {
        int total = chongList.size() + wenList.size() + baoList.size();
        int chongSize = chongList.size();
        int wenSize = wenList.size();
        int baoSize = baoList.size();

        double slipProbability = 0.0;
        if (baoSize == 0) {
            slipProbability = wenSize > 0 ? 55.0 : 90.0;
        } else if (baoSize < 3) {
            slipProbability = 30.0;
        } else if (wenSize == 0) {
            slipProbability = 15.0;
        } else {
            double baoAvg = baoList.stream().mapToDouble(RecommendationItem::getProbability).average().orElse(0.8);
            if (baoAvg >= 0.85) {
                slipProbability = 3.0;
            } else if (baoAvg >= 0.75) {
                slipProbability = 8.0;
            } else {
                slipProbability = 15.0;
            }
        }

        double wasteScoreIndex = 0.0;
        if (baoSize > 0) {
            double avgProb = baoList.stream()
                    .mapToDouble(RecommendationItem::getProbability)
                    .average()
                    .orElse(0.9);
            wasteScoreIndex = Math.max(0, (avgProb - 0.80) * 100);
        }

        String riskLevel;
        if (slipProbability >= 60) {
            riskLevel = "极高风险";
        } else if (slipProbability >= 25) {
            riskLevel = "高风险";
        } else if (slipProbability >= 10) {
            riskLevel = "中等风险";
        } else {
            riskLevel = "安全";
        }

        return RiskAssessment.builder()
                .slipProbability(Math.round(slipProbability * 100) / 100.0)
                .wasteScoreIndex(Math.round(wasteScoreIndex * 100) / 100.0)
                .totalRecommended(total)
                .chongCount(chongSize)
                .wenCount(wenSize)
                .baoCount(baoSize)
                .riskLevel(riskLevel)
                .build();
    }
}
