package com.vimalselvam;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class provides the capability to connect or disconnect device.
 */
public class DeviceApi {
    private OkHttpClient client;
    private JsonParser jsonParser;
    private STFService stfService;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    public DeviceApi(STFService stfService) {
        this.client = new OkHttpClient();
        this.jsonParser = new JsonParser();
        this.stfService = stfService;
    }

    public boolean connectDevice(String deviceSerial, String remoteUrl) throws InterruptedException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "devices/" + deviceSerial)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            JsonObject deviceObject = jsonObject.getAsJsonObject("device");
            boolean present = deviceObject.get("present").getAsBoolean();
            boolean ready = deviceObject.get("ready").getAsBoolean();
            boolean using = deviceObject.get("using").getAsBoolean();
            JsonElement ownerElement = deviceObject.get("owner");
            boolean owner = !(ownerElement instanceof JsonNull);

            if (!present || !ready) {
                LOGGER.severe("Device is in use");
                return false;
            }

            addDeviceToUser(deviceSerial, remoteUrl);
            Thread.sleep(5000);
            return test(deviceSerial);
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

    private boolean isDeviceFound(JsonObject jsonObject) {
        if (!jsonObject.get("success").getAsBoolean()) {
            LOGGER.severe("Device not found");
            return false;
        }
        return true;
    }

    public boolean addDeviceToUser(String deviceSerial, String remoteUrl) {
        RequestBody requestBody = RequestBody.create(JSON, "{\"serial\": \"" + deviceSerial + "\"}");
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "user/devices" + "/" + deviceSerial /*+ "/remoteConnect"*/)
                .post(requestBody)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            LOGGER.info("The device <" + deviceSerial + "> is locked successfully");
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

    public boolean test(String deviceSerial) {
        RequestBody requestBody = RequestBody.create(JSON, "{\"serial\": \"" + deviceSerial + "\"}");
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "user/devices" + "/" + deviceSerial + "/remoteConnect")
                .post(requestBody)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            LOGGER.info("The device <" + deviceSerial + "> is locked successfully");
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

//    public boolean addDeviceToUser(String deviceSerial, String remoteUrl) {
//        RequestBody requestBody = RequestBody.create(JSON, "{\"serial\": \"" + deviceSerial + "\", " +
//                "\"remoteConnect\": " + true + ", " +
//                "\"remoteConnectUrl\": \"" + remoteUrl + "\", " +
//                "\"using\": "+ true + "}");
//        Request request = new Request.Builder()
//                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
//                .url(stfService.getStfUrl() + "user/devices/")
//                .post(requestBody)
//                .build();
//        Response response;
//        try {
//            response = client.newCall(request).execute();
//            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();
//
//            if (!isDeviceFound(jsonObject)) {
//                return false;
//            }
//
//            LOGGER.info("The device <" + deviceSerial + "> is locked successfully");
//            return true;
//        } catch (IOException e) {
//            throw new IllegalArgumentException("STF service is unreachable", e);
//        }
//    }

    public boolean releaseDevice(String deviceSerial) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "user/devices/" + deviceSerial)
                .delete()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            LOGGER.info("The device <" + deviceSerial + "> is released successfully");
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

}
