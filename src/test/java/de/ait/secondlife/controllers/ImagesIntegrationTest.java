package de.ait.secondlife.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.ImageConstants;
import de.ait.secondlife.controllers.test_dto.TestImagePropsDto;
import de.ait.secondlife.domain.dto.ImageRequestDto;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Image integration tests:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
public class ImagesIntegrationTest implements ImageConstants {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    private Long createdOfferId;
    private Long createdCategoryId;
    private Long createdUser1Id;
    private Long createdUser2Id;

    private Cookie userCookie1;
    private Cookie userCookie2;
    private Cookie adminCookie;

    @BeforeEach
    public void setup() throws Exception {

        MvcResult registerUserResult1 = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "barak.obama@email.com",
                                  "password": "Security!234"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();
        String userToken1 = registerUserResult1.getResponse().getCookie("Access-Token").getValue();
        userCookie1 = new Cookie("Access-Token", userToken1);
        createdUser1Id =1L;

        MvcResult registerUserResult2 = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john.smith@email.com",
                                  "password": "Security!234"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();
        String userToken2 = registerUserResult2.getResponse().getCookie("Access-Token").getValue();
        userCookie2 = new Cookie("Access-Token", userToken2);
        createdUser2Id =2L;

        MvcResult authAdminResult = mockMvc.perform(post("/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@second-life.space",
                                  "password": "Security!234"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = authAdminResult.getResponse().getCookie("Access-Token").getValue();
        adminCookie = new Cookie("Access-Token", adminToken);

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
                        .cookie(userCookie1))
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

        mockMvc.perform(patch("/v1/categories/" + createdCategoryId + "/set-active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(adminCookie))
                .andExpect(status().isOk());
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
        if (entityId != null) params.add("entityId", String.valueOf(entityId));

        return TestImagePropsDto.builder()
                .testFile(testFile)
                .params(params)
                .build();
    }

    private void setImageAndGet200(String entityType, Long entityId) throws Exception {
        TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                entityType,
                entityId,
                "classpath:test_image/testImg.jpeg");

        mockMvc.perform(multipart("/v1/images")
                        .file(imageProps.getTestFile())
                        .params(imageProps.getParams())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .cookie(getCookieByEntityType(entityType)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.values").isMap())
                .andExpect(jsonPath("$.values[*]").isArray())
                .andExpect(jsonPath("$.values[*][*]").isArray())
                .andExpect(jsonPath("$.values[*][*]").value(everyItem(startsWith("http"))))
                .andExpect(jsonPath("$.values[*]['320x320']").exists());
    }

    private Cookie getCookieByEntityType(String entityType) {
        return switch (EntityTypeWithImages.get(entityType.toLowerCase())) {
            case OFFER, USER -> userCookie1;
            case CATEGORY -> adminCookie;
        };
    }

    @Nested
    @DisplayName("POST /v1/images")
    @Transactional
    @Rollback
    public class UploadImageTest {

        private void setImageAndGet400(TestImagePropsDto imageProps, String entityType) throws Exception {

            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(getCookieByEntityType(entityType)))
                    .andExpect(status().isBadRequest());
        }

        private void setImageForEntity400(String entityType, Long entityId) throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    entityType, entityId, "classpath:test_image/empty.jpeg");
            setImageAndGet400(imageProps, entityType);
        }

        private TestImagePropsDto getTestImagePropsForMAX_FILE_SIZE(
                String entityType, Long entityId) {
            long fileSizeInBytes = MAX_FILE_SIZE + 1;
            byte[] largeFileBytes = new byte[(int) fileSizeInBytes];
            new Random().nextBytes(largeFileBytes);

            MockMultipartFile largeTestFile = new MockMultipartFile(
                    "file",
                    "largeTestImage.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    largeFileBytes);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("entityType", entityType);
            if (entityId != null) params.add("entityId", String.valueOf(entityId));
            return TestImagePropsDto.builder()
                    .testFile(largeTestFile)
                    .params(params)
                    .build();
        }

        private void setLargeImageForEntity(String entityType, Long entityId) throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForMAX_FILE_SIZE(
                    entityType, entityId);
            setImageAndGet400(imageProps, entityType);
        }

        private void setImageWithResponse403(String entityType, Long entityId) throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    entityType, entityId, "classpath:test_image/testImg.jpeg");

            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(userCookie2))
                    .andExpect(status().isForbidden());
        }

        private void setImageWithResponse200IfAnotherUser(String entityType, Long entityId) throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    entityType, entityId, "classpath:test_image/testImg.jpeg");

            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(userCookie2))
                    .andExpect(status().isOk());
        }

        private void setImageWithResponse400IfCountOfImageIsGreaterThanMaxCount(
                String entityType, Long entityId, int maxCount) throws Exception {
            for (int i = 0; i < maxCount; i++) {
                setImageAndGet200(entityType, entityId);
            }
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    entityType, entityId, "classpath:test_image/testImg.jpeg");
            setImageAndGet400(imageProps, entityType);
        }

        private void setImageWithResponse200IfCountOfImageIsGreaterThanMaxCount(
                String entityType, Long entityId, int maxCount) throws Exception {
            for (int i = 0; i < maxCount + 1; i++) {
                setImageAndGet200(entityType, entityId);
            }
        }

        private void setImageAndGet404(TestImagePropsDto imageProps) throws Exception {
            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(userCookie1))
                    .andExpect(status().isNotFound());
        }

        private void setImageAndGet400IfImageMediaTypeNull(
                String entityType, Long entityId) throws Exception {
            String testImagePath = "classpath:test_image/testImg.jpeg";
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
                    null,
                    imageBytes
            );
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("entityType", entityType);
            if (entityId != null) params.add("entityId", String.valueOf(entityId));

            setImageAndGet400(TestImagePropsDto.builder()
                    .testFile(testFile)
                    .params(params)
                    .build(), entityType);
        }

        private void setImageAndGet400IfImageMediaTypeWrong(
                String entityType, Long entityId) throws Exception {
            String testImagePath = "classpath:test_image/testImg.jpeg";
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
                    "WrongContentType",
                    imageBytes
            );
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("entityType", entityType);
            if (entityId != null) params.add("entityId", String.valueOf(entityId));

            setImageAndGet400(TestImagePropsDto.builder()
                    .testFile(testFile)
                    .params(params)
                    .build(), entityType);
        }

        private TestImagePropsDto getTestImagePropsIfEntityTypeIsNull(
                Long entityId) throws Exception {
            File imageFile = ResourceUtils.getFile("classpath:test_image/testImg.jpeg");
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
            if (entityId != null) params.add("entityId", String.valueOf(entityId));

            return TestImagePropsDto.builder()
                    .testFile(testFile)
                    .params(params)
                    .build();
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_offer() throws Exception {
            setImageAndGet200(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_user() throws Exception {
            setImageAndGet200(EntityTypeWithImages.USER.getType(), createdUser1Id);
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_category() throws Exception {
            setImageAndGet200(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_offer_without_entity_id() throws Exception {
            setImageAndGet200(EntityTypeWithImages.OFFER.getType(), null);
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_category_without_entity_id() throws Exception {
            setImageAndGet200(EntityTypeWithImages.CATEGORY.getType(), null);
        }

        @Test
        public void return_200_and_paths_of_images_upload_image_for_user_without_entity_id() throws Exception {
            setImageAndGet200(EntityTypeWithImages.USER.getType(), null);
        }

        @Test
        public void return_400_if_entity_type_is_null() throws Exception {
            TestImagePropsDto imageProps= getTestImagePropsIfEntityTypeIsNull(createdOfferId);

            mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(userCookie1))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void return_400_if_image_file_for_category_is_empty() throws Exception {
            setImageForEntity400(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }

        @Test
        public void return_400_if_image_file_for_user_is_empty() throws Exception {
            setImageForEntity400(EntityTypeWithImages.USER.getType(), createdUser1Id);
        }

        @Test
        public void return_400_if_image_file_for_offer_is_empty() throws Exception {
            setImageForEntity400(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void return_400_if_image_file_for_category_is_empty_and_if_entity_id_is_null() throws Exception {
            setImageForEntity400(EntityTypeWithImages.CATEGORY.getType(), null);
        }

        @Test
        public void return_400_if_image_file_for_user_is_empty_and_if_entity_id_is_null() throws Exception {
            setImageForEntity400(EntityTypeWithImages.USER.getType(), null);
        }

        @Test
        public void return_400_if_image_file_for_offer_is_empty_and_if_entity_id_is_null() throws Exception {
            setImageForEntity400(EntityTypeWithImages.OFFER.getType(), null);
        }

        @Test
        public void return_400_if_image_file_for_category_is_greater_than_MAX_FILE_SIZE() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }

        @Test
        public void return_400_if_image_file_for_user_is_greater_than_MAX_FILE_SIZE() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.USER.getType(), createdUser1Id);
        }

        @Test
        public void return_400_if_image_file_for_offer_is_greater_than_MAX_FILE_SIZE() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void return_400_if_image_file_for_category_is_greater_than_MAX_FILE_SIZE_and_if_entity_id_is_null() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.CATEGORY.getType(), null);
        }

        @Test
        public void return_400_if_image_file_for_user_is_greater_than_MAX_FILE_SIZE_and_if_entity_id_is_null() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.USER.getType(), null);
        }

        @Test
        public void return_400_if_image_file_for_offer_is_greater_than_MAX_FILE_SIZE_and_if_entity_id_is_null() throws Exception {
            setLargeImageForEntity(EntityTypeWithImages.OFFER.getType(), null);
        }

        @Test
        public void return_403_user_does_not_have_enough_rights_for_save_image_to_offer() throws Exception {
            setImageWithResponse403(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void return_403_user_does_not_have_enough_rights_for_save_image_to_user() throws Exception {
            setImageWithResponse403(EntityTypeWithImages.USER.getType(), createdUser1Id);
        }

        @Test
        public void  return_403_user_does_not_have_enough_rights_for_save_image_to_category() throws Exception {
            setImageWithResponse403(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }
        @Test
        public void return_200_user_have_enough_rights_for_save_image_to_offer_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfAnotherUser(EntityTypeWithImages.OFFER.getType(), null);
        }

        @Test
        public void return_200_user_have_enough_rights_for_save_image_to_user_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfAnotherUser(EntityTypeWithImages.USER.getType(), null);
        }

        @Test
        public void return_200_user_have_enough_rights_for_save_image_to_category_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfAnotherUser(EntityTypeWithImages.CATEGORY.getType(), null);
        }

        @Test
        public void return_404_if_entity_type_is_wrong() throws Exception {
            String wrongEntityType = "wrongEntityType";
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    wrongEntityType,
                    createdOfferId,
                    "classpath:test_image/testImg.jpeg");

            setImageAndGet404(imageProps);
        }

        @Test
        public void return_404_if_entity_type_is_wrong_and_if_entity_id_is_null() throws Exception {
            String wrongEntityType = "wrongEntityType";
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    wrongEntityType,
                    null,
                    "classpath:test_image/testImg.jpeg");

            setImageAndGet404(imageProps);
        }

        @Test
        public void return_404_if_entity_id_is_wrong_for_offer() throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    EntityTypeWithImages.OFFER.getType(),
                    -22L,
                    "classpath:test_image/testImg.jpeg");
            setImageAndGet404(imageProps);
        }

        @Test
        public void return_404_if_entity_id_is_wrong_for_user() throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    EntityTypeWithImages.USER.getType(),
                    -22L,
                    "classpath:test_image/testImg.jpeg");
            setImageAndGet404(imageProps);
        }

        @Test
        public void return_404_if_entity_id_is_wrong_category() throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    EntityTypeWithImages.CATEGORY.getType(),
                    -22L,
                    "classpath:test_image/testImg.jpeg");
            setImageAndGet404(imageProps);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_offer() throws Exception {
            setImageWithResponse400IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.OFFER.getType(), createdOfferId, 5);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_user() throws Exception {
            setImageWithResponse400IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.USER.getType(), createdUser1Id, 1);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_category() throws Exception {
            setImageWithResponse400IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId, 1);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_offer_and_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.OFFER.getType(), null, 5);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_user_and_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.USER.getType(), null, 1);
        }

        @Test
        public void return_400_if_count_image_is_greater_than_MAX_COUNT_for_category_and_if_entity_id_is_null() throws Exception {
            setImageWithResponse200IfCountOfImageIsGreaterThanMaxCount(EntityTypeWithImages.CATEGORY.getType(), null, 1);
        }

        @Test
        public void return_404_if_type_of_image_file_is_null_for_offer() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.OFFER.getType(),createdOfferId);
        }

        @Test
        public void return_404_if_type_of_image_file_is_null_for_user() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.USER.getType(),createdUser1Id);
        }

        @Test
        public void return_404_if_type_of_image_file_is_null_for_category() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.CATEGORY.getType(),createdCategoryId);
        }

        @Test
        public void return_404_if_type_of_image_file_is_null_l_for_offer_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.OFFER.getType(),null);
        }
        @Test
        public void return_404_if_type_of_image_file_is_null_l_for_category_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.CATEGORY.getType(),null);
        }
        @Test
        public void return_404_if_type_of_image_file_is_null_l_for_user_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeNull(EntityTypeWithImages.USER.getType(),null);
        }

        @Test
        public void return_404_if_type_of_image_file_is_wrong_for_user() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.USER.getType(),createdUser1Id);
        }

        @Test
        public void return_404_if_type_of_image_file_is_wrong_for_category() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.CATEGORY.getType(),createdCategoryId);
        }

        @Test
        public void return_404_if_type_of_image_file_is_wrong_for_offer() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.OFFER.getType(),createdOfferId);
        }

        @Test
        public void return_404_if_type_of_image_file_is_wrong_l_for_offer_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.OFFER.getType(),null);
        }
        @Test
        public void return_404_if_type_of_image_file_is_wrong_l_for_category_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.CATEGORY.getType(),null);
        }
        @Test
        public void return_404_if_type_of_image_file_is_wrong_l_for_user_and_entity_id_is_null() throws Exception {
            setImageAndGet400IfImageMediaTypeWrong(EntityTypeWithImages.USER.getType(),null);
        }
    }

    @Nested
    @DisplayName("DELETE /v1/images")
    @Rollback
    @Transactional
    public class DeleteImageTest {

        public String setImageAndReturnImageBaseName(String entityType, Long entityId) throws Exception {
            TestImagePropsDto imageProps = getTestImagePropsForSettingImage(
                    entityType,
                    entityId,
                    "classpath:test_image/testImg.jpeg");

            MvcResult createdImage = mockMvc.perform(multipart("/v1/images")
                            .file(imageProps.getTestFile())
                            .params(imageProps.getParams())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .cookie(getCookieByEntityType(entityType)))
                    .andExpect(status().isOk())
                    .andReturn();
            String jsonRes = createdImage.getResponse().getContentAsString();
            JsonNode json = mapper.readTree(jsonRes).get("values");
            Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
            String key = null;
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                key = field.getKey();
            }
            return key;
        }

        private void delImageByImageBaseName200(String imageBaseNAme, String entityType) throws Exception {
            ImageRequestDto imageRequestDto = new ImageRequestDto(imageBaseNAme);
            String jsonRequest = mapper.writeValueAsString(imageRequestDto);

            mockMvc.perform(delete("/v1/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                            .cookie(getCookieByEntityType(entityType)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").isString());
        }

        private void delImageByImageBaseName404(String imageBaseNAme) throws Exception {
            ImageRequestDto imageRequestDto = new ImageRequestDto(imageBaseNAme);
            String jsonRequest = mapper.writeValueAsString(imageRequestDto);

            mockMvc.perform(delete("/v1/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                            .cookie(getCookieByEntityType(EntityTypeWithImages.OFFER.getType())))
                    .andExpect(status().isNotFound());
        }

        private void delImageByImageBaseName400(String imageBaseNAme) throws Exception {
            ImageRequestDto imageRequestDto = new ImageRequestDto(imageBaseNAme);
            String jsonRequest = mapper.writeValueAsString(imageRequestDto);

            mockMvc.perform(delete("/v1/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                            .cookie(getCookieByEntityType(EntityTypeWithImages.OFFER.getType())))
                    .andExpect(status().isBadRequest());
        }

        private void deleteImageAndGet403(String entityType, Long entityId) throws Exception {
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, entityId);

            ImageRequestDto imageRequestDto = new ImageRequestDto(imageBaseNAme);
            String jsonRequest = mapper.writeValueAsString(imageRequestDto);

            mockMvc.perform(delete("/v1/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
                            .cookie(userCookie2))
                    .andExpect(status().isForbidden());
        }

        @Test
        public void return_403_delete_image_for_offer_user_do_not_have_rights() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.OFFER.getType(), createdOfferId);
        }

        @Test
        public void return_403_delete_image_for_user_user_do_not_have_rights() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.USER.getType(), createdUser1Id);
        }

        @Test
        public void return_403_delete_image_for_category_user_do_not_have_rights() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.CATEGORY.getType(), createdCategoryId);
        }

        @Test
        public void return_403_delete_image_for_offer_user_do_not_have_rights_if_entity_id_is_null() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.OFFER.getType(), null);
        }

        @Test
        public void return_403_delete_image_for_user_user_do_not_have_rights_if_entity_id_is_null() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.USER.getType(), null);
        }

        @Test
        public void return_403_delete_image_for_category_user_do_not_have_rights_if_entity_id_is_null() throws Exception {
            deleteImageAndGet403(EntityTypeWithImages.CATEGORY.getType(), null);
        }

        @Test
        public void return_200_delete_image_for_offer() throws Exception {
            String entityType =EntityTypeWithImages.OFFER.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, createdOfferId);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_200_delete_image_for_user() throws Exception {
            String entityType =EntityTypeWithImages.USER.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, createdUser1Id);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_200_delete_image_for_category() throws Exception {
            String entityType =EntityTypeWithImages.CATEGORY.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, createdCategoryId);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_200_delete_image_for_offer_if_entity_id_is_null() throws Exception {
            String entityType =EntityTypeWithImages.OFFER.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, null);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_200_delete_image_for_user_if_entity_id_is_null() throws Exception {
            String entityType =EntityTypeWithImages.USER.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, null);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_200_delete_image_for_category_if_entity_id_is_null() throws Exception {
            String entityType =EntityTypeWithImages.CATEGORY.getType();
            String imageBaseNAme = setImageAndReturnImageBaseName(entityType, null);
            delImageByImageBaseName200(imageBaseNAme, entityType);
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_offer() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_user() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_category() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_offer_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_user_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_empty_for_category_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400("");
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_offer() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_user() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_category() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_offer_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_user_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_400_if_image_base_name_is_null_for_category_if_entity_id_is_null() throws Exception {
            delImageByImageBaseName400(null);
        }

        @Test
        public void return_404_delete_image_with_nonexistent_base_name() throws Exception {
            String imageBaseNAme = UUID.randomUUID().toString();
            delImageByImageBaseName404(imageBaseNAme);
        }
        @Test
        public void return_404_delete_image_if_image_base_name_wrong() throws Exception {
            String imageBaseNAme = "wrong base name";
            delImageByImageBaseName404(imageBaseNAme);
        }
    }
}