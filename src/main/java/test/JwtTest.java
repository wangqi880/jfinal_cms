package test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    public  static String SECRET="hehe";


    public static void main(String args[]) throws Exception {
        String token = createToken();

        Map<String,Claim> map = verifyToken(token);
        System.out.println(token);
        System.out.println(map.get("name").asString());
    }
    public static String createToken() throws Exception {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,1);
        Date expireDate=calendar.getTime();
        Map<String,Object> map = new HashMap<>();
        map.put("type","JWT");
        map.put("alg","HS256");
        String token = JWT.create().withHeader(map)
                .withClaim("name","wq")
                .withClaim("age","12")
                .withExpiresAt(expireDate)
                .withIssuedAt(date)
                .sign(Algorithm.HMAC256(SECRET));

        return token;
    }


    //解码token
    public static Map<String ,Claim> verifyToken(String token) throws UnsupportedEncodingException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        DecodedJWT decodedJWT =null;
        try {
            decodedJWT = verifier.verify(token);
        } catch (JWTVerificationException e) {
            e.printStackTrace();
        }
        return decodedJWT.getClaims();
    }
}
