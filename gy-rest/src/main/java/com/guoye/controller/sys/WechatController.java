package com.guoye.controller.sys;

import com.alibaba.fastjson.JSON;
import com.guoye.base.BizAction;
import com.guoye.bean.Wxpoid;
import com.guoye.util.BaseResult;
import com.guoye.util.DESWrapper;
import com.guoye.util.HttpClientUtil;
import com.guoye.util.StatusConstant;
import com.ning.http.util.Base64;
import net.sf.json.JSONObject;
import com.sun.deploy.net.HttpUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.g4studio.core.metatype.Dto;
import org.g4studio.core.metatype.impl.BaseDto;
import org.g4studio.core.resource.util.StringUtils;
import org.g4studio.core.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信用户业务处理
 *
 * @author zxl
 * @see SysUserController
 * @since 2017年6月20日15:49:06
 */
@RestController
@RequestMapping("/wechat")
public class WechatController extends BizAction {


    //获取openid and session_key
    @RequestMapping(value = "/getOpenid")
    public BaseResult wechatLogin(HttpServletRequest request, HttpServletResponse response) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {

            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);
            redisService.delete("512");
            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }
            Map<String, String> param = new HashMap<>();
            param.put("appid", "wx8638e80c7186b393");
            param.put("secret", "f2b40a6cb42b71deb901e8ef180697a4");
            param.put("js_code", dto.getAsString("code"));
            param.put("grant_type", "authorization_code");
            Map<String, String> head = new HashMap<>();
            head.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            String s = HttpClientUtil.doRequestGet("https://api.weixin.qq.com/sns/jscode2session", param, head);
            if (StringUtils.isNotEmpty(s)) {
                JSONObject jsonObject = JSONObject.fromObject(s);
                  //获取openid
                String openid = jsonObject.getString("openid");
                redisService.setValue("openid",openid);
                //获取session_key
                String session_key=jsonObject.getString("session_key");
                redisService.setValue("session_key",session_key);
                Dto udto=new BaseDto();
                udto.put("openid",openid);
                udto.put("session_key",session_key);
                result.setData(udto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    //返回个人信息
    @RequestMapping(value = "/userInformation")
    public BaseResult userInformation(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {

            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);
            DESWrapper Des = new DESWrapper();
            String password = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";


            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }
            //小程序端唯一标识
            String unionid = dto.getAsString("unionid");
            dto.put("tableName", "sysUser");


            if (StringUtils.isNotEmpty(unionid)) {
                Dto udto=(BaseDto)bizService.queryForDto("sysUser.getInfo",new BaseDto("unionid",dto.getAsString("unionid")));
                if(udto !=null){
                    Dto userdto=(BaseDto)bizService.queryForDto("sysUser.getUserInfo",new BaseDto("id",udto.getAsString("id")));
                    userdto.put("mobile",Des.decrypt(userdto.getAsString("mobile"),password));
                    result.setData(userdto);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    //个人信息修改
    @RequestMapping(value = "/editWeUser")
    public BaseResult editUser(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {
            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);
            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }
            String id = dto.getAsString("id");
            dto.put("tableName", "sysUser");
            Dto udto=(BaseDto)bizService.queryForDto("sysUser.getInfo",new BaseDto("id",dto.getAsString("id")));
            // 修改
            if (StringUtils.isNotEmpty(id) && udto !=null) {
                if(udto.getAsInteger("editSum")==1){
                    throw new Exception("性别只可修改一次");
                }
                if (udto.getAsString("sex")!=null && udto.getAsString("sex")!=""){
                    dto.put("editSum",1);
                }
                dto.put("updator", member == null ? "" : member.get("id"));
                bizService.updateInfo(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }





    /**
     * 解密获取微信用户信息
     *
     * @param encryptedData
     * @param sessionkey
     * @param iv
     * @return
     */
    public JSONObject decryptUserInfo(String encryptedData, String sessionkey, String iv) {
        encryptedData = encryptedData.replaceAll(" ", "+");
        // 被加密的数据
        byte[] dataByte = org.bouncycastle.util.encoders.Base64.decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = org.bouncycastle.util.encoders.Base64.decode(sessionkey);
        // 偏移量
        byte[] ivByte = Base64.decode(iv);
        try {
            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, StandardCharsets.UTF_8);
                return JSON.parseObject(result);
            }
        } catch (Exception e) {
            logger.error("获取unionId异常", e);
            throw new RuntimeException("获取unionId异常", e);
        }
        throw new RuntimeException("获取联合登陆用户信息失败");

    }


}
