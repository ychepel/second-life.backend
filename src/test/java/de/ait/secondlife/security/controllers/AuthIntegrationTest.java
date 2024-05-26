package de.ait.secondlife.security.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController integration tests:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
@Transactional
@Rollback
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void createUser() throws Exception {
        mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "TestUserFirstName",
                                  "lastName": "TestUserLastName",
                                  "email": "test.user@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isCreated());
    }

    @Test
    public void return_user_tokens_after_user_login() throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test.user@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String content = response.getContentAsString();
        assertNotNull(JsonPath.parse(content).read("$.accessToken"));
        assertNotNull(JsonPath.parse(content).read("$.refreshToken"));
        assertNotNull(response.getCookie("Access-Token").getValue());
    }

    @Test
    public void return_admin_tokens_after_user_login() throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@email.com",
                                  "password": "Security!234"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String content = response.getContentAsString();
        assertNotNull(JsonPath.parse(content).read("$.accessToken"));
        assertNotNull(JsonPath.parse(content).read("$.refreshToken"));
        assertNotNull(response.getCookie("Access-Token").getValue());
    }

    @Test
    public void return_400_for_missing_user_email() throws Exception {
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void return_400_for_non_existent_user_email() throws Exception {
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "usertest@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void return_401_for_incorrect_user_password() throws Exception {
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test.user@test.com",
                                  "password": "123"
                                }"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void return_new_access_token_for_user() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test.user@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse loginResponse = loginResult.getResponse();
        String loginContent = loginResponse.getContentAsString();
        String refreshToken = JsonPath.parse(loginContent).read("$.refreshToken");

        MvcResult accessResult = mockMvc.perform(post("/v1/auth/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"refreshToken\": \"" + refreshToken + "\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse accessResponse = accessResult.getResponse();
        String accessContent = accessResponse.getContentAsString();
        assertNotNull(JsonPath.parse(accessContent).read("$.accessToken"));
        assertNotNull(JsonPath.parse(accessContent).read("$.refreshToken"));
        assertNotNull(accessResponse.getCookie("Access-Token").getValue());
    }

    @Test
    public void return_400_for_refreshing_with_invalid_user_refresh_token() throws Exception {
        mockMvc.perform(post("/v1/auth/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "invalidUserRefreshToken"
                                }"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void return_400_for_refreshing_with_invalid_admin_refresh_token() throws Exception {
        mockMvc.perform(post("/v1/auth/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "invalidAdminRefreshToken"
                                }"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void remove_access_token_from_cookie_after_user_logout() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test.user@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = loginResult.getResponse().getCookie("Access-Token").getValue();
        Cookie cookie = new Cookie("Access-Token", accessToken);

        MvcResult logoutResult = mockMvc.perform(get("/v1/auth/user/logout").cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();
        assertNull(logoutResult.getResponse().getCookie("Access-Token").getValue());
    }

    @Test
    public void return_400_for_refreshing_tokens_after_user_logout() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test.user@test.com",
                                  "password": "qwerty!123"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse loginResponse = loginResult.getResponse();
        String loginContent = loginResponse.getContentAsString();
        String refreshToken = JsonPath.parse(loginContent).read("$.refreshToken");
        String accessToken = loginResponse.getCookie("Access-Token").getValue();
        Cookie cookie = new Cookie("Access-Token", accessToken);

        mockMvc.perform(get("/v1/auth/user/logout").cookie(cookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/auth/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"refreshToken\": \"" + refreshToken + "\"\n" +
                                "}"))
                .andExpect(status().isBadRequest());
    }
}