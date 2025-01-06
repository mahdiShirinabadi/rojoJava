package com.melli.hub.security;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtProfileDetailsService implements UserDetailsService {


    private final ChannelService channelService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ChannelEntity channelEntity = channelService.findByUsername(username);
        if (channelEntity == null) {
            log.error("user with username ({}) not found", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return channelEntity;
    }
}
