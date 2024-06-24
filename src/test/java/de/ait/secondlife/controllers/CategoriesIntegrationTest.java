package de.ait.secondlife.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.secondlife.services.interfaces.CategoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Endpoint /categories works:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class CategoriesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /v1/categories")
    public class GetCategories{
        @Test
        public void return_list_of_categories() throws Exception{
            mockMvc.perform(get("/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(10)))
                    .andExpect(jsonPath("$.[0].id", is(1)))
                    .andExpect(jsonPath("$.[0].name", is("Electronics and Gadgets")))
                    .andExpect(jsonPath("$.[0].description", is("Smartphones,Laptops,Televisions,Peripherals")))
                    .andExpect(jsonPath("$.[0].active", is(true)))
                    .andExpect(jsonPath("$.[1].id", is(2)))
                    .andExpect(jsonPath("$.[1].name", is("Furniture and Home Decor")))
                    .andExpect(jsonPath("$.[1].description", is("Sofas,Tables and Chairs,Cabinets and Shelves,Decor and Accessories")))
                    .andExpect(jsonPath("$.[1].active", is(true)));
        }
    }


    @Nested
    @DisplayName("GET /v1/categories/{category-id}")
    public class GetCategory{

        @MockBean
        private CategoryService categoryService;

        @Test
        public void return_existed_category() throws Exception{
            mockMvc.perform(get("/v1/categories/4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(4)))
                    .andExpect(jsonPath("$.name", is("Vehicles")))
                    .andExpect(jsonPath("$.description", is("Cars,Motorcycles and Scooters,Bicycles,Auto Parts")))
                    .andExpect(jsonPath("$.active", is(true)));
        }
        @Test
        public void return_404_for_not_existed_category() throws Exception{
            mockMvc.perform(get("/v1/categories/12"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @Transactional
    @Rollback
    @DisplayName("POST /v1/categories")
    public class CreateCategory{

        private String adminToken;


        @BeforeEach
        public void setAdminToken() throws Exception{
            MvcResult authResult = mockMvc.perform(post("/v1/auth/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "email": "admin@second-life.space",
                                  "password": "Security!234"
                                }"""))
                    .andExpect(status().isOk())
                    .andReturn();

            String authResponse = authResult.getResponse().getContentAsString();

            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> authMap = mapper.readValue(authResponse, new TypeReference<Map<String, String>>() {});

            adminToken = authMap.get(("accessToken"));
        }

        @Test
        public void return_201_for_successfully_created_category() throws Exception{

            mockMvc.perform(post("/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                    .content("""
                             {
                              "name": "Test category",
                              "description": "Test description"
                             }"""))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Test category")))
                    .andExpect(jsonPath("$.description", is("Test description")))
                    .andExpect(jsonPath("$.active", is(false)));

        }
        @Test
        public void return_400_for_not_valid_category() throws  Exception{
            mockMvc.perform(post("/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("""
                             {
                              "name": ""
                             }"""))
                    .andExpect(status().isBadRequest());
        }
        @Test
        public void return_403_for_the_request_without_admin_token() throws  Exception{
            mockMvc.perform(post("/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                             {
                              "name": "Test category",
                              "description": "Test description"
                             }"""))
                    .andExpect(status().isForbidden());
        }
    }

}