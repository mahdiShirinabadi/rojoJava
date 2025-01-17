package com.melli.hub.service;

import com.melli.hub.domain.master.entity.StatusEntity;

public interface StatusService {

    int TOKEN_NOT_VALID=2;
    int USER_NOT_PERMISSION=3;
    int REQUEST_TYPE_NOT_FOUND=4;
    int CHANNEL_NOT_FOUND=5;
    int INVALID_USERNAME_PASSWORD=6;

    int SUSPEND = 900;
    int CAN_NOT_READ_FILE = 987;
    int ERROR_IN_LOCK = 988;
    int TIMEOUT = 998;
    int GENERAL_ERROR = 999;


    void init();

    StatusEntity findByCode(String code);

}
