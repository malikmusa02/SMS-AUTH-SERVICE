package com.java.sms.DataClass;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssignRequest {
    private String yearLevelName;
    private List<String> classPeriodNames;
}
