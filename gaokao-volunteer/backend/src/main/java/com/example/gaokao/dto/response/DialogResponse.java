package com.example.gaokao.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * V2.0 对话响应 - AI回复+更新后的状态
 */
@Data
public class DialogResponse {
    private String response;       // AI回复文本
    private String phase;          // 当前阶段
    private String nextAction;     // 下一步动作
    private String nextQuestionId; // 下一个问题ID
    private String intent;         // 意图分类结果
    private Double intentConfidence; // 意图置信度
    private Map<String, Object> slots; // 已采集的槽位
    private String needConfirm;    // 需要用户确认的内容
    private List<String> options;  // 可选项（如有）
    private String dialogHistory;  // 对话历史摘要
}
