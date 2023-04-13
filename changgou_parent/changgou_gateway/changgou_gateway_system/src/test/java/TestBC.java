import org.springframework.security.crypto.bcrypt.BCrypt;

public class TestBC {
    public static void main(String[] args) {
        for(int i = 1; i < 10; i++) {
            String gensalt = BCrypt.gensalt();
            System.out.println("盐:" + gensalt);
            String password = BCrypt.hashpw("123456",gensalt);
            System.out.println(password);
            boolean checkpw = BCrypt.checkpw("123456",password);
            System.out.println(checkpw);
        }
    }
}
