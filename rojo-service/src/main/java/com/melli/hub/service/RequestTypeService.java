package com.melli.hub.service;

import com.melli.hub.domain.master.entity.RequestTypeEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;


public interface RequestTypeService {
    String BATCH_INSERT = "BATCH_INSERT";

    RequestTypeEntity getRequestType(String name);
    List<RequestTypeEntity> findAllRequestType();
    RequestTypeEntity findById(Long id) throws InternalServiceException;
}
