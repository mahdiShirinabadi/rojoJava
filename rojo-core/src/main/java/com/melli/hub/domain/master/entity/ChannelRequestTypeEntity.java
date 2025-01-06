package com.melli.hub.domain.master.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Created by shirinabadi on 03/11/2016.
 *
 */
@Entity
@Table(name = "channel_request_type")
@Setter
@Getter
public class ChannelRequestTypeEntity extends BaseEntityAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_type_id", nullable = false)
    private RequestTypeEntity requestType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", nullable = false)
    private ChannelEntity channelEntity;


    @Override
    public String toString() {
        return "{" + requestType.getName() +'}';
    }
}
