package com.example.gaokao.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * V2.0 对话请求 - 语音/文字消息
 */
@Data
public class DialogRequest {
    private Long userId;
    private String text;          // 用户输入文本（ASR后或直接输入）
    private String phase;         // 当前阶段
    private String questionId;    // 当前问题ID
    private String method;        // voice / tap
    private Map<String, Object> extra; // 额外数据
}
