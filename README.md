### 前端调用方法
#### 小程序端要发起两次请求
##### 1.获取手机号
         发送请求到（域名/服务名/miniLogin）
         参数 
                "encryptedData": e.detail.encryptedData,
                "iv": e.detail.iv,
                "code": res.code
         返回值，手机号
##### 2.调用登陆接口（域名/服务名/login/{loginType}）
loginType是登陆类型，不填则为密码登陆
        loginType为weiXinLogin 时是微信登陆
        请求参数
                    username: rs.data,  //手机号
                    password: res.code //微信的code
返回值：token
#### 视图
```html
<view class="userinfo">
    <button 
      open-type="getUserInfo" 
      bindgetuserinfo="onGetUserInfo"
      class="userinfo-avatar"
      style="background-image: url({{avatarUrl}})"
      size="default"
    ></button>
    <view class="userinfo-nickname-wrapper">
      <button open-type="getPhoneNumber" bindgetphonenumber="wxXinLogin" >微信一键登陆</button>
    </view>
  </view>
  ```
  #### js方法
  ```javascript
   wxXinLogin: function(e){
    console.log(e.detail.errMsg);
     //是否授权，授权通过进入主页面，授权拒绝则停留在登陆界面
     if (e.detail.errMsg == 'getPhoneNumber:fail user deny') { //用户点击拒绝
      //wx.navigateTo({
       // url: '../index/index',
     // })
    } else { 
      //允许授权执行跳转
      wx.login({
        success (res) {
          if (res.code) {
            //获取手机号
            wx.request({
              url: "http://localhost:8081/business-user/miniLogin",
              method: "POST",
              header: {
                'content-type': 'application/json',
              },
              data: {
                "encryptedData": e.detail.encryptedData,
                "iv": e.detail.iv,
                "code": res.code
              },
              success: function (rs) {
                if(rs.statusCode!=200){
                  console.log(rs)
                  return;
                }
                console.log(rs)
                if(rs.data==null){
                  console.log("微信登陆失败")
                  return
                }
                //调用登陆接口
                wx.request({
                  url: "http://localhost:8081/business-user/login/weiXinLogin",
                  method: "POST",
                  header: {
                    'content-type': 'application/x-www-form-urlencoded',
                  },
                  data: {
                    username: rs.data,
                    password: res.code //passsword 是微信的code
                  },
                  fail: function (error) {
                    console.log(error);
                  }
                })
              },
              fail: function (error) {
                console.log(error);
              }
            })
          } else {
            console.log('登录失败！' + res.errMsg)
          }
        },
        fail: function (error) {
          console.log(error);
        }
      })
    }
  }
  ```
  ### 后端
##### 修改后端网关配置文件
![](https://cdn.nlark.com/yuque/0/2020/png/1698739/1601184602218-952f500c-06e8-4408-99e7-8ac38744e3e0.png)
##### 修改登陆路径拦截
![](https://cdn.nlark.com/yuque/0/2020/png/1698739/1601184690510-6fcd4e1c-a87e-4307-9abd-74a6757ed8ff.png)
##### 让登陆类型参数能正常映射
![](https://cdn.nlark.com/yuque/0/2020/png/1698739/1601184762344-61336bf4-92f5-4577-95a3-7c6cbf64d019.png)
##### 解析code获取手机号
```java
 @Override
    public String miniLogin(MiniProgramBody miniProgram) {
        String params = "appid=" + WeChatAppletConfig.APPID + "&secret=" + WeChatAppletConfig.SECRET + "&js_code=" + miniProgram.getCode() + "&grant_type=authorization_code";
        String s= HttpUtil.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
        JSONObject jsonObject = JSON.parseObject(s);


        String session_key=jsonObject.getString("session_key");

        String result= WXCore.decrypt(WeChatAppletConfig.APPID,miniProgram.getEncryptedData(),session_key,miniProgram.getIv());
        JSONObject json=JSON.parseObject(result);
        if (StringUtils.isEmpty(result)&&result.length()<=0){
            throw ServiceRuntimeException.build(ErrorCode.WECHAT_LOGIN_FAILED);
        }
        String purePhoneNumber = json.getString("purePhoneNumber");
        if (!StringUtils.isEmpty(purePhoneNumber)&&purePhoneNumber.length()>0){
            return purePhoneNumber;
        }else {
            throw ServiceRuntimeException.build(ErrorCode.WECHAT_LOGIN_FAILED);
        }
    }
    ```
    #### 微信登陆验证逻辑
    ```java
    if(loginType!=null&&loginType.equals("weiXinLogin")){
            //微信登陆
            String params = "appid=" + WeChatAppletConfig.APPID + "&secret=" + WeChatAppletConfig.SECRET + "&js_code=" + loginBody.getPassword() + "&grant_type=authorization_code";
            String s= HttpUtil.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
            if (StringUtils.isEmpty(s)&&s.length()==0){
                throw ServiceRuntimeException.build(ErrorCode.WECHAT_LOGIN_FAILED);
            }
}else {
            if(!passwordEncoder.matches(loginBody.getPassword(), account.getPassword())) {
                throw ServiceRuntimeException.build(ErrorCode.WRONG_PASSWORD);
            }
}
```
