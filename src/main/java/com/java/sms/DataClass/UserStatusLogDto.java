package com.java.sms.DataClass;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusLogDto {
    private Long id;
    private Long userId;
    private String status;
    private String reason;
    private String timestamp; // ISO string, optional
}

