package de.ait.secondlife.controllers.test_dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;

@Data
@Builder
public class TestImagePropsDto {
   private MockMultipartFile testFile;
    private  MultiValueMap<String, String> params;
}
