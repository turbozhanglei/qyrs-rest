package com.guoye.util;

import com.guoye.config.WxConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2019/3/6.
 */
@Component
public class UserUtil {
    @Autowired
    WxConfig wxConfig;
    //  获取微信id的方法
    public String getopenid(String code) {
            BufferedReader in = null;
            String appid = wxConfig.getAppid();
            String secret = wxConfig.getSecret();
            //appid和secret是开发者分别是小程序ID和小程序密钥，开发者通过微信公众平台-》设置-》开发设置就可以直接获取，
            String url="https://api.weixin.qq.com/sns/jscode2session?appid="
                    +appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
            try {
                URL weChatUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection connection = weChatUrl.openConnection();
                // 设置通用的请求属性
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                // 建立实际的连接
                connection.connect();
                // 定义 BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
            return sb.toString();
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


}
