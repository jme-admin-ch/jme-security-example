package ch.admin.bit.jeap.jme.skeleton.scs.user;

import ch.admin.bit.jeap.security.user.JeapCurrentUser;
import ch.admin.bit.jeap.security.user.JeapCurrentUserCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JmeCurrentUserCustomizer implements JeapCurrentUserCustomizer<JmeCurrentUserDto> {

    @Override
    public JmeCurrentUserDto customize(JeapCurrentUser jeapCurrentUser) {
        return new JmeCurrentUserDto(jeapCurrentUser, "fooBar");
    }
}
