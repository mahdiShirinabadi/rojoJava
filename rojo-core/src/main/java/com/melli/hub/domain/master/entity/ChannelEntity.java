package com.melli.hub.domain.master.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "channel")
public class ChannelEntity extends BaseEntityAudit implements Serializable, UserDetails {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "access_token")
    @ToString.Exclude
    private String accessToken;

    @Column(name = "refresh_token")
    @ToString.Exclude
    private String refreshToken;

    @Column(name = "token_expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpireTime;

    @Column(name = "expire_time_duration")
    private long expireTimeDuration;

    @Column(name = "status")
    private int status;

    @Column(name = "trust")
    private Integer trust;

    @Column(name = "sign")
    private Integer sign;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "ip")
    private String ip;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "channelEntity")
    private List<ChannelRequestTypeEntity> channelAccessList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return channelAccessList.stream().map(ChannelRequestTypeEntity::getRequestType).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
