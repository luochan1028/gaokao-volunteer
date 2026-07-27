
package com.example.gaokao.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    private String province;

    private String year;

    private Integer score;

    private Integer rank;

    private String scienceOrArts;

    private String selectedSubjects;

    private List<String> preferredMajors;

    private List<String> preferredProvinces;

    private String selectionMode;

    private Integer chongCount;

    private Integer wenCount;

    private Integer baoCount;

    private Boolean include985;

    private Boolean include211;

    private Boolean includeDoubleFirstClass;

    private Boolean includePrivate;
}
