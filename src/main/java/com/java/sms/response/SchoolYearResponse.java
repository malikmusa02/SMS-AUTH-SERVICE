package com.java.sms.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolYearResponse {
    private Long id;

    @JsonProperty("year_name")
    private String yearName;
}
