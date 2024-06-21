package de.ait.secondlife.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.controllers.test_dto.TestImagePropsDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;


import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Image integration tests:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImagesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    private Long createdOfferId;
    private Long createdCategoryId;
    private Long createdUserId;

    private Cookie userCookie;
    private Cookie adminCookie;

    @BeforeEach
    public void setup() throws Exception {

        MvcResult registerUserResult = mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "NewUserFirstName",
                                  "lastName": "NewUserLastName",
                                  "email": "newUser.user1@mail.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponse = registerUserResult.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(jsonResponse);
        createdUserId = jsonNode.get("id").asLong();

        MvcResult authAdminResult = mockMvc.perform(post("/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@email.com",
                                  "password": "Security!234"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = authAdminResult.getResponse().getCookie("Access-Token").getValue();
        adminCookie = new Cookie("Access-Token", adminToken);

        MvcResult authUserResult = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "newUser.user1@mail.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();
        String userToken = authUserResult.getResponse().getCookie("Access-Token").getValue();
        userCookie = new Cookie("Access-Token", userToken);

        MvcResult creatingOffer = mockMvc.perform(post("/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test offer title",
                                  "description": "Test offer description",
                                  "auctionDurationDays": 3,
                                  "startPrice": 2000,
                                  "winBid": 3000,
                                  "isFree": false,
                                  "categoryId": 3,
                                  "locationId": 4,
                                  "sendToVerification": false
                                }""")
                        .cookie(userCookie))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponseOffer = creatingOffer.getResponse().getContentAsString();
        JsonNode jsonNodeOffer = mapper.readTree(jsonResponseOffer);
        createdOfferId = jsonNodeOffer.get("id").asLong();

        MvcResult creatingCategory = mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "name": "Test category",
                                 "description": "Test description"
                                }""")
                        .cookie(adminCookie))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponseCategory = creatingCategory.getResponse().getContentAsString();
        JsonNode jsonNodeCategory = mapper.readTree(jsonResponseCategory);
        createdCategoryId = jsonNodeCategory.get("id").asLong();
        System.out.println();
    }

    private TestImagePropsDto getTestImagePropsForSettingImage(
            String entityType, Long entityId, String testImagePath) throws Exception {
        File imageFile = ResourceUtils.getFile(testImagePath);
        byte[] imageBytes = null;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            imageBytes = fis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MockMultipartFile testFile = new MockMultipartFile(
                "file",
                imageFile.getName(),
                MediaType.IMAGE_JPEG_VALUE,
                imageBytes
        );
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("entityType", entityType);
        params.add("entityId", String.valueOf(entityId));

        return TestImagePropsDto.builder()
                .testFile(testFile)
                .params(params)
                .build();
    }

    private void setImageWithResponse200(String entityType, Long entityId) throws Exception {
        TestImagePropsDto imageProps = getTestImagePropsForSettingImage(entityType, entityId, "classpath:test_image/testImg.jpeg");

        mockMvc.perform(multipart("/v1/images")
                        .file(imageProps.getTestFile())
                        .params(imageProps.getParams())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .cookie(userCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.values").isMap())
                .andExpect(jsonPath("$.values[*]").isArray())
                .andExpect(jsonPath("$.values[*][*]").isArray())
                .andExpect(jsonPath("$.values[*][*]").value(everyItem(startsWith("http"))))
                .andExpect(jsonPath("$.values[*]['64x64']").exists());
    }

    @Nested
    @DisplayName("POST /v1/images")
    @Transactional
    @Rollback
    public class UploadImageTest {

        @Test
               public void upload_image_for_offer_return_200_and_paths_of_images() throws Exception {
            setImageWithResponse200(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void upload_image_for_user_return_200_and_paths_of_images() throws Exception {
            setImageWithResponse200(EntityTypeWithImages.USER.getType(), createdUserId);
        }

        @Test
        public void upload_image_for_category_return_200_and_paths_of_images() throws Exception {
            setImageWithResponse200(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }

        @Test
        public void return_400_if_image_file_is_empty() throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    EntityTypeWithImages.CATEGORY.getType(), createdCategoryId,
                    "classpath:test_image/empty.jpeg");

            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(userCookie))
                    .andExpect(status().isBadRequest());
        }
    }

//    @Nested
//    @DisplayName("DELETE /v1/images")
//    public class DeleteImageTest {
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
//            params.add("entityType", EntityTypeWithImages.OFFER.getType());
//            params.add("entityId", String.valueOf(createdEntityId));
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
//            MvcResult result = mockMvc.perform(get("/v1/offers/" + createdEntityId)
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
}