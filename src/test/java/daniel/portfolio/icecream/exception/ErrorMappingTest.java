package daniel.portfolio.icecream.exception;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ErrorMappingTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestHelper testHelper;

    private String customerToken;
    private String adminToken;

    @BeforeEach
    public void setUp() {
        AppUser customer = testHelper.customer("errors@test.local");
        AppUser admin = testHelper.admin("errors-admin@test.local");
        customerToken = "Bearer " + testHelper.token(customer);
        adminToken = "Bearer " + testHelper.token(admin);
    }

    @Test
    public void anUnmatchedUrlIsNotFoundRatherThanAServerError() throws Exception {
        mockMvc.perform(get("/api/order").header("Authorization", customerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void anUnknownEnumValueIsABadRequestAndListsTheValidOnes() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("status", "NOPE")
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("PLACED")));
    }

    @Test
    public void anUnparsableDateIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("from", "not-a-date")
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    // The two endpoints fail differently: a derived query is validated by Spring
    // Data, an @Query one only fails inside Hibernate. Both must answer 400.
    @Test
    public void anUnknownSortPropertyIsABadRequestOnADerivedQuery() throws Exception {
        mockMvc.perform(get("/api/orders").param("sort", "nope,desc")
                        .header("Authorization", customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void anUnknownSortPropertyIsABadRequestOnAnExplicitQuery() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("sort", "nope,desc")
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    // A frontend that renders "all statuses" as an empty query parameter should
    // get the unfiltered list, not an error.
    @Test
    public void anEmptyFilterIsTreatedAsNoFilter() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("status", "")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void theWrongHttpMethodIsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/products").header("Authorization", customerToken))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void aMalformedJsonBodyIsABadRequest() throws Exception {
        mockMvc.perform(put("/api/cart/items")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json "))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void aQuantityAboveTheCapIsRejected() throws Exception {
        testHelper.product("banana", "8.99", 500);

        mockMvc.perform(put("/api/cart/items")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slug": "banana", "quantity": 101}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("quantity")));
    }

    @Test
    public void anUnknownProductSlugIsNotFound() throws Exception {
        mockMvc.perform(put("/api/cart/items")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slug": "does-not-exist", "quantity": 1}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    public void anUnknownOrderReferenceIsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/ZZZZZZZZ").header("Authorization", customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void checkingOutAnEmptyCartIsUnprocessable() throws Exception {
        mockMvc.perform(post("/api/orders").header("Authorization", customerToken))
                .andExpect(status().isUnprocessableEntity());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
