package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ServerEntity;
import com.melli.hub.domain.master.entity.ServerHistoryEntity;
import com.melli.hub.domain.master.persistence.ServerHistoryRepository;
import com.melli.hub.domain.master.persistence.ServerRepository;
import com.melli.hub.domain.response.base.AddServerResponse;
import com.melli.hub.domain.response.base.ServerResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ServerService;
import com.melli.hub.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ServerServiceImplementation implements ServerService {


    private final ServerRepository serverRepository;
    private final ServerHistoryRepository serverHistoryRepository;
    private final Helper helper;

    @Override
    public AddServerResponse batchInsert(MultipartFile multipartFile, String serverIp, String serverName, String protocol) throws InternalServiceException {

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
                serverRepository.save(serverEntity);
                successRow++;
            }
        } catch (IOException e) {
            failRow++;
        }

        return helper.fillAddServerResponse(String.valueOf(allRow), String.valueOf(failRow), String.valueOf(successRow));

    }

    @Override
    public ServerResponse getServer(String deviceName, String ip) throws InternalServiceException {
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

        return new ServerResponse(serverEntity.getConfig());
    }
}
