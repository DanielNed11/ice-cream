package daniel.portfolio.icecream.security;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Role;
import daniel.portfolio.icecream.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private AppUserRepository appUserRepository;

    // --- authentication ---

    @Test
    public void productCatalogIsPublic() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    public void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void malformedTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void tokenSignedWithAnotherSecretIsUnauthorized() throws Exception {
        // A structurally valid JWT whose signature does not match our key.
        String foreignToken = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDAifQ"
                + ".Zm9yZ2VkLXNpZ25hdHVyZS10aGF0LWRvZXMtbm90LXZlcmlmeQ";

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + foreignToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void validTokenReachesTheEndpoint() throws Exception {
        AppUser customer = testHelper.customer("valid@test.local");

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + testHelper.token(customer)))
                .andExpect(status().isOk());
    }

    // --- authorisation ---

    @Test
    public void customerCannotReachAdminOrders() throws Exception {
        AppUser customer = testHelper.customer("cust@test.local");

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + testHelper.token(customer)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void adminCanReachAdminOrders() throws Exception {
        AppUser admin = testHelper.admin("admin@test.local");

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + testHelper.token(admin)))
                .andExpect(status().isOk());
    }

    @Test
    public void adminCannotManageProductsBecauseThatIsSuperAdminOnly() throws Exception {
        AppUser admin = testHelper.admin("admin2@test.local");

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + testHelper.token(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void superAdminCanManageProductsAndReadAdminOrders() throws Exception {
        AppUser superAdmin = testHelper.superAdmin("super@test.local");
        String token = testHelper.token(superAdmin);

        mockMvc.perform(get("/api/admin/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // --- registration ---

    @Test
    public void registrationIgnoresAClientSuppliedRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sneaky User",
                                  "email": "sneaky@test.local",
                                  "password": "Str0ngPass!23",
                                  "role": "SUPERADMIN"
                                }
                                """))
                .andExpect(status().isCreated());

        AppUser created = appUserRepository.findByEmail("sneaky@test.local").orElseThrow();
        assertEquals(Role.CUSTOMER, created.getRole());
    }

    @Test
    public void registrationStoresAHashedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hash User",
                                  "email": "hash@test.local",
                                  "password": "Str0ngPass!23"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        AppUser created = appUserRepository.findByEmail("hash@test.local").orElseThrow();
        assertNotEquals("Str0ngPass!23", created.getPassword());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
