//package de.ait.secondlife.controllers;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import de.ait.secondlife.constants.EntityTypeWithImgs;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.util.ResourceUtils;
//
//import static org.hamcrest.Matchers.is;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Map;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@DisplayName("Image integration tests:")
//@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
//@Transactional
//@Rollback
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class ImagesIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    private ObjectMapper mapper = new ObjectMapper();
//    Long createdOfferId;
//    private String token;
//
//    @BeforeAll
//    public void setToken() throws Exception {
//        MvcResult authResult = mockMvc.perform(post("/v1/auth/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                 "email": "barak.obama@email.com",
//                                  "password": "Security!234"
//                                }"""))
//                .andExpect(status().isOk())
//                .andReturn();
//        String authResponse = authResult.getResponse().getContentAsString();
//        Map<String, String> authMap = mapper.readValue(authResponse, new TypeReference<>() {
//        });
//        token = authMap.get(("accessToken"));
//
//        MvcResult result = mockMvc.perform(post("/v1/offers")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + token)
//                        .content("""
//                                  {
//                                  "title": "1newoffer",
//                                  "description": "1asfdfadsgdgfgdfgasdasdadadfgdfgdfgdgdag1",
//                                  "auctionDurationDays": 3,
//                                  "startPrice": 2000,
//                                  "step": 4,
//                                  "winBid": 90,
//                                  "isFree":false,
//                                  "userId": 1,
//                                  "categoryId": 3,
//                                  "locationId": 2
//                                }"""))
//                .andExpect(status().isCreated())
//                .andReturn();
//        String response = result.getResponse().getContentAsString();
//        createdOfferId = mapper.readTree(response).get("id").asLong();
//    }
//
//    @Nested
//    @DisplayName("POST /v1/images/upload")
//    public class UploadImageTest {
//        @Test
//        public void set_image_for_offer() throws Exception {
//
//            File imageFile = ResourceUtils.getFile("classpath:test_image/testImg.jpeg");
//            byte[] imageBytes = null;
//            try (FileInputStream fis = new FileInputStream(imageFile)) {
//                imageBytes = fis.readAllBytes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            MockMultipartFile testFile = new MockMultipartFile(
//                    "file",
//                    imageFile.getName(),
//                    MediaType.IMAGE_JPEG_VALUE,
//                    imageBytes
//            );
//            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//            params.add("entityType", EntityTypeWithImgs.OFFER.getType());
//            params.add("entityId", String.valueOf(createdOfferId));
//
//            mockMvc.perform(multipart("/v1/images/upload")
//                            .file(testFile)
//                            .params(params)
//                            .header("Authorization", "Bearer " + token)
//                            .contentType(MediaType.MULTIPART_FORM_DATA)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message", is("Image(s) successful saved")));
//        }
//    }
//
//    @Nested
//    @DisplayName("DELETE /v1/images")
//     public class DeleteImageTest {
//
//        String fileName;
//
//        @BeforeEach
//        public void setup() throws Exception {
//            File imageFile = ResourceUtils.getFile("classpath:test_image/testImg.jpeg");
//            byte[] imageBytes = null;
//            try (FileInputStream fis = new FileInputStream(imageFile)) {
//                imageBytes = fis.readAllBytes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            MockMultipartFile testFile = new MockMultipartFile(
//                    "file",
//                    imageFile.getName(),
//                    MediaType.IMAGE_JPEG_VALUE,
//                    imageBytes
//            );
//            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//            params.add("entityType", EntityType.OFFER.getType());
//            params.add("entityId", String.valueOf(createdOfferId));
//
//            mockMvc.perform(multipart("/v1/images/upload")
//                            .file(testFile)
//                            .params(params)
//                            .header("Authorization", "Bearer " + token)
//                            .contentType(MediaType.MULTIPART_FORM_DATA)
//                    )
//                    .andExpect(status().isOk());
//
//
//            MvcResult result = mockMvc.perform(get("/v1/offers/" + createdOfferId)
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andReturn();
//            String response = result.getResponse().getContentAsString();
//
//            fileName = mapper.readTree(response)
//                    .get("images")
//                    .get("images")
//                    .get("0")
//                    .get("320x320").toString();
//
//            fileName = fileName.replaceAll("^\"|\"$", "");
//        }
//
//        @Test
//        public void delete_image_for_offer() throws Exception {
//            ImageRequestDto imageRequestDto = new ImageRequestDto(fileName.replaceAll("\"", ""));
//
//            String json = mapper.writeValueAsString(imageRequestDto);
//            mockMvc.perform(delete("/v1/images")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + token)
//                            .content(json))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message", is("Image deleted successfully")));
//        }
//    }
//}