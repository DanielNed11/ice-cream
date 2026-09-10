package daniel.portfolio.icecream.security;

import daniel.portfolio.icecream.model.AppUser;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUser extends User {

    @Getter
    private final UUID id;

    private CustomUser(
            String email,
            @Nullable String password,
            Collection<? extends GrantedAuthority> authorities,
            UUID id
    ) {
        super(email, password, authorities);
        this.id = id;
    }

    static CustomUser buildUser(AppUser appUser) {
        return new CustomUser(
                appUser.getEmail(),
                appUser.getPassword(),
                List.of(),
                appUser.getId()
        );
    }
}
