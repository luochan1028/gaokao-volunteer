package com.example.gaokao.service;

import com.example.gaokao.data.FuzzyExpressionMap;
import com.example.gaokao.data.HollandQuestions;
import com.example.gaokao.data.ZhangXuefengQuestions;
import com.example.gaokao.dto.request.DialogRequest;
import com.example.gaokao.dto.response.DialogResponse;
import com.example.gaokao.entity.StudentProfile;
import com.example.gaokao.repository.StudentProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * V2.0 对话引擎 - PRD 3.1节
 * 多轮对话管理 + Slot-Filling + 意图分类 + 模糊表达归一
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DialogEngineService {

    private final StudentProfileRepository profileRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_TURNS = 20;

    public enum Phase {
        IDLE, BASE_INFO, FAMILY_Q, MAJOR_Q, HOLLAND, GENERATE, REPORT, DONE
    }

    public enum Intent {
        INFO_PROVIDE,      // 信息提供
        FOLLOW_UP,          // 追问
        CORRECTION,         // 修正
        CONFIRM,            // 确认
        TOPIC_JUMP          // 跳转主题
    }

    /**
     * 开始对话
     */
    public DialogResponse startDialog(Long userId) {
        StudentProfile profile = getOrCreateProfile(userId);
        profile.setDialogPhase(Phase.BASE_INFO.name());
        profile.setCurrentQuestionId("base_province");
        profileRepo.save(profile);

        DialogResponse resp = new DialogResponse();
        resp.setResponse("你好！我是高考志愿AI助手🤖 接下来几分钟，我会通过对话帮你理清志愿方向。先从基本信息开始——你是哪个省份的考生？");
        resp.setOptions(Arrays.asList("四川", "河南", "山东", "广东", "河北", "湖南"));
        resp.setPhase(Phase.BASE_INFO.name());
        resp.setNextAction("voice_input");
        resp.setNextQuestionId("base_province");
        return resp;
    }

    /**
     * 处理用户消息 - 核心对话循环
     */
    public DialogResponse processMessage(DialogRequest req) {
        StudentProfile profile = getOrCreateProfile(req.getUserId());
        String userText = req.getText();
        Phase phase = Phase.valueOf(profile.getDialogPhase());

        // 1. 意图分类
        Intent intent = classifyIntent(userText);
        double confidence = calculateIntentConfidence(userText, intent);

        // 2. 修正意图处理
        if (intent == Intent.CORRECTION) {
            return handleCorrection(profile, userText, phase);
        }

        // 3. 模糊表达归一
        List<FuzzyExpressionMap.FuzzyMatch> fuzzyMatches = FuzzyExpressionMap.match(userText);

        // 4. 按阶段路由
        DialogResponse resp;
        switch (phase) {
            case BASE_INFO:
                resp = handleBaseInfo(profile, userText, fuzzyMatches);
                break;
            case FAMILY_Q:
                resp = handleFamilyQuestion(profile, userText);
                break;
            case MAJOR_Q:
                resp = handleMajorQuestion(profile, userText);
                break;
            case HOLLAND:
                resp = handleHolland(profile, userText);
                break;
            case GENERATE:
                resp = triggerGeneration(profile, userText);
                break;
            default:
                resp = new DialogResponse();
                resp.setResponse("对话已完成。你可以查看你的志愿意向书，或者重新开始对话。");
                resp.setPhase(Phase.DONE.name());
                resp.setNextAction("done");
        }

        resp.setIntent(intent.name());
        resp.setIntentConfidence(confidence);

        // 5. 低置信度确认
        if (confidence < 0.6) {
            resp.setNeedConfirm("我理解你说的是「" + userText + "」，能确认一下你的意思吗？");
        }

        // 保存对话历史
        saveDialogHistory(profile, "user: " + userText + "\nassistant: " + resp.getResponse());
        profileRepo.save(profile);

        return resp;
    }

    // === 基础信息采集 ===
    private DialogResponse handleBaseInfo(StudentProfile profile, String text, List<FuzzyExpressionMap.FuzzyMatch> fuzzy) {
        String qId = profile.getCurrentQuestionId();
        DialogResponse resp = new DialogResponse();
        resp.setPhase(Phase.BASE_INFO.name());

        switch (qId) {
            case "base_province":
                String province = extractProvince(text);
                if (province != null) {
                    profile.setProvince(province);
                    resp.setResponse("好的，" + province + "考生👌 你的高考年份是？");
                    resp.setOptions(Arrays.asList("2026年", "2025年", "2027年"));
                    profile.setCurrentQuestionId("base_year");
                } else {
                    resp.setResponse("你是哪个省份的考生呀？");
                    resp.setOptions(Arrays.asList("四川", "河南", "山东", "广东", "河北", "湖南"));
                }
                break;

            case "base_year":
                try {
                    int year = extractYear(text);
                    profile.setExamYear(year);
                } catch (Exception e) {
                    profile.setExamYear(2026);
                }
                resp.setResponse("明白了～你的选科组合是什么？");
                resp.setOptions(Arrays.asList("物化生", "物化地", "物化政", "史地政", "史地生"));
                profile.setCurrentQuestionId("base_subject");
                break;

            case "base_subject":
                String subject = extractSubjectComb(text);
                if (subject != null) {
                    profile.setSubjectComb(subject);
                    resp.setResponse("好的，" + subject + "组合👍 你的高考总分是多少？（可以填预估分）");
                    resp.setOptions(Arrays.asList("600分以上", "550-600分", "500-550分", "450-500分"));
                    profile.setCurrentQuestionId("base_score");
                } else {
                    resp.setResponse("你的选科组合是？比如物理+化学+生物，简称物化生～");
                    resp.setOptions(Arrays.asList("物化生", "物化地", "物化政", "史地政"));
                }
                break;

            case "base_score":
                Integer score = extractNumber(text);
                if (score != null && score > 0 && score <= 750) {
                    profile.setTotalScore(score);
                    resp.setResponse("收到！你的省排名位次大概是多少？（不确定可以说不知道）");
                    resp.setOptions(Arrays.asList("10000以内", "10000-30000", "30000-50000", "不知道"));
                    profile.setCurrentQuestionId("base_rank");
                } else {
                    resp.setResponse("请输入0-750之间的分数哦～");
                    resp.setOptions(Arrays.asList("600分以上", "550分左右", "500分左右"));
                }
                break;

            case "base_rank":
                Integer rank = extractNumber(text);
                if (text.contains("不知道") || text.contains("不确定") || text.contains("没")) {
                    profile.setRank(0);
                    resp.setResponse("没关系，位次可以后续补充。接下来进入张雪峰家庭现实八问，帮我了解你的家庭背景～");
                    resp.setOptions(ZhangXuefengQuestions.FAMILY_QUESTIONS.get(0).getOptions());
                    profile.setDialogPhase(Phase.FAMILY_Q.name());
                    profile.setCurrentQuestionId("Q1");
                    resp.setPhase(Phase.FAMILY_Q.name());
                    resp.setNextQuestionId("Q1");
                } else if (rank != null && rank > 0) {
                    profile.setRank(rank);
                    profile.setDialogPhase(Phase.FAMILY_Q.name());
                    profile.setCurrentQuestionId("Q1");
                    resp.setResponse("好的，记下了！接下来进入张雪峰家庭现实八问。第一题：" + ZhangXuefengQuestions.FAMILY_QUESTIONS.get(0).getText());
                    resp.setOptions(ZhangXuefengQuestions.FAMILY_QUESTIONS.get(0).getOptions());
                    resp.setPhase(Phase.FAMILY_Q.name());
                    resp.setNextQuestionId("Q1");
                } else {
                    resp.setResponse("请输入你的省排名位次，或者说不知道也行～");
                    resp.setOptions(Arrays.asList("10000以内", "50000左右", "不知道"));
                }
                break;
        }

        resp.setNextAction("voice_input");
        return resp;
    }

    // === 张雪峰家庭八问 ===
    private DialogResponse handleFamilyQuestion(StudentProfile profile, String text) {
        String qId = profile.getCurrentQuestionId();

        // 处理followup追问状态
        if (qId.equals("Q7_followup")) {
            DialogResponse resp = new DialogResponse();
            resp.setPhase(Phase.FAMILY_Q.name());
            if (text.contains("不知道") || text.contains("没有") || text.contains("随便") || text.contains("都可以")) {
                setProfileField(profile, "city_preference", "city_open");
            } else {
                setProfileField(profile, "city_excluded", text);
            }
            // 继续下一题Q8
            int nextIdx = 7; // Q7的下一题是Q8(index=7)
            if (nextIdx < ZhangXuefengQuestions.FAMILY_QUESTIONS.size()) {
                ZhangXuefengQuestions.Question nextQ = ZhangXuefengQuestions.FAMILY_QUESTIONS.get(nextIdx);
                resp.setResponse("好的～下一题：" + nextQ.getText());
                resp.setOptions(nextQ.getOptions());
                profile.setCurrentQuestionId(nextQ.getId());
                resp.setNextQuestionId(nextQ.getId());
            } else {
                resp.setResponse("家庭现实八问完成！接下来是专业筛选八问。第一题：" + ZhangXuefengQuestions.MAJOR_QUESTIONS.get(0).getText());
                resp.setOptions(ZhangXuefengQuestions.MAJOR_QUESTIONS.get(0).getOptions());
                profile.setDialogPhase(Phase.MAJOR_Q.name());
                profile.setCurrentQuestionId("P1");
                resp.setPhase(Phase.MAJOR_Q.name());
                resp.setNextQuestionId("P1");
            }
            resp.setNextAction("voice_input");
            return resp;
        }

        int idx = Integer.parseInt(qId.substring(1)) - 1;

        if (idx < 0 || idx >= ZhangXuefengQuestions.FAMILY_QUESTIONS.size()) {
            profile.setDialogPhase(Phase.MAJOR_Q.name());
            profile.setCurrentQuestionId("P1");
            return handleMajorQuestion(profile, text);
        }

        ZhangXuefengQuestions.Question q = ZhangXuefengQuestions.FAMILY_QUESTIONS.get(idx);
        int answerIdx = matchAnswer(text, q.getOptions());

        DialogResponse resp = new DialogResponse();
        resp.setPhase(Phase.FAMILY_Q.name());

        if (answerIdx >= 0) {
            String value = q.getValues()[answerIdx];
            setProfileField(profile, q.getField(), value);

            // Q7不确定追问
            if (qId.equals("Q7") && answerIdx == 3) {
                resp.setResponse(ZhangXuefengQuestions.Q7_FOLLOWUP);
                resp.setOptions(Arrays.asList("没有，都可以", "不想去西藏/新疆等偏远地区", "只想留本省"));
                profile.setCurrentQuestionId("Q7_followup");
                resp.setNextQuestionId("Q7_followup");
                resp.setNextAction("voice_input");
                return resp;
            }

            // 下一题
            int nextIdx = idx + 1;
            if (nextIdx < ZhangXuefengQuestions.FAMILY_QUESTIONS.size()) {
                ZhangXuefengQuestions.Question nextQ = ZhangXuefengQuestions.FAMILY_QUESTIONS.get(nextIdx);
                resp.setResponse("好的～下一题：" + nextQ.getText());
                resp.setOptions(nextQ.getOptions());
                profile.setCurrentQuestionId(nextQ.getId());
                resp.setNextQuestionId(nextQ.getId());
            } else {
                resp.setResponse("家庭现实八问完成！接下来是专业筛选八问。第一题：" + ZhangXuefengQuestions.MAJOR_QUESTIONS.get(0).getText());
                resp.setOptions(ZhangXuefengQuestions.MAJOR_QUESTIONS.get(0).getOptions());
                profile.setDialogPhase(Phase.MAJOR_Q.name());
                profile.setCurrentQuestionId("P1");
                resp.setPhase(Phase.MAJOR_Q.name());
                resp.setNextQuestionId("P1");
            }
        } else {
            resp.setResponse("可以从下面选项中选一个哦～");
            resp.setOptions(q.getOptions());
            resp.setNextQuestionId(qId);
        }

        resp.setNextAction("voice_input");
        return resp;
    }

    // === 张雪峰专业八问 ===
    private DialogResponse handleMajorQuestion(StudentProfile profile, String text) {
        String qId = profile.getCurrentQuestionId();

        // 处理followup追问状态 (P1_followup, P2_followup, P3_followup)
        if (qId.endsWith("_followup")) {
            DialogResponse resp = new DialogResponse();
            resp.setPhase(Phase.MAJOR_Q.name());
            String baseQId = qId.replace("_followup", "");
            if (text.contains("不能") || text.contains("不行")) {
                setProfileField(profile, getBaseField(baseQId), "谨慎");
            }
            // 继续下一题
            int baseIdx = Integer.parseInt(baseQId.substring(1)) - 1;
            int nextIdx = baseIdx + 1;
            if (nextIdx < ZhangXuefengQuestions.MAJOR_QUESTIONS.size()) {
                ZhangXuefengQuestions.Question nextQ = ZhangXuefengQuestions.MAJOR_QUESTIONS.get(nextIdx);
                resp.setResponse("好的～下一题：" + nextQ.getText());
                resp.setOptions(nextQ.getOptions());
                profile.setCurrentQuestionId(nextQ.getId());
                resp.setNextQuestionId(nextQ.getId());
            } else {
                resp.setResponse("专业筛选完成！接下来做个简单的性格测试，12道题很快就好～第1题：" + HollandQuestions.QUESTIONS.get(0).getScenario());
                resp.setOptions(Arrays.asList(
                    HollandQuestions.QUESTIONS.get(0).getOptionA(),
                    HollandQuestions.QUESTIONS.get(0).getOptionB()
                ));
                profile.setDialogPhase(Phase.HOLLAND.name());
                profile.setCurrentQuestionId("H1");
                resp.setPhase(Phase.HOLLAND.name());
                resp.setNextQuestionId("H1");
            }
            resp.setNextAction("voice_input");
            return resp;
        }

        int idx = Integer.parseInt(qId.substring(1)) - 1;

        if (idx < 0 || idx >= ZhangXuefengQuestions.MAJOR_QUESTIONS.size()) {
            profile.setDialogPhase(Phase.HOLLAND.name());
            profile.setCurrentQuestionId("H1");
            return handleHolland(profile, text);
        }

        ZhangXuefengQuestions.Question q = ZhangXuefengQuestions.MAJOR_QUESTIONS.get(idx);

        // P5-P8触发条件检查
        if (idx >= 4) {
            String subjects = profile.getSubjectComb() != null ? profile.getSubjectComb() : "";
            if (!subjects.contains("物理") && !subjects.contains("化学")) {
                int nextIdx = idx + 1;
                if (nextIdx < ZhangXuefengQuestions.MAJOR_QUESTIONS.size()) {
                    ZhangXuefengQuestions.Question nextQ = ZhangXuefengQuestions.MAJOR_QUESTIONS.get(nextIdx);
                    profile.setCurrentQuestionId(nextQ.getId());
                    return handleMajorQuestion(profile, text);
                }
            }
        }

        int answerIdx = matchAnswer(text, q.getOptions());

        DialogResponse resp = new DialogResponse();
        resp.setPhase(Phase.MAJOR_Q.name());

        if (answerIdx >= 0) {
            String value = q.getValues()[answerIdx];
            setProfileField(profile, q.getField(), value);

            // P1-P3回答"是"时追问考研
            if ((qId.equals("P1") || qId.equals("P2") || qId.equals("P3")) && answerIdx == 0) {
                resp.setResponse(ZhangXuefengQuestions.P_MEDICAL_FOLLOWUP);
                resp.setOptions(Arrays.asList("能接受", "勉强接受", "不能接受"));
                profile.setCurrentQuestionId(qId + "_followup");
                resp.setNextQuestionId(qId + "_followup");
                resp.setNextAction("voice_input");
                return resp;
            }

            int nextIdx = idx + 1;
            if (nextIdx < ZhangXuefengQuestions.MAJOR_QUESTIONS.size()) {
                ZhangXuefengQuestions.Question nextQ = ZhangXuefengQuestions.MAJOR_QUESTIONS.get(nextIdx);
                resp.setResponse("好的～下一题：" + nextQ.getText());
                resp.setOptions(nextQ.getOptions());
                profile.setCurrentQuestionId(nextQ.getId());
                resp.setNextQuestionId(nextQ.getId());
            } else {
                resp.setResponse("专业筛选完成！接下来做个简单的性格测试，12道题很快就好～第1题：" + HollandQuestions.QUESTIONS.get(0).getScenario());
                resp.setOptions(Arrays.asList(
                    HollandQuestions.QUESTIONS.get(0).getOptionA(),
                    HollandQuestions.QUESTIONS.get(0).getOptionB()
                ));
                profile.setDialogPhase(Phase.HOLLAND.name());
                profile.setCurrentQuestionId("H1");
                resp.setPhase(Phase.HOLLAND.name());
                resp.setNextQuestionId("H1");
            }
        } else {
            resp.setResponse("可以从下面选项中选一个哦～");
            resp.setOptions(q.getOptions());
            resp.setNextQuestionId(qId);
        }

        resp.setNextAction("voice_input");
        return resp;
    }

    // === 霍兰德测评 ===
    private DialogResponse handleHolland(StudentProfile profile, String text) {
        String qId = profile.getCurrentQuestionId();

        // 解析已答题数
        int answeredCount = 0;
        if (profile.getHollandScores() != null && !profile.getHollandScores().isEmpty()) {
            try {
                Map<String, Integer> scores = objectMapper.readValue(profile.getHollandScores(), Map.class);
                answeredCount = scores.values().stream().mapToInt(Integer::intValue).sum();
            } catch (Exception ignored) {}
        }

        int currentIdx = 0;
        if (qId != null && qId.startsWith("H")) {
            try {
                currentIdx = Integer.parseInt(qId.substring(1)) - 1;
            } catch (NumberFormatException ignored) {}
        }

        // 处理当前回答 - 支持 "A"、"B"、"A. xxx"、"B. xxx" 等多种格式
        boolean isA = text.trim().toUpperCase().startsWith("A");
        boolean isB = text.trim().toUpperCase().startsWith("B");
        
        if (isA || isB) {
            if (currentIdx >= 0 && currentIdx < HollandQuestions.QUESTIONS.size()) {
                HollandQuestions.HollandQuestion q = HollandQuestions.QUESTIONS.get(currentIdx);
                String value = isA ? q.getValueA() : q.getValueB();

                Map<String, Integer> scores;
                try {
                    scores = profile.getHollandScores() != null ?
                        objectMapper.readValue(profile.getHollandScores(), Map.class) : new HashMap<>();
                } catch (Exception e) {
                    scores = new HashMap<>();
                    for (String c : new String[]{"R","I","A","S","E","C"}) scores.put(c, 0);
                }
                scores.put(value, scores.getOrDefault(value, 0) + 1);
                try {
                    profile.setHollandScores(objectMapper.writeValueAsString(scores));
                } catch (Exception ignored) {}

                currentIdx++;
            }
        }

        DialogResponse resp = new DialogResponse();
        resp.setPhase(Phase.HOLLAND.name());

        if (currentIdx >= HollandQuestions.QUESTIONS.size()) {
            // 测评完成，计算结果
            try {
                Map<String, Integer> scores = objectMapper.readValue(profile.getHollandScores(), Map.class);
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
                sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                profile.setHollandPrimary(sorted.get(0).getKey());
                profile.setHollandSecondary(sorted.get(1).getKey());

                int skipped = (int) scores.values().stream().filter(v -> v == 0).count();
                profile.setHollandConfidence(skipped > HollandQuestions.QUESTIONS.size() * 0.3 ? "low" : "high");
            } catch (Exception e) {
                profile.setHollandPrimary("I");
                profile.setHollandSecondary("R");
                profile.setHollandConfidence("low");
            }

            String primaryName = HollandQuestions.TYPE_NAMES.getOrDefault(profile.getHollandPrimary(), profile.getHollandPrimary());
            List<String> majors = HollandQuestions.TYPE_MAJORS.getOrDefault(profile.getHollandPrimary(), Collections.emptyList());

            resp.setResponse("性格测评完成！你的主导类型是" + primaryName + "，适配专业包括：" + String.join("、", majors) + "等。接下来我将为你生成志愿意向方案。");
            profile.setDialogPhase(Phase.GENERATE.name());
            profile.setCurrentQuestionId("generate");
            resp.setPhase(Phase.GENERATE.name());
            resp.setNextAction("show_result");
        } else {
            HollandQuestions.HollandQuestion nextQ = HollandQuestions.QUESTIONS.get(currentIdx);
            resp.setResponse("第" + (currentIdx + 1) + "题 / 共12题：" + nextQ.getScenario());
            resp.setOptions(Arrays.asList(
                nextQ.getOptionA(),
                nextQ.getOptionB()
            ));
            profile.setCurrentQuestionId("H" + (currentIdx + 1));
            resp.setNextQuestionId("H" + (currentIdx + 1));
            resp.setNextAction("voice_input");
        }

        return resp;
    }

    // === 触发生成 ===
    private DialogResponse triggerGeneration(StudentProfile profile, String text) {
        DialogResponse resp = new DialogResponse();
        resp.setPhase(Phase.GENERATE.name());

        if (text.contains("好") || text.contains("可以") || text.contains("生成") || text.contains("确认")) {
            resp.setResponse("正在为你生成志愿意向方案，请稍等...");
            resp.setNextAction("show_result");
            profile.setDialogPhase(Phase.REPORT.name());
            resp.setPhase(Phase.REPORT.name());
        } else if (text.contains("不") || text.contains("改")) {
            resp.setResponse("好的，你想修改哪些信息？可以说「改省份」「改分数」「重做性格测试」等。");
            resp.setNextAction("voice_input");
        } else {
            resp.setResponse("输入「确认」开始生成志愿意向方案，或告诉我你想修改什么。");
            resp.setNextAction("voice_input");
        }

        return resp;
    }

    // === 修正处理 ===
    private DialogResponse handleCorrection(StudentProfile profile, String text, Phase phase) {
        DialogResponse resp = new DialogResponse();
        resp.setPhase(phase.name());
        resp.setResponse("好的，我已经标记上一条回答为废弃。请重新回答：" + getLastQuestionText(profile));
        resp.setNextAction("voice_input");
        return resp;
    }

    // === 辅助方法 ===
    private Intent classifyIntent(String text) {
        if (text == null || text.isEmpty()) return Intent.INFO_PROVIDE;
        if (text.contains("刚才") && (text.contains("不算") || text.contains("改") || text.contains("不对")) ||
            text.contains("我改一下") || text.contains("不对，其实"))
            return Intent.CORRECTION;
        if (text.contains("？") || text.contains("为什么") || text.contains("怎么"))
            return Intent.FOLLOW_UP;
        if (text.contains("好") || text.contains("对") || text.contains("确认") || text.contains("没问题"))
            return Intent.CONFIRM;
        if (text.contains("跳过") || text.contains("下一个") || text.contains("换一个"))
            return Intent.TOPIC_JUMP;
        return Intent.INFO_PROVIDE;
    }

    private double calculateIntentConfidence(String text, Intent intent) {
        if (text.length() < 3) return 0.4;
        if (text.length() > 50) return 0.9;
        return 0.75;
    }

    private int matchAnswer(String text, List<String> options) {
        if (text == null) return -1;
        for (int i = 0; i < options.size(); i++) {
            if (text.contains(options.get(i))) return i;
        }
        // 模糊匹配
        if (text.contains("是") || text.contains("能") || text.contains("好")) return 0;
        if (text.contains("否") || text.contains("不能") || text.contains("不行")) return 1;
        return -1;
    }

    private String extractProvince(String text) {
        String[] provinces = {"四川", "河南", "山东", "广东", "河北", "湖南", "湖北", "安徽", "江苏", "浙江",
            "福建", "江西", "陕西", "重庆", "辽宁", "吉林", "黑龙江", "山西", "广西", "云南",
            "贵州", "甘肃", "海南", "宁夏", "青海", "新疆", "内蒙古", "西藏", "北京", "上海", "天津"};
        for (String p : provinces) {
            if (text.contains(p)) return p;
        }
        return null;
    }

    private int extractYear(String text) {
        if (text.contains("2026")) return 2026;
        if (text.contains("2025")) return 2025;
        if (text.contains("2024")) return 2024;
        return 2026;
    }

    private String extractSubjectComb(String text) {
        String[] subjects = {"物化生", "物化地", "物化政", "物生地", "物生政", "物地政",
            "史化生", "史化地", "史化政", "史生地", "史生政", "史地政"};
        for (String s : subjects) {
            if (text.contains(s)) return s;
        }
        if (text.contains("物理") && text.contains("化学")) return "物化+";
        if (text.contains("历史")) return "历史类";
        return null;
    }

    private Integer extractNumber(String text) {
        try {
            // 处理范围型输入，如 "550-600分"、"10000-30000"，取中间值
            if (text.matches(".*\\d+\\s*[-~～]\\s*\\d+.*")) {
                String[] parts = text.split("[-~～]");
                int n1 = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                int n2 = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                return (n1 + n2) / 2;
            }
            // 处理 "以上" "以内" 等，如 "600分以上" → 600, "10000以内" → 10000
            String num = text.replaceAll("[^0-9]", "");
            if (num.isEmpty()) return null;
            return Integer.parseInt(num);
        } catch (Exception e) {
            return null;
        }
    }

    private void setProfileField(StudentProfile profile, String field, String value) {
        switch (field) {
            case "economy_type": profile.setEconomyType(value); break;
            case "resource_type": profile.setResourceType(value); break;
            case "strategy_type": profile.setStrategyType(value); break;
            case "gender": profile.setGender(value); break;
            case "social_preference": profile.setSocialPreference(value); break;
            case "postgrad_intent": profile.setPostgradIntent(value); break;
            case "city_preference": profile.setCityPreference(value); break;
            case "parent_value": profile.setParentValue(value); break;
            case "medical": profile.setMedical(value); break;
            case "agriculture": profile.setAgriculture(value); break;
            case "normal": profile.setNormal(value); break;
            case "liberal_arts_major": profile.setLiberalArtsMajor(value); break;
            case "chemistry_related": profile.setChemistryRelated(value); break;
            case "physics_related": profile.setPhysicsRelated(value); break;
            case "engineering_sub": profile.setEngineeringSub(value); break;
            case "math_major": profile.setMathMajor(value); break;
        }
    }

    private String getBaseField(String qId) {
        switch (qId) {
            case "P1": return "medical";
            case "P2": return "agriculture";
            case "P3": return "normal";
            default: return qId;
        }
    }

    private String getLastQuestionText(StudentProfile profile) {
        String qId = profile.getCurrentQuestionId();
        if (qId == null) return "请重新回答";
        if (qId.startsWith("Q")) {
            int idx = Integer.parseInt(qId.substring(1)) - 1;
            if (idx >= 0 && idx < ZhangXuefengQuestions.FAMILY_QUESTIONS.size())
                return ZhangXuefengQuestions.FAMILY_QUESTIONS.get(idx).getText();
        }
        if (qId.startsWith("P")) {
            int idx = Integer.parseInt(qId.substring(1)) - 1;
            if (idx >= 0 && idx < ZhangXuefengQuestions.MAJOR_QUESTIONS.size())
                return ZhangXuefengQuestions.MAJOR_QUESTIONS.get(idx).getText();
        }
        return "请重新回答";
    }

    private StudentProfile getOrCreateProfile(Long userId) {
        return profileRepo.findByUserId(userId).orElseGet(() -> {
            StudentProfile p = StudentProfile.builder()
                .userId(userId)
                .dialogPhase(Phase.IDLE.name())
                .reportVersion(0)
                .build();
            return profileRepo.save(p);
        });
    }

    private void saveDialogHistory(StudentProfile profile, String turn) {
        String history = profile.getDialogTurns() != null ? profile.getDialogTurns() : "[]";
        try {
            List<String> turns = objectMapper.readValue(history, List.class);
            turns.add(turn);
            // 保留20轮，超出压缩
            if (turns.size() > MAX_TURNS) {
                List<String> compressed = turns.subList(turns.size() - MAX_TURNS, turns.size());
                turns = new ArrayList<>(compressed);
            }
            profile.setDialogTurns(objectMapper.writeValueAsString(turns));
        } catch (Exception e) {
            profile.setDialogTurns("[\"" + turn + "\"]");
        }
    }
}
