package com.java.sms.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponseDTO {
    private Long id;
    private Long user;
    private String joining_date;  // match Django field names
}

