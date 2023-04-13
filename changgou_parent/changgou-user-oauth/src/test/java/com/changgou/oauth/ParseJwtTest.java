package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {
    @Test
    public void testParse() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IkFETUlOIiwibmFtZSI6ImJ1a2EiLCJpZCI6IjEifQ.kmdIEDF2Uq2ancFHjk-LRVteLZyTyD0vkdolvAYEX0AYXy9F4YQG8IhJ04_JSoHoGkuQ2PRkuRyN80EaMvfWDx1Rctu-ENfh0p9tMvdnmYo_CkE_rAYoFboig7Sc899ym0AUIP4evkZGV-MFiZ2ewM-GDmpNcyR5ZfC9SNpbDvZ95-9KQ4WWsn9xQcPB4cBj9oGr9SPxwz87jqFU-arPtIm3r1zKx5aF_3Toqiyulb-LTib_RsfLa8KGZRVhzgWl4zXOoznML3NNjeynmOH2tbmRfgJNF_fcyUnT7jWYtW2nF7ksEaB9aXL6riyQCaME9J6AvT-uvuPuoXtzMpzDLQ";

        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token,new RsaVerifier(publicKey));

        String claims = jwt.getClaims();
        System.out.println(claims);
        String encode = jwt.getEncoded();
        System.out.println(encode);
    }
}
