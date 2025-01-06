package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "server_history")
@ToString
@NoArgsConstructor
@Setter
@Getter
public class ServerHistoryEntity extends BaseEntityAudit implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity serverEntity;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "device_id", nullable = false)
    private String deviceId;
}
