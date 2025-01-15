package com.melli.hub.service;


import com.melli.hub.domain.response.base.AddServerResponse;
import com.melli.hub.domain.response.base.ServerResponse;
import com.melli.hub.exception.InternalServiceException;
import org.springframework.web.multipart.MultipartFile;


/**
 * Class Name: AuthenticationService
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ServerService {
    AddServerResponse batchInsert(MultipartFile multipartFile, String serverIp, String serverName, String protocol, boolean isJson, String country) throws InternalServiceException;
    ServerResponse getServer(String deviceName, String ip) throws InternalServiceException;
}
