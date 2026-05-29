package rikser123.security.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rikser123.security.repository.entity.User;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class Jwt {
  @Value("${jwt.expirationTime}")
  private long expirationMs;

  @Value("${jwt.refreshExpirationTime}")
  private long refreshExpirationTime;

  @Value("${jwt.privateKey}")
  private String privateKeyBase64;

  @Value("${jwt.publicKey}")
  private String publicKeyBase64;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @PostConstruct
  public void init() throws Exception {
    var privatePem = new String(Base64.getDecoder().decode(privateKeyBase64));
    var publicPem = new String(Base64.getDecoder().decode(publicKeyBase64));

    privateKey = loadPrivateKey(privatePem);
    publicKey = loadPublicKey(publicPem);
  }

  private PrivateKey loadPrivateKey(String pem) throws Exception {
    var privateKeyContent = pem
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replaceAll("\\s", "");

    var decoded = Base64.getDecoder().decode(privateKeyContent);
    var keySpec = new PKCS8EncodedKeySpec(decoded);
    var keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(keySpec);
  }

  private PublicKey loadPublicKey(String pem) throws Exception {
    var publicKeyContent = pem
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replaceAll("\\s", "");

    var decoded = Base64.getDecoder().decode(publicKeyContent);
    var keySpec = new X509EncodedKeySpec(decoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(keySpec);
  }

  public String generateRefreshToken(User user) {
    return Jwts.builder()
      .subject(user.getLogin())
      .claim("id", user.getId())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
      .signWith(privateKey, SignatureAlgorithm.RS256)
      .compact();
  }

  public String generateToken(User user) {
    return Jwts.builder()
      .subject(user.getLogin())
      .claim("id", user.getId())
      .claim("email", user.getEmail())
      .claim("status", user.getStatus())
      .claim("firstName", user.getFirstName())
      .claim("middleName", user.getMiddleName())
      .claim("lastName", user.getLastName())
      .claim("birthDate", user.getBirthDate().toString())
      .claim("privileges", user.getPrivileges())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expirationMs))
      .signWith(privateKey, SignatureAlgorithm.RS256)
      .compact();
  }

  public String extractUserName(String token) {
    return Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(token).getBody().getSubject();
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
  }

  public String getPublicKey() {
    return new String(Base64.getDecoder().decode(publicKeyBase64));
  }
}
