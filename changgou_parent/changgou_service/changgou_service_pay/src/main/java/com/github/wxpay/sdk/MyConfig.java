package com.github.wxpay.sdk;

import java.io.InputStream;

public class MyConfig extends WXPayConfig {
    public String getAppID() {
        return "wx796055a9a5d2822b";
    }

    public String getMchID() {
        return "1617197168";
    }

    public String getKey() {
        return "sahuan66sahuan66sahuan66sahuan66";
    }

    public InputStream getCertStream() {
        return null;
    }

    public IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            public void report(String s, long l, Exception e) {

            }

            public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                return new DomainInfo("api.mch.weixin.qq.com",true);
            }
        };
    }
}
