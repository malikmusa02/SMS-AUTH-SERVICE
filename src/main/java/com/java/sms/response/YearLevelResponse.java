package com.java.sms.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearLevelResponse {
    private Long id;
    @JsonProperty("level_name")
    private String levelName;
    @JsonProperty("level_order")
    private Integer levelOrder;
}
