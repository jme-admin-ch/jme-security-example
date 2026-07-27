package ch.admin.bit.jeap.jme.security.oauth.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Thing {
    private String id;
    private String partnerId;
    private String value;
}
