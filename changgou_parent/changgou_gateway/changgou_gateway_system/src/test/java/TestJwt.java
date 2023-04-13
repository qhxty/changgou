import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class TestJwt {
    public static void main(String[] args) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId("888")
                .setSubject("buka")
                .setIssuedAt(new Date())
//                .setExpiration(new Date())
                .claim("roles","admin")
                .signWith(SignatureAlgorithm.HS256,"itbuka");
        System.out.println(jwtBuilder.compact());
    }
}
