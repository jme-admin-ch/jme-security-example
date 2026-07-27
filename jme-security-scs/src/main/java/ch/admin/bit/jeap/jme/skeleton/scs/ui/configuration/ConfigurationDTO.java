package ch.admin.bit.jeap.jme.skeleton.scs.ui.configuration;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
class ConfigurationDTO {
    String authority;
    String applicationUrl;
    String logoutRedirectUri;
    boolean mockPams;
    List<String> tokenAwarePatterns;
    String redirectUrl;
    String clientId;
    boolean useAutoLogin;

}
