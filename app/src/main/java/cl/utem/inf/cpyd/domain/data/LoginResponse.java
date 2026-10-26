package cl.utem.inf.cpyd.domain.data;

import java.util.Objects;

public class LoginResponse {

    private String jwt;

    public LoginResponse() {
        this.jwt = "";
    }

    public LoginResponse(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.jwt);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoginResponse other = (LoginResponse) obj;
        return Objects.equals(this.jwt, other.jwt);
    }
}
