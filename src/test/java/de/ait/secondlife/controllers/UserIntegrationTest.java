package de.ait.secondlife.controllers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController integration tests:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /v1/users/register")
    @Transactional
    @Rollback
    public class RegisterUser {

        @Test
        public void return_created_user() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"firstName\": \"TestFirstName\",\n" +
                                    "  \"lastName\": \"TestLastName\",\n" +
                                    "  \"email\": \"test.user@test.com\",\n" +
                                    "  \"password\": \"qwerty-123\"\n" +
                                    "}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName", is("TestFirstName")));
        }

        @Test
        public void return_400_for_invalid_email_format() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"firstName\": \"TestFirstName\",\n" +
                                    "  \"lastName\": \"TestLastName\",\n" +
                                    "  \"email\": \"test.user\",\n" +
                                    "  \"password\": \"qwerty-123\"\n" +
                                    "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.additionalMessage", is("Email is not valid")));
        }

        @Test
        public void return_400_for_missed_lastname() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"firstName\": \"TestFirstName\",\n" +
                                    "  \"email\": \"test.user@test.com\",\n" +
                                    "  \"password\": \"qwerty-123\"\n" +
                                    "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.additionalMessage", is("Last Name cannot be empty")));
        }

        @Test
        public void return_409_for_existed_email() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"firstName\": \"TestFirstName1\",\n" +
                                    "  \"lastName\": \"TestLastName1\",\n" +
                                    "  \"email\": \"test.user@test.com\",\n" +
                                    "  \"password\": \"qwerty-123\"\n" +
                                    "}"))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"firstName\": \"TestFirstName2\",\n" +
                                    "  \"lastName\": \"TestLastName2\",\n" +
                                    "  \"email\": \"test.user@test.com\",\n" +
                                    "  \"password\": \"qwerty-123\"\n" +
                                    "}"))
                    .andExpect(status().isConflict());
        }

    }

}