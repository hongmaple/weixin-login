package com.maple.weixinlogin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maple.weixinlogin.pojo.MiniProgram;
import com.maple.weixinlogin.utils.HttpUtil;
import com.maple.weixinlogin.utils.WXCore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author maple
 */
@RestController
@RequestMapping("/miniprogram")
public class MiniProgramLogin {

    /**
     *  小程序 AppID
     */
	private static final String APPID = "***********";
    /**
     * 小程序 AppSecret
      */
	private static final String SECRET = "************";

	/**
	 * 小程序授权登录
	 */
  	@PostMapping("/miniLogin")
  	public  Map<String,Object> miniLogin(@RequestBody MiniProgram miniProgram){
        Map<String,Object> map=new HashMap<>(10);
        
        String params = "appid=" + APPID + "&secret=" + SECRET + "&js_code=" + miniProgram.getCode() + "&grant_type=authorization_code";
        String s= HttpUtil.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
        JSONObject jsonObject = JSON.parseObject(s);


        String session_key=jsonObject.getString("session_key");
        String openid=jsonObject.getString("openid");
        String unionid=jsonObject.getString("unionid");
        
        /*String result= WXCore.decrypt(APPID,miniProgram.getEncryptedData(),session_key,miniProgram.getIv());
        JSONObject json=JSON.parseObject(result);
        
        if (!StringUtils.isEmpty(result)&&result.length()>0){
            map.put("unionid", unionid);
            map.put("session_key",session_key);
            map.put("openid",openid);
            map.put("avatarUrl",json.getString("avatarUrl"));
            map.put("nickName",json.getString("nickName"));
            map.put("msg","success");
            return map;
        }else {
            map.put("msg","error");
        }*/

        return map;
    }

    /**
     * 小程序授权获取手机号
     */
    @PostMapping(value="/getPhoneNumber")
    public Map<String,Object> getPhoneNumber(@RequestBody MiniProgram miniProgram) {
        Map<String,Object> map=new HashMap<>(10);

        String result=WXCore.decrypt(APPID,miniProgram.getEncryptedData(),miniProgram.getSession_key(),miniProgram.getIv());
        JSONObject json=JSONObject.parseObject(result);

        if (!StringUtils.isEmpty(result)&&result.length()>0) {
            map.put("purePhoneNumber", json.getString("purePhoneNumber"));
            map.put("phoneNumber", json.getString("phoneNumber"));
            map.put("countryCode", json.getString("countryCode"));
            map.put("msg","success");
        }
        map.put("msg","error");
        return map;
    }
    
}