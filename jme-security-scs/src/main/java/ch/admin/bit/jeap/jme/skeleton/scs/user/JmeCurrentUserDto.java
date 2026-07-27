package ch.admin.bit.jeap.jme.skeleton.scs.user;

import ch.admin.bit.jeap.security.user.JeapCurrentUser;
import ch.admin.bit.jeap.security.user.JeapCurrentUserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JmeCurrentUserDto extends JeapCurrentUserDto {

    String myCustomValue;

    public JmeCurrentUserDto(JeapCurrentUser jeapCurrentUser, String myCustomValue) {
        super(jeapCurrentUser);
        this.myCustomValue = myCustomValue;
    }
}
