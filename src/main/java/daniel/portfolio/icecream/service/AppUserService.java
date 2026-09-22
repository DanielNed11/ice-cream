package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.controller.response.UserResponse;
import daniel.portfolio.icecream.exception.UserNotFoundException;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static daniel.portfolio.icecream.constants.Constants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public UserResponse findProfile(UUID userId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        return new UserResponse(appUser.getName(), appUser.getEmail(), appUser.getRole());
    }
}
