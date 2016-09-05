package kr.tamiflus.beaconlocation;

import android.util.Log;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by juwoong on 16. 1. 22..
 */
public class InformationSender {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String endpoint = "http://tamiflus.cloudapp.net";

    public static class ServerErrorException extends Exception {
        public ServerErrorException(String message) {
            super(message);
        }
    }

    public static void post(String uuid, String token) throws IOException, ServerErrorException{
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON,  "{\"uuid\" : \"" + uuid + "\", \"token\" : \"" +  token + "\"}");
        Log.i("InformationSender",  "{\"uuid\" : \"" + uuid + "\", \"token\" : \"" +  token + "\"}");
        Log.i("InformationSender", token);

        Request request = new Request.Builder()
                .url(endpoint + "/api/locate")
                .header("accesstoken", token)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        Log.i("InformationSender", Integer.toString(response.code()));
        if(response.code() != 200) throw new ServerErrorException(Integer.toString(response.code()));
    }

    public static String signup(String id, String pwd, String isMale, String age) throws IOException, ServerErrorException, ParseException, JSONException {
        OkHttpClient client = new OkHttpClient();
        JSONParser parser = new JSONParser();
        String result;
        String json = String.format("{\"id\" : \"%s\", \"passwd\" : \"%s\", \"gender\" : \"%s\", \"age\" : %s}",
                id, pwd, (isMale == "true" ? "male" : "female"), age);

        Log.i("InformationSender", json);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(endpoint + "/api/user/signup")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if(response.code() != 200) throw new ServerErrorException(Integer.toString(response.code()));
        result = response.body().string();
        Log.i("InforamtionSender", result);

        JSONObject obj = (JSONObject) parser.parse(result);
        Log.i("InformationSender", (String) obj.get("token"));

        return (String) obj.get("token");
    }
}
