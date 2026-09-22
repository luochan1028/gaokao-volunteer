package com.example.gaokao.data;

import java.util.*;

/**
 * 模糊表达归一表 - PRD 3.1.4
 * 口语表达 -> 标准字段值
 */
public class FuzzyExpressionMap {

    private static final Map<String, Map<String, String>> RULES = new HashMap<>();

    static {
        // 数学
        addRule("数学还行", "math_ability", "medium");
        addRule("数学不错", "math_ability", "medium");
        addRule("数学一般", "math_ability", "medium");
        addRule("数学很好", "math_ability", "high");
        addRule("数学差", "math_ability", "low");
        addRule("数学不行", "math_ability", "low");
        addRule("数学不太好", "math_ability", "low");

        // 竞争偏好
        addRule("不想太卷", "competition_preference", "low");
        addRule("不想卷", "competition_preference", "low");
        addRule("不想太累", "competition_preference", "low");
        addRule("能卷", "competition_preference", "high");
        addRule("不怕卷", "competition_preference", "high");

        // 家长期望
        addRule("我妈想我当老师", "family_expectation", "normal_university");
        addRule("家里想让我当老师", "family_expectation", "normal_university");
        addRule("家里想让我考公", "family_expectation", "civil_service");
        addRule("家长想让我学医", "family_expectation", "medical");
        addRule("家里想让我学计算机", "family_expectation", "computer");

        // 城市
        addRule("想去大城市", "city_preference", "一线");
        addRule("想去北京", "city_preference", "一线");
        addRule("想去上海", "city_preference", "一线");
        addRule("想去深圳", "city_preference", "一线");
        addRule("想去广州", "city_preference", "一线");
        addRule("想去成都", "city_preference", "新一线");
        addRule("想去杭州", "city_preference", "新一线");
        addRule("想留在老家", "city_preference", "老家");
        addRule("不想离家太远", "city_preference", "老家");

        // 意向
        addRule("随便吧", "undecided", "true");
        addRule("不知道", "undecided", "true");
        addRule("都行", "undecided", "true");
        addRule("没想好", "undecided", "true");

        // 考研
        addRule("想考研", "postgrad_intent", "考研导向");
        addRule("想读研", "postgrad_intent", "考研导向");
        addRule("不想考研", "postgrad_intent", "就业导向");
        addRule("想直接工作", "postgrad_intent", "就业导向");

        // 学医
        addRule("想当医生", "medical", "白名单");
        addRule("想学医", "medical", "白名单");
        addRule("不想学医", "medical", "黑名单");
        addRule("不想当医生", "medical", "黑名单");

        // 计算机
        addRule("想学计算机", "computer_major", "白名单");
        addRule("想学编程", "computer_major", "白名单");
        addRule("想搞IT", "computer_major", "白名单");
    }

    private static void addRule(String expression, String field, String value) {
        RULES.computeIfAbsent(expression, k -> new HashMap<>()).put(field, value);
    }

    /**
     * 从自由文本中提取模糊表达并归一化
     */
    public static List<FuzzyMatch> match(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        List<FuzzyMatch> results = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (Map.Entry<String, Map<String, String>> entry : RULES.entrySet()) {
            if (lowerText.contains(entry.getKey().toLowerCase())) {
                for (Map.Entry<String, String> field : entry.getValue().entrySet()) {
                    results.add(new FuzzyMatch(entry.getKey(), field.getKey(), field.getValue()));
                }
            }
        }

        return results;
    }

    public static class FuzzyMatch {
        public String expression;
        public String field;
        public String value;

        public FuzzyMatch(String expression, String field, String value) {
            this.expression = expression;
            this.field = field;
            this.value = value;
        }
    }
}
