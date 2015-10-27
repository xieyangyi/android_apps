package com.fsl.fslclubs.login;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by B47714 on 10/6/2015.
 */
public class HttpUtil {
    public final static String BASE_URL = "http://10.192.252.135:8080/FslClubsServer/";
    public final static String ACTIVITY_URL = "http://10.192.252.135:8080/FslClubsServer/myRes/activities/";
//    public final static String BASE_URL = "http://192.168.1.102:8080/FslClubsServer/";
//    public final static String ACTIVITY_URL = "http://192.168.1.102:8080/FslClubsServer/myRes/activities/";
    public final static String REQUEST_LOGIN = "1";
    public final static String REQUEST_IS_PHONE_EXSIT = "2";
    public final static String REQUEST_REGISTER = "3";
    public final static String REQUEST_JOIN_CLUB = "4";
    public final static String REQUEST_SAVE_USER = "5";

    public final static String LOGIN_PASSWORD_WRONG = "1";
    public final static String LOGIN_PHONE_NOT_EXIST = "2";
    public final static String CHECK_PHONE_EXIST_TRUE = "1";
    public final static String CHECK_PHONE_EXIST_FALSE = "2";
    public final static String REGISTER_SUCEESS = "1";
    public final static String REGISTER_FAILED = "2";
    public final static String JOIN_CLUB_SUCCESS = "1";
    public final static String SAVE_USER_SUCEESS = "1";
    public final static String SAVE_USER_FAILED = "2";
    public final static String SIGNUP_ACTIVITY_SUCCESS = "1";
    public final static String CHECK_CLUB_MEMBER_SUCCESS = "1";
    public final static String CHECK_CLUB_MEMBER_FAILED = "2";


    public static String queryStringForPost(final String url, final Map<String, String> rawParams) throws Exception {
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {

                HttpPost request = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<>();
                for (String key: rawParams.keySet()) {
                    params.add(new BasicNameValuePair(key, rawParams.get(key)));
                }

                request.setEntity(new UrlEncodedFormEntity(params, "gbk"));
                HttpResponse response = new DefaultHttpClient().execute(request);
                if(response.getStatusLine().getStatusCode() == 200) {
                    String result = EntityUtils.toString(response.getEntity());
                    return result;
                }
                return null;
            }
        });

        new Thread(task).start();
        return task.get();
    }

    public static String queryStringForGet(String url) {
        HttpGet request = new HttpGet(url);
        String result;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            if(response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity());
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return "network error";
        } catch (IOException e) {
            e.printStackTrace();
            return "IO error";
        }

        return null;
    }
}
