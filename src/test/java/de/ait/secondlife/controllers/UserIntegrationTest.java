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
                            .content("""
                                    {
                                      "firstName": "TestFirstName",
                                      "lastName": "TestLastName",
                                      "email": "test.user@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName", is("TestFirstName")));
        }

        @Test
        public void return_400_for_invalid_email_format() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "TestFirstName",
                                      "lastName": "TestLastName",
                                      "email": "test.user",
                                      "password": "qwerty!123"
                                    }"""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].message", is("Field Email is not valid")));
        }

        @Test
        public void return_400_for_missed_lastname() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "TestFirstName",
                                      "email": "test.user@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].message", is("Field Last Name cannot be empty")));
        }

        @Test
        public void return_400_with_3_validation_errors() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": null,
                                      "lastName": "CorrectTestLastName",
                                      "email": "test@user@@@test.com",
                                      "password": "qwerty"
                                    }"""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", org.hamcrest.Matchers.hasSize(3)));
        }

        @Test
        public void return_409_for_existed_email() throws Exception {
            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "TestFirstName1",
                                      "lastName": "TestLastName1",
                                      "email": "test.user@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "TestFirstName2",
                                      "lastName": "TestLastName2",
                                      "email": "test.user@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                    .andExpect(status().isUnprocessableEntity());
        }

    }
}
