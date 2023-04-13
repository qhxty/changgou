package com.changgou.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTest {

    @Test
    public void testCreateToken() {
        //证书文件路径
        String key_location = "changgou.jks";
        //密钥库密码
        String key_password = "changgou";
        //密钥密码
        String keypwd = "changgou";
        //密钥别名
        String alias = "changgou";

        //访问私钥
        ClassPathResource resource = new ClassPathResource(key_location);
        //创建密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());
        //获取私钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias,keypwd.toCharArray());
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String,Object> tokenMap = new HashMap<>();
        tokenMap.put("id","1");
        tokenMap.put("name","buka");
        tokenMap.put("roles","ADMIN");

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap),new RsaSigner(rsaPrivateKey));
        String encode = jwt.getEncoded();
        System.out.println(encode);
    }
   
}
