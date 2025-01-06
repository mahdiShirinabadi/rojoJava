package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "server")
@ToString
@NoArgsConstructor
@Setter
@Getter
public class ServerEntity extends BaseEntityAudit implements Serializable {

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "server_ip")
    private String serverIp;

    @Column(name = "config")
    private String config;

    @Column(name = "status")
    private String status;

    @Column(name = "count_used")
    private Integer countUsed;

    @Column(name = "protocol")
    private String protocol;


    @Column(name = "last_used_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsedTime;
}
