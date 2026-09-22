package com.example.gaokao.data;

import lombok.Data;
import java.util.*;

/**
 * 张雪峰八问 + 专业筛选八问 - 静态题库
 * PRD 3.3节
 */
public class ZhangXuefengQuestions {

    // 家庭现实八问
    public static final List<Question> FAMILY_QUESTIONS = Arrays.asList(
        new Question("Q1", "家里经济条件能否承受长学制（如医学5+3）或高学费专业？",
            Arrays.asList("能", "勉强", "不能"),
            "economy_type", new String[]{"宽松型", "承压型", "承压型"}),
        new Question("Q2", "家里有没有相关行业人脉或资源？",
            Arrays.asList("有", "没有", "不确定"),
            "resource_type", new String[]{"资源型", "无资源型", "无资源型"}),
        new Question("Q3", "当前分数定位更接近哪种？",
            Arrays.asList("冲名校", "保专业", "有学上"),
            "strategy_type", new String[]{"名校导向", "专业导向", "兜底导向"}),
        new Question("Q4", "性别（用于就业现实提示，非限制）",
            Arrays.asList("男", "女", "不愿透露"),
            "gender", new String[]{"男", "女", "不愿透露"}),
        new Question("Q5", "性格偏内向还是外向？",
            Arrays.asList("内向", "外向", "中间"),
            "social_preference", new String[]{"内向", "外向", "中间"}),
        new Question("Q6", "未来是否计划考研？",
            Arrays.asList("是", "否", "不确定"),
            "postgrad_intent", new String[]{"考研导向", "就业导向", "未定"}),
        new Question("Q7", "想去什么类型的城市？",
            Arrays.asList("一线", "新一线", "老家", "不确定"),
            "city_preference", new String[]{"一线", "新一线", "老家", "city_open"}),
        new Question("Q8", "家长最看重什么？",
            Arrays.asList("稳定", "高薪", "兴趣", "名校平台"),
            "parent_value", new String[]{"稳定", "高薪", "兴趣", "名校平台"})
    );

    // 专业筛选八问
    public static final List<Question> MAJOR_QUESTIONS = Arrays.asList(
        new Question("P1", "是否考虑学医？",
            Arrays.asList("是", "否", "犹豫"),
            "medical", new String[]{"白名单", "黑名单", "待定"}),
        new Question("P2", "是否考虑学农？",
            Arrays.asList("是", "否", "犹豫"),
            "agriculture", new String[]{"白名单", "黑名单", "待定"}),
        new Question("P3", "是否考虑当老师？",
            Arrays.asList("是", "否", "犹豫"),
            "normal", new String[]{"白名单", "黑名单", "待定"}),
        new Question("P4", "能否接受文理兼招专业（经管法新传等）？",
            Arrays.asList("能", "不能", "优先考虑"),
            "liberal_arts_major", new String[]{"接受", "排除", "偏好"}),
        new Question("P5", "能否接受带化学的专业？",
            Arrays.asList("能", "不能"),
            "chemistry_related", new String[]{"接受", "排除"}),
        new Question("P6", "能否接受带物理的专业？",
            Arrays.asList("能", "不能"),
            "physics_related", new String[]{"接受", "排除"}),
        new Question("P7", "物理偏力学还是电学？",
            Arrays.asList("力学", "电学", "都不好"),
            "engineering_sub", new String[]{"机械类", "电气类", "排除"}),
        new Question("P8", "数学主导专业（数学/统计/计算机）能接受吗？",
            Arrays.asList("能", "不能", "勉强"),
            "math_major", new String[]{"白名单", "黑名单", "谨慎"})
    );

    @Data
    public static class Question {
        public String id;
        public String text;
        public List<String> options;
        public String field;
        public String[] values;

        public Question(String id, String text, List<String> options, String field, String[] values) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.field = field;
            this.values = values;
        }
    }

    /**
     * Q7回答"不确定"时追问
     */
    public static final String Q7_FOLLOWUP = "有没有绝对不想去的地方？";

    /**
     * P1-P3回答"是"时追问
     */
    public static final String P_MEDICAL_FOLLOWUP = "能接受硕士起步吗？";
    public static final String P_AGRICULTURE_FOLLOWUP = "能接受硕士起步吗？";
    public static final String P_NORMAL_FOLLOWUP = "能接受硕士起步吗？";

    /**
     * 偏见护栏声明
     */
    public static final String GENDER_DISCLAIMER = "以下基于行业统计概率，不代表个体能力限制";
    public static final String ZHANGXUEFENG_DISCLAIMER = "第三方经验参考，非官方结论";
    public static final String SYSTEM_DISCLAIMER = "AI生成内容仅供参考，最终志愿以官方系统为准";
}
