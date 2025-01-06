package com.melli.hub.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;

@Entity
@Table(name = "request_type")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RequestTypeEntity extends BaseEntityAudit implements GrantedAuthority {


    @Column(name= "name")
    private String name;

    @Column(name= "fa_name")
    private String faName;

    @Column(name= "display")
    private int display;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Override
    public String getAuthority() {
        return name;
    }
}
