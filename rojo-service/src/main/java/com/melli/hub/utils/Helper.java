package com.melli.hub.utils;

import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.response.base.AddServerResponse;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.base.ErrorDetail;
import com.melli.hub.util.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class Helper {

    public static final String FORMAT_DATE_RESPONSE = "yyyy/MM/dd HH:mm:ss";
    private final Logger log = LogManager.getLogger(Helper.class);

    public BaseResponse<ObjectUtils.Null> fillBaseResponse(boolean result, Date doTime, ErrorDetail errorDetail) {
        BaseResponse<ObjectUtils.Null> response = new BaseResponse<>(result);
        response.setErrorDetail(errorDetail);
        return response;
    }



    public AddServerResponse fillAddServerResponse(String countAll, String countFail, String countSuccess){
        AddServerResponse response = new AddServerResponse();
        response.setCountAllRow(countAll);
        response.setCountFailRow(countFail);
        response.setCountSuccessRow(countSuccess);
        return response;
    }


    public static String findInListMapValueByKey(List<Map<String, String>> listOfMaps, String key) {
        return listOfMaps.stream().filter(map -> map.containsKey(key) && StringUtils.hasText(map.get(key))).map(map -> map.get(key)).findFirst().orElse(null); // Return null if no match is found
    }

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static String encodePassword(PasswordEncoder passwordEncoder, String username, String password){
        return passwordEncoder.encode(username + "M@hd!" + password);
    }

    public boolean notInAllowedList(String allowedList, String ip) {
        log.info("start check Ip ({}) in allowedList ({})", ip, allowedList);
        if(!StringUtils.hasText(allowedList)){
            log.info("allewdList is empty !!!");
            return false;
        }
        boolean isExist = false;
        List<String> ipList = new LinkedList<>(Arrays.asList(allowedList.split(";")));
        for (String validIp : ipList) {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(validIp);
            isExist = ipAddressMatcher.matches(ip);
            if (isExist) {
                log.info("ip ({}) is match with value ({})", ip, validIp);
                return false;
            }
        }
        log.info("result ({}) for Ip ({})", false, ip);
        return true;
    }

    public static Integer generateRandomNumber() {
        Random r = new Random(System.currentTimeMillis());
        return ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }


    public static String generateVlessURIs(String jsonString, String serverIp) {
        try {
            // Parse the root JSON object
            JSONObject root = new JSONObject(jsonString);
            JSONObject obj = root.getJSONObject("obj");

            // Extract fields from the JSON object
            String remark = obj.optString("remark", "");
            int port = obj.getInt("port");
            String settingsJson = obj.getString("settings");
            String streamSettingsJson = obj.getString("streamSettings");

            // Parse settings and streamSettings
            JSONObject settings = new JSONObject(settingsJson);
            JSONArray clients = settings.getJSONArray("clients");
            JSONObject streamSettings = new JSONObject(streamSettingsJson);
            String network = streamSettings.getString("network");
            String security = streamSettings.getString("security");

            // Generate VLESS URIs
            StringBuilder uris = new StringBuilder();
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                String clientId = client.getString("id");
                String flow = client.optString("flow", "");

                // Format the VLESS URI
                String vlessUri = String.format(
                        "vless://%s@%s:%d?flow=%s&encryption=none&type=%s&security=%s#%s",
                        clientId, serverIp, port,
                        flow.isEmpty() ? "none" : flow, network, security,
                        remark.isEmpty() ? "VLESS Client" : remark
                );

                uris.append(vlessUri).append("\n");
            }

            return uris.toString().trim();
        } catch (JSONException e) {
            e.printStackTrace();
            return ""; // Handle the error appropriately
        }
    }
}