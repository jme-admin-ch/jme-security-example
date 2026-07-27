package ch.admin.bit.jeap.jme.skeleton.scs.ui.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration")
@RequiredArgsConstructor
@Slf4j
class ConfigurationController {
    private final FrontendConfigProperties frontendConfigProperties;

    @GetMapping
    public ConfigurationDTO getConfiguration() {
        // Quadrel Auth uses this legacy flag to bypass the ePortal session check when no service navigation is present.
        return ConfigurationDTO.builder()
                .applicationUrl(frontendConfigProperties.getApplicationUrl().toString())
                .authority(frontendConfigProperties.getAuthority().toString())
                .logoutRedirectUri(frontendConfigProperties.getLogoutRedirectUri().toString())
                .mockPams(true)
                .tokenAwarePatterns(frontendConfigProperties.getTokenAwarePatterns())
                .clientId(frontendConfigProperties.getClientId())
                .useAutoLogin(frontendConfigProperties.isUseAutoLogin())
                .redirectUrl(frontendConfigProperties.getRedirectUrl())
                .build();
    }
}
