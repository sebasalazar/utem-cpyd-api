package cl.utem.inf.cpyd.api.v1;

import cl.utem.inf.cpyd.domain.data.LoginRequest;
import cl.utem.inf.cpyd.domain.data.LoginResponse;
import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.exceptions.AuthException;
import cl.utem.inf.cpyd.manager.AuthManager;
import cl.utem.inf.cpyd.utils.JwtUtils;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/v1/login"})
public class LoginRest {

    private final AuthManager authManager;

    private static final String VALID_DOMAIN = "@utem.cl";

    public LoginRest(final AuthManager authManager) {
        this.authManager = authManager;
    }

    @PostMapping(value = {"/authenticate"}, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest request) {
        final String email = StringUtils.lowerCase(StringUtils.trimToEmpty(request.getEmail()));
        if (!Strings.CS.endsWith(email, VALID_DOMAIN)) {
            throw new AuthException();
        }

        Optional<Credential> opt = authManager.getCredential(email);
        if (opt.isEmpty()) {
            throw new AuthException();
        }

        final Credential credential = opt.get();
        final String apiKey = StringUtils.trimToEmpty(request.getApiKey());
        final boolean valid = authManager.isValid(credential, apiKey);
        if (!valid) {
            throw new AuthException();
        }

        final String jwt = JwtUtils.makeJwt(authManager.getSign(), credential.getEmail(), credential.getToken());
        return ResponseEntity.ok(new LoginResponse(jwt));
    }

}
