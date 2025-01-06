package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.exception.InternalServiceException;


public interface ChannelService {
    int FALSE = 0;

    ChannelEntity findByUsername(String username);

    ChannelEntity findById(Long id) throws InternalServiceException;

    ChannelEntity findByUsernameAndStatus(String username, String status) throws InternalServiceException;

    ChannelEntity save(ChannelEntity channelEntity);

    void logout(ChannelEntity channelEntity);
}
