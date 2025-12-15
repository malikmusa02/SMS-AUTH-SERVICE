package com.java.sms.response;

// com.java.sms.DataClass.DirectorsResponse.java
import com.java.sms.DataClass.DirectorDTO;
import lombok.Data;
import java.util.List;

@Data
public class DirectorsResponse {
    private List<DirectorDTO> directors;
}
