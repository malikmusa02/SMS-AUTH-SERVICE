package com.java.sms.DataClass;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClassPeriodRequest {
    private Long subjectId;
    private Long yearLevelId;
    private Long teacherId;
    private Long termId;
    private Long startTimeId;
    private Long endTimeId;
    private Long classroomId;
    private String name;

    // optional forwarding fields
    private String yearLevelName;
    private List<String> classPeriodNames;
}
