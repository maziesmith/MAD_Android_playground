package comfycorp.hueapp;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.*;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by Kraegon on 27/10/2015.
 */
public class BridgeMiddleMan {
    private static BridgeMiddleMan ourInstance = new BridgeMiddleMan();

    public static BridgeMiddleMan getInstance() {
        return ourInstance;
    }

    public Context mContext;
    public String username = "1f4d16f21308d5973663850e592c5c3";
    public String bridgeIp = "192.168.1.179";
    public ArrayList<HueLight> lightArray = new ArrayList<HueLight>();
    private String bridgeFinderUrl = "https://www.meethue.com/api/nupnp";
    private BridgeMiddleMan() {


    }

    public void findAndSetBridgeIp() {
        System.out.println("findAndSetBridgeIp()");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(bridgeFinderUrl, new JsonHttpResponseHandler() {

//            @Override
//            public void onStart() {
//                // called before request is started
//            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // called when response HTTP status is "200 OK"
                try {
                    System.out.println("Bridge IP found: " + response.getJSONObject(0).getString("internalipaddress"));
                    bridgeIp = response.getJSONObject(0).getString("internalipaddress");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//            }

//            @Override
//            public void onRetry(int retryNo) {
//                // called when request is retried
//            }
        });
    }

    public void createUsernameHueBridge() {
        System.out.println("createUsernameHueBridge()");
        final StringEntity entity;
        try {

            entity = new StringEntity("{\"devicetype\":\"HueApp#ComfyCrew\"}");
            AsyncHttpClient client = new AsyncHttpClient();

            client.post(mContext, "http://" + bridgeIp + "/api/", entity, "application/json", new JsonHttpResponseHandler() {

//                @Override
//                public void onStart() {
//                    System.out.println(entity.toString());
//                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    // called when response HTTP status is "200 OK"
//                    System.out.println("Bridge Username get: " + response.toString());
                    try {
                        if( response.getJSONObject(0).toString().contains("error") )
                            System.out.println("Please press the link button");
                        else
                            username = response.getJSONObject(0).getJSONObject("success")
                                    .getString("username");
                        System.out.println(username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public void getAllLamps() {
        System.out.println("getAllLamps()");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://" + bridgeIp + "/api/" + username + "/lights/", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                JSONArray lampIds = response.names();
                int length = lampIds.length();
                for (int i = 0; i < length; i++) {
                    if (lampIds.toString().contains("\"" + Integer.toString(i) + "\""))
                        try {
                            lightArray.add(parseLampData(response.getJSONObject(Integer.toString(i)), i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    else length++;
                }
            }
        });

    }

    public HueLight parseLampData(JSONObject jsonDataHue, int id) throws JSONException {
        System.out.println("parseLampData()");
        HueLight hueLight = new HueLight();
        JSONObject jsonHueLightState = jsonDataHue.getJSONObject("state");
        hueLight.id = id;
        hueLight.name = jsonDataHue.getString("name");
        hueLight.isOn = jsonHueLightState.getBoolean("on");
        if (jsonDataHue.getString("type").equals("Extended color light")) {
            hueLight.hue = jsonHueLightState.getInt("hue");
            hueLight.saturation = jsonHueLightState.getInt("sat");
        }
        hueLight.brightness = jsonHueLightState.getInt("bri");
        return hueLight;
    }

    public void setLampState(HueLight hueLight) {
        System.out.println("setLampState()");
    }

    public void saveBridgeSettings() {
        System.out.println("saveBridgeSettings()");
    }

    public void loadBridgeSettings() {
        System.out.println("loadBridgeSettings()");
    }

    public void resetBridgeSettings() {
        System.out.println("resetBridgeSettings()");
    }

}