package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.logging.Sensitive;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Role;
import daniel.portfolio.icecream.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static daniel.portfolio.icecream.constants.Constants.EMAIL_ALREADY_REGISTERED;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UUID register(String name, String email, @Sensitive String rawPassword) {

        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException(EMAIL_ALREADY_REGISTERED);
        }

        AppUser appUser = new AppUser();
        appUser.setName(name);
        appUser.setEmail(email);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setRole(Role.CUSTOMER);

        try {
            appUserRepository.save(appUser);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyRegisteredException(EMAIL_ALREADY_REGISTERED);
        }

        return appUser.getId();
    }
}
