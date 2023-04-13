import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class TestJwt2 {
    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiJidWthIiwiaWF0IjoxNjc1NjcxOTI4LCJyb2xlcyI6ImFkbWluIn0.yW2BRZh4vzy302-YUKP9ZC0NAgXbLXABroEerlZvmrc";
        Claims claims = Jwts.parser().setSigningKey("itbuka").parseClaimsJws(token).getBody();
        System.out.println(claims);
    }
}
