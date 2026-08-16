package com.greentech.security;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** 액세스 토큰 발급. 검증은 oauth2-resource-server 담당 */
@Service
public class JwtService {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_EMPLOYEE_ID = "empId";
    public static final String CLAIM_EMPLOYEE_NAME = "empName";

    private static final String ISSUER = "greentech-was";

    private final JwtEncoder encoder;
    private final SecurityProperties properties;

    public JwtService(JwtEncoder encoder, SecurityProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public IssuedToken issue(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.accessTokenTtl());

        List<String> roles = user.getRoles().stream().map(AppRole::getCode).toList();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLES, roles);

        if (user.getEmployee() != null) {
            claims.claim(CLAIM_EMPLOYEE_ID, user.getEmployee().getId());
            claims.claim(CLAIM_EMPLOYEE_NAME, user.getEmployee().getName());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();

        return new IssuedToken(token, expiresAt, properties.accessTokenTtl().toSeconds(), roles);
    }

    public record IssuedToken(String token, Instant expiresAt, long expiresInSeconds, List<String> roles) {
    }
}
