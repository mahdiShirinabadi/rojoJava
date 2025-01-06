package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.StatusEntity;
import com.melli.hub.domain.master.persistence.StatusRepository;

import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import com.melli.hub.utils.Constant;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Log4j2
@RequiredArgsConstructor
public class StatusServiceImplementation implements StatusService {

    private final StatusRepository statusRepository;


    @PostConstruct
    @Override
    public void init() {
        log.info("Start init status .....");
    }

    @Override

    public StatusEntity findByCode(String code) {
        log.info("Starting retrieval of Status by code: {}", code);
        StatusEntity statusEntity = statusRepository.findByCode(code);

        if (statusEntity == null) {
            log.info("Status code not defined: {}", code);
            return createUndefinedStatusEntity(code);
        }
        return statusEntity;
    }

    private StatusEntity createUndefinedStatusEntity(String code) {
        StatusEntity undefinedStatus = new StatusEntity();
        undefinedStatus.setCode(code);
        undefinedStatus.setPersianDescription("کد تعریف نشده است");
        return undefinedStatus;
    }
}
