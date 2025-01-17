package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ServerEntity;
import com.melli.hub.domain.master.entity.ServerHistoryEntity;
import com.melli.hub.domain.master.persistence.ServerHistoryRepository;
import com.melli.hub.domain.master.persistence.ServerRepository;
import com.melli.hub.domain.response.base.AddServerResponse;
import com.melli.hub.domain.response.base.ServerResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ServerService;
import com.melli.hub.service.StatusService;
import com.melli.hub.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class ServerServiceImplementation implements ServerService {


    private final ServerRepository serverRepository;
    private final ServerHistoryRepository serverHistoryRepository;
    private final Helper helper;

    @Override
    public AddServerResponse batchInsert(MultipartFile multipartFile, String serverIp, String serverName, String protocol, boolean isJson, String country) throws InternalServiceException {

        int allRow = 0;
        int successRow = 0;
        int failRow = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allRow++;
                ServerEntity serverEntity = new ServerEntity();
                serverEntity.setConfig(line);
                serverEntity.setServerIp(serverIp);
                serverEntity.setServerName(serverName);
                serverEntity.setStatus("CREATED");
                serverEntity.setProtocol(protocol);
                serverEntity.setCreatedBy("Admin");
                serverEntity.setCountUsed(0);
                serverEntity.setCreatedAt(new Date());
                serverEntity.setJson(isJson);
                serverEntity.setCountry(country);
                serverRepository.save(serverEntity);
                successRow++;
            }
        } catch (IOException e) {
            failRow++;
        }

        return helper.fillAddServerResponse(String.valueOf(allRow), String.valueOf(failRow), String.valueOf(successRow));

    }

    @Override
    public AddServerResponse batchInsertWithTemplate(MultipartFile templateFile, MultipartFile valueFile, String serverIp, String serverName, String protocol, boolean isJson, String country, String keyName) throws InternalServiceException {
        int allRow = 0;
        int successRow = 0;
        int failRow = 0;

        String templateStr = readTemplateAndRemoveExtraData(templateFile);

        if(templateStr == null){
            log.error("template is null");
            throw new InternalServiceException("فایل template قابل پردازش نیست", StatusService.CAN_NOT_READ_FILE, HttpStatus.BAD_REQUEST);
        }


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(valueFile.getInputStream()))) {
            String line;
            String template = "";
            while ((line = reader.readLine()) != null) {
                allRow++;
                String uuid = extractUuidValueFromUrl(line);
                if(uuid == null){
                    log.error("can not extract uuid from line ({})", line);
                    continue;
                }
                String placeholder = "%" + keyName + "%"; // Placeholder format: %key%
                template = templateStr.replace(placeholder, uuid);
                log.info("template create with uuid ({}) is ({})", uuid, template);
                ServerEntity serverEntity = new ServerEntity();
                serverEntity.setConfig(template);
                serverEntity.setServerIp(serverIp);
                serverEntity.setServerName(serverName);
                serverEntity.setStatus("CREATED");
                serverEntity.setProtocol(protocol);
                serverEntity.setCreatedBy("Admin");
                serverEntity.setCountUsed(0);
                serverEntity.setCreatedAt(new Date());
                serverEntity.setJson(isJson);
                serverEntity.setCountry(country);
                serverRepository.save(serverEntity);
                successRow++;
            }

        } catch (IOException e) {
            failRow++;
        }
        return helper.fillAddServerResponse(String.valueOf(allRow), String.valueOf(failRow), String.valueOf(successRow));
    }


    //vless://2d11f631-5b5d-4404-c4b6-508e80df56e9@se.khalil-online.space:55000?mode=multi&security=reality&encryption=none&pbk=gnLadfh2TQ6-qfBSVA2LP3x-UhqMBSbxAoF5akDxrUQ&fp=randomized&spx=%2F&type=grpc&sni=www.target.com#test676y67
    public static String extractUuidValueFromUrl(String strInput) {
        // Input string
        // Regex pattern to match the UUID
        String regex = "vless://([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})@";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strInput);

        // Find and extract the UUID
        if (matcher.find()) {
            String uuid = matcher.group(1); // Group 1 contains the UUID
            log.info("Extracted UUID: " + uuid);
            return uuid;
        } else {
            log.error("UUID not found in the input ({}).", strInput);
            return null;
        }
    }


    private String readTemplateAndRemoveExtraData(MultipartFile template) {
        try {
            BufferedReader readerTemplate = new BufferedReader(new InputStreamReader(template.getInputStream()));
            String line = readerTemplate.readLine();
            return line.replace("\n", "");
        } catch (IOException ex) {
            log.error("can not read file!!!!");
            return null;
        }
    }

    @Override
    public ServerResponse getServer(String deviceName, String ip) throws InternalServiceException {
        log.info("start get config from ip ({}) and deviceName ({})", ip, deviceName);
        List<ServerEntity> serverEntityList = serverRepository.findAllByStatusOrderByCountUsedAsc("CREATED");
        ServerEntity serverEntity = serverEntityList.get(0);
        ServerHistoryEntity serverHistoryEntity = new ServerHistoryEntity();
        serverHistoryEntity.setServerEntity(serverEntity);
        serverHistoryEntity.setIp(ip);
        serverHistoryEntity.setDeviceId(deviceName);
        serverHistoryEntity.setCreatedBy("Admin");
        serverHistoryEntity.setCreatedAt(new Date());
        serverHistoryRepository.save(serverHistoryEntity);

        serverEntity.setCountUsed((serverEntity.getCountUsed() == null ? 0 : serverEntity.getCountUsed()) + 1);
        serverEntity.setLastUsedTime(new Date());
        serverRepository.save(serverEntity);
        if(deviceName == null || deviceName.length() < 5){
            log.error("deviceName ({}) is invalid and we send invalid data ({})", deviceName, deviceName);
            return new ServerResponse("vless://6c1e73d0-0f38-4c8e-a9d0-798d25a4a2a9@WwW.spEEdTest.net:2082?type=ws&path=%2F%3Fed%3D1024&host=download.gamecentermahan.info&security=none#WS-Test-17k7pa09", serverEntity.getCountry(), serverEntity.isJson());
        }

        return new ServerResponse(serverEntity.getConfig(), serverEntity.getCountry(), serverEntity.isJson());
    }
}
