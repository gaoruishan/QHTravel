package com.cmcc.hyapps.andyou.model;

import android.content.Context;

import com.cmcc.hyapps.andyou.utils.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by Administrator on 2015/5/29.
 */
public class Token {
    private static final String KEY_TOKEN_INFO = "key_token_info";

    public String token;

    public static Token getTokenInfo(Context context) {
        String json = PreferencesUtils.getString(context, KEY_TOKEN_INFO);
        Token info = null;
        try {
            info = new Gson().fromJson(json, Token.class);
        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return info;
    }
}
