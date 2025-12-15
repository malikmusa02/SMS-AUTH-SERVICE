package com.java.sms.DataClass;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicResponse {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
}
