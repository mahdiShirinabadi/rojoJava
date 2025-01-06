package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.RequestTypeEntity;
import com.melli.hub.domain.master.persistence.RequestTypeRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.RequestTypeService;
import com.melli.hub.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class RequestTypeServiceImplementation implements RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;

    @Override
    public RequestTypeEntity getRequestType(String name) {
        return requestTypeRepository.findByName(name);
    }

    @Override
    public List<RequestTypeEntity> findAllRequestType() {
        return requestTypeRepository.findAll();
    }

    @Override
    public RequestTypeEntity findById(Long id) throws InternalServiceException {
        return requestTypeRepository.findById(id).orElseThrow(() -> {
            log.error("RequestType not found, id ({})", id);
            return new InternalServiceException("RequestType not found", StatusService.REQUEST_TYPE_NOT_FOUND, HttpStatus.OK);
        });
    }
}
