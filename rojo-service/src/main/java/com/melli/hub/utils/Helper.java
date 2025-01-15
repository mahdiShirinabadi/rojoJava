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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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


    public static Map<String, String> parseVlessUri(String uri, boolean version) throws URISyntaxException {
        URI parsedUri = new URI(uri);
        String scheme = parsedUri.getScheme();
        String uuid = parsedUri.getUserInfo();
        String host = parsedUri.getHost();
        int port = parsedUri.getPort();
        String query = parsedUri.getQuery();
        String fragment = parsedUri.getFragment();

        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    if (version) {
                        value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                    queryParams.put(key, value);
                }
            }
        }

        Map<String, String> parsedData = new HashMap<>();
        parsedData.put("scheme", scheme);
        parsedData.put("uuid", uuid);
        parsedData.put("host", host);
        parsedData.put("port", String.valueOf(port));
        parsedData.putAll(queryParams);
        parsedData.put("fragment", fragment);

        return parsedData;
    }

    public static String buildConfig(Map<String, String> parsedData) throws JSONException {
        JSONObject config = new JSONObject();

        String alpnValue = parsedData.get("alpn");

// Initialize an empty JSONArray for ALPN
        JSONArray alpnArray = new JSONArray();

// Check if ALPN value is not null or empty
        if (alpnValue != null && !alpnValue.trim().isEmpty()) {
            // Split the ALPN value by commas and add to the JSONArray
            String[] alpnValues = alpnValue.split(",");
            for (String value : alpnValues) {
                if (!value.trim().isEmpty()) { // Skip empty values
                    alpnArray.put(value.trim());
                }
            }
        }

        // Inbounds (local proxy settings)
        JSONArray inbounds = new JSONArray();
        JSONObject socksInbound = new JSONObject()
                .put("port", 10808) // Local SOCKS5 port
                .put("protocol", "socks")
                .put("settings", new JSONObject()
                        .put("auth", "noauth")
                        .put("udp", true));
        inbounds.put(socksInbound);
        config.put("inbounds", inbounds);

        // Outbounds (server connection settings)
        JSONArray outbounds = new JSONArray();
        JSONObject vlessOutbound = new JSONObject()
                .put("protocol", "vless")
                .put("settings", new JSONObject()
                        .put("vnext", new JSONArray().put(new JSONObject()
                                .put("address", parsedData.get("host"))
                                .put("port", Integer.parseInt(parsedData.get("port")))
                                .put("users", new JSONArray().put(new JSONObject()
                                        .put("id", parsedData.get("uuid"))
                                        .put("encryption", "none")
                                ))
                        )))
                .put("streamSettings", new JSONObject()
                        .put("network", parsedData.get("type"))
                        .put("security", parsedData.get("security"))
                        .put("httpSettings", new JSONObject()
                                .put("path", parsedData.get("path"))
                                .put("headers", new JSONObject()
                                        .put("Host", parsedData.get("host")))
                        )
                        .put("tlsSettings", new JSONObject()
                                .put("serverName", parsedData.get("sni"))
                                .put("fingerprint", parsedData.get("fp"))
                                .put("alpn", alpnArray)
                        )
                );
        outbounds.put(vlessOutbound);
        config.put("outbounds", outbounds);

        return config.toString();
    }


    public static void main(String[] args) throws Exception {
        String url="vless://d135d0d1-8d82-4058-a69e-c690ed145615@torobcom.gamecentermahan.info:2087?type=httpupgrade&path=%2F%3Fed%3D1024&host=torobcom.gamecentermahan.info&security=tls&fp=firefox&alpn=h2%2Chttp%2F1.1&sni=torobcom.gamecentermahan.info#NginX-01-user1SRV2";
        Map<String, String> parseVlessUri = parseVlessUri(url,true);
        String json = (buildConfig(parseVlessUri));
        String DEFAULT_FULL_JSON_CONFIG = "{\n" +
                "  \"dns\": {\n" +
                "    \"hosts\": {\n" +
                "      \"domain:googleapis.cn\": \"googleapis.com\"\n" +
                "    },\n" +
                "    \"servers\": [\n" +
                "      \"1.1.1.1\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"inbounds\": [\n" +
                "    {\n" +
                "      \"listen\": \"127.0.0.1\",\n" +
                "      \"port\": 10808,\n" +
                "      \"protocol\": \"socks\",\n" +
                "      \"settings\": {\n" +
                "        \"auth\": \"noauth\",\n" +
                "        \"udp\": true,\n" +
                "        \"userLevel\": 8\n" +
                "      },\n" +
                "      \"sniffing\": {\n" +
                "        \"destOverride\": [],\n" +
                "        \"enabled\": false\n" +
                "      },\n" +
                "      \"tag\": \"socks\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"listen\": \"127.0.0.1\",\n" +
                "      \"port\": 10809,\n" +
                "      \"protocol\": \"http\",\n" +
                "      \"settings\": {\n" +
                "        \"userLevel\": 8\n" +
                "      },\n" +
                "      \"tag\": \"http\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"log\": {\n" +
                "    \"loglevel\": \"error\"\n" +
                "  },\n" +
                "  \"outbounds\": [\n" +
                "    "+ json +",\n" +
                "    {\n" +
                "      \"protocol\": \"freedom\",\n" +
                "      \"settings\": {},\n" +
                "      \"tag\": \"direct\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"protocol\": \"blackhole\",\n" +
                "      \"settings\": {\n" +
                "        \"response\": {\n" +
                "          \"type\": \"http\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"tag\": \"block\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"remarks\": \"test\",\n" +
                "  \"routing\": {\n" +
                "    \"domainStrategy\": \"IPIfNonMatch\",\n" +
                "    \"rules\": [\n" +
                "      {\n" +
                "        \"ip\": [\n" +
                "          \"1.1.1.1\"\n" +
                "        ],\n" +
                "        \"outboundTag\": \"proxy\",\n" +
                "        \"port\": \"53\",\n" +
                "        \"type\": \"field\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        System.out.println(DEFAULT_FULL_JSON_CONFIG);
    }



}