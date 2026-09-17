package com.example.gaokao.data;

import lombok.Data;
import java.util.*;

/**
 * 霍兰德RIASEC简化版12题 - 聊天式
 * PRD 3.4节 - MVP版
 */
public class HollandQuestions {

    // 六种类型: R(实用型) I(研究型) A(艺术型) S(社会型) E(企业型) C(常规型)
    public static final List<HollandQuestion> QUESTIONS = Arrays.asList(
        new HollandQuestion("H1",
            "你周末更愿意：",
            "A 拆装旧电器或做手工",
            "B 写短篇小说或画画",
            "R", "A"),
        new HollandQuestion("H2",
            "小组讨论中，你通常：",
            "A 整理数据做分析",
            "B 协调不同意见推动达成共识",
            "I", "S"),
        new HollandQuestion("H3",
            "你更欣赏哪种人：",
            "A 白手起家的企业家",
            "B 钻研难题的科学家",
            "E", "I"),
        new HollandQuestion("H4",
            "面对新环境，你会：",
            "A 先观察规律再行动",
            "B 直接找人聊天了解情况",
            "C", "S"),
        new HollandQuestion("H5",
            "你更喜欢哪种课：",
            "A 动手实验课",
            "B 设计创作课",
            "R", "A"),
        new HollandQuestion("H6",
            "你觉得更重要的事：",
            "A 把事情做到完美",
            "B 让团队达成目标",
            "C", "E"),
        new HollandQuestion("H7",
            "你倾向于：",
            "A 研究事物为什么这样运作",
            "B 帮助别人解决困难",
            "I", "S"),
        new HollandQuestion("H8",
            "你更想做：",
            "A 管理一个项目团队",
            "B 独立完成一项精密任务",
            "E", "C"),
        new HollandQuestion("H9",
            "你理想的工作环境：",
            "A 户外或车间，能看到实物成果",
            "B 安静的书房或实验室",
            "R", "I"),
        new HollandQuestion("H10",
            "你处理问题更依赖：",
            "A 逻辑和证据",
            "B 直觉和美感",
            "I", "A"),
        new HollandQuestion("H11",
            "你更看重：",
            "A 稳定的收入和明确的发展路径",
            "B 自由发挥创意的空间",
            "C", "A"),
        new HollandQuestion("H12",
            "你带团队时更注重：",
            "A 规则和流程的执行",
            "B 激励成员和沟通协调",
            "C", "S")
    );

    @Data
    public static class HollandQuestion {
        public String id;
        public String scenario;
        public String optionA;
        public String optionB;
        public String valueA;
        public String valueB;

        public HollandQuestion(String id, String scenario, String optionA, String optionB, String valueA, String valueB) {
            this.id = id;
            this.scenario = scenario;
            this.optionA = optionA;
            this.optionB = optionB;
            this.valueA = valueA;
            this.valueB = valueB;
        }
    }

    /**
     * 霍兰德类型中文名
     */
    public static final Map<String, String> TYPE_NAMES = new HashMap<>() {{
        put("R", "实用型（Realistic）");
        put("I", "研究型（Investigative）");
        put("A", "艺术型（Artistic）");
        put("S", "社会型（Social）");
        put("E", "企业型（Enterprising）");
        put("C", "常规型（Conventional）");
    }};

    /**
     * 霍兰德类型 -> 适配专业簇
     */
    public static final Map<String, List<String>> TYPE_MAJORS = new HashMap<>() {{
        put("R", Arrays.asList("机械工程", "土木工程", "电气工程", "农学", "交通运输", "航空航天工程"));
        put("I", Arrays.asList("数学", "物理学", "化学", "生物科学", "计算机科学", "统计学", "医学"));
        put("A", Arrays.asList("汉语言文学", "新闻传播", "设计学", "音乐", "美术", "戏剧影视"));
        put("S", Arrays.asList("教育学", "心理学", "社会工作", "护理学", "公共管理", "法学"));
        put("E", Arrays.asList("工商管理", "市场营销", "金融学", "经济学", "国际商务", "人力资源管理"));
        put("C", Arrays.asList("会计学", "财务管理", "审计学", "统计学", "信息管理", "物流管理"));
    }};

    /**
     * 计算霍兰德类型
     */
    public static HollandResult calculate(List<String> answers) {
        Map<String, Integer> scores = new HashMap<>();
        for (String code : new String[]{"R", "I", "A", "S", "E", "C"}) {
            scores.put(code, 0);
        }

        for (int i = 0; i < answers.size() && i < QUESTIONS.size(); i++) {
            String choice = answers.get(i);
            HollandQuestion q = QUESTIONS.get(i);
            String value = choice.equals("A") ? q.valueA : q.valueB;
            scores.put(value, scores.get(value) + 1);
        }

        // 排序找主次
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String primary = sorted.get(0).getKey();
        String secondary = sorted.get(1).getKey();

        // 缺失值检查
        int skipped = (int) answers.stream().filter(a -> a == null || a.isEmpty()).count();
        String confidence = skipped > QUESTIONS.size() * 0.3 ? "low" : "high";

        return new HollandResult(primary, secondary, scores, confidence);
    }

    @Data
    public static class HollandResult {
        public String primary;
        public String secondary;
        public Map<String, Integer> scores;
        public String confidence;

        public HollandResult(String primary, String secondary, Map<String, Integer> scores, String confidence) {
            this.primary = primary;
            this.secondary = secondary;
            this.scores = scores;
            this.confidence = confidence;
        }
    }
}
