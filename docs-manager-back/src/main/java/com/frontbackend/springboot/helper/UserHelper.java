package com.frontbackend.springboot.helper;
import org.json.JSONObject;
public class UserHelper {
    public static String UserInfosAsJson(String roles, String orgname, String username) {
        JSONObject userInfos = new JSONObject();
        userInfos.put("username", username);
        userInfos.put("orgname", orgname);
        userInfos.put("roles", roles);
     return userInfos.toString();
    }
}
