package rikser123.security.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rikser123.security.repository.entity.User;

import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class Jwt {
    @Value("${jwt.expirationTime}")
    private long expirationMs;

    private byte[] secret;

    @Autowired
    private void secret(@Value("${jwt.secret}") String rawSecret) {
        var encoder = Base64.getEncoder();
        this.secret = encoder.encode(rawSecret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getLogin())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("status", user.getStatus())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUserName(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
