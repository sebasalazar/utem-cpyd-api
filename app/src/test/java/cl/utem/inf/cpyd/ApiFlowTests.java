package cl.utem.inf.cpyd;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.domain.repository.CredentialRepository;
import cl.utem.inf.cpyd.domain.repository.ExchangeRepository;
import cl.utem.inf.cpyd.utils.JwtUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiFlowTests {

    private static final String EMAIL = "user@utem.cl";
    private static final String API_KEY = "secret-key";
    private static final UUID TOKEN = UUID.randomUUID();
    private static final UUID INACTIVE_TOKEN = UUID.randomUUID();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private CredentialRepository credentials;
    @Autowired
    private ExchangeRepository exchanges;
    @Value("${cpyd.sign}")
    private String sign;

    @BeforeAll
    void seed() {
        credentials.save(credential(EMAIL, TOKEN, true));
        credentials.save(credential("off@utem.cl", INACTIVE_TOKEN, false));
        exchanges.save(new Exchange(Country.CHILE, LocalDate.of(2025, 1, 2), new BigDecimal("980.50")));
    }

    private static Credential credential(String email, UUID token, boolean active) {
        Credential c = new Credential();
        c.setEmail(email);
        c.setToken(token);
        c.setApiKey(API_KEY);
        c.setActive(active);
        return c;
    }

    private String bearer(String email, UUID token) {
        return "Bearer " + JwtUtils.makeJwt(sign, email, token);
    }

    @Test
    void loginAndQuery() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/v1/login/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"apiKey\":\"" + API_KEY + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").exists());

        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile/2025-01-02")
                .header("Authorization", bearer(EMAIL, TOKEN)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.rate").value(980.50));
    }

    @Test
    void wrongApiKeyIs401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/v1/login/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"apiKey\":\"nope\"}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void inactiveCredentialIs403() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile/2025-01-02")
                .header("Authorization", bearer("off@utem.cl", INACTIVE_TOKEN)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void missingHeaderIs400() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile/2025-01-02"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void badDateIs400() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile/not-a-date")
                .header("Authorization", bearer(EMAIL, TOKEN)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void invertedRangeIs400() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile")
                .param("start_date", "2025-02-01").param("end_date", "2025-01-01")
                .header("Authorization", bearer(EMAIL, TOKEN)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void rangeQueryReturnsData() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/exchanges/chile")
                .param("start_date", "2025-01-01").param("end_date", "2025-01-31")
                .header("Authorization", bearer(EMAIL, TOKEN)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }

    @Test
    void repeatedFailuresAreThrottled() throws Exception {
        final String body = "{\"email\":\"off@utem.cl\",\"apiKey\":\"bad\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(MockMvcRequestBuilders.post("/v1/login/authenticate")
                    .contentType(MediaType.APPLICATION_JSON).content(body));
        }
        mvc.perform(MockMvcRequestBuilders.post("/v1/login/authenticate")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests());
    }
}
