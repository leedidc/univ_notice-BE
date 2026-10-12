package univ_notice.backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private static final long EXPIRATION_MILLIS = 60 * 60 * 1000L;
  private final Key key;

  public JwtUtil(@Value("${app.jwt-secret}") String secret) {
    if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String userId) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + EXPIRATION_MILLIS))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUserIdFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
