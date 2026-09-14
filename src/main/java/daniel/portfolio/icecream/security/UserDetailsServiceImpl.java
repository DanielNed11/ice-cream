package daniel.portfolio.icecream.security;

import daniel.portfolio.icecream.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .map(CustomUser::buildUser)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public Optional<CustomUser> loadUserById(UUID id) {
        return appUserRepository.findById(id).map(CustomUser::buildUser);
    }
}
