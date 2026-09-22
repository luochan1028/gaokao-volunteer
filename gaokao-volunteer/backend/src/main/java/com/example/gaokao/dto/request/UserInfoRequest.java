
package com.example.gaokao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "考试年份不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "年份格式不正确")
    private String examYear;

    @NotBlank(message = "选考科目不能为空")
    private String selectedSubjects;

    private String subjectDetails;

    private Integer totalScore;

    private Integer rank;

    private String scienceOrArts;

    private String idCard;

    private String userType;
}
