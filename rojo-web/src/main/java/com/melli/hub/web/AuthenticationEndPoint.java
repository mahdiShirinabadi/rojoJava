package com.melli.hub.web;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.request.login.LoginRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.base.LoginResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.security.JwtProfileDetailsService;
import com.melli.hub.security.JwtTokenUtil;
import com.melli.hub.security.RequestContext;
import com.melli.hub.service.ChannelService;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.Validator;
import com.melli.hub.utils.Helper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channel/authentication")
@Validated
@Log4j2
public class AuthenticationEndPoint extends WebEndPoint {
    private final RequestContext context;
    private final AuthenticationManager authenticationManager;
    private final JwtProfileDetailsService userDetailsService;
    private final ChannelService channelService;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "ورود به حساب")
    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestJson loginJson, HttpServletRequest httpRequest) throws InternalServiceException {
        try {
            authenticate(loginJson.getUsername(), loginJson.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginJson.getUsername());
            ChannelEntity channelEntity = channelService.findByUsername(loginJson.getUsername());
            boolean isAfter = checkExpiration(channelEntity);
            if (!Validator.isNull(channelEntity.getAccessToken()) && isAfter) {
                log.info("token for channel ({}) is not expired and we will return current token", channelEntity.getAccessToken());
                return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, new LoginResponse(channelEntity.getAccessToken(), jwtTokenUtil.getExpirationDateFromToken(channelEntity.getAccessToken()).getTime())));
            }
            log.info("start generate token for username ({}), Ip ({})...", loginJson.getUsername(), getIP(httpRequest));
            Map<String, String> token = jwtTokenUtil.generateToken(userDetails.getUsername(), channelEntity.getExpireTimeDuration());
            channelEntity.setAccessToken(token.get("token"));
            channelEntity.setTokenExpireTime(new Date(jwtTokenUtil.getExpirationDateFromToken(token.get("token")).getTime()));
            channelService.save(channelEntity);
            log.info("success generate token for username ({}), Ip ({})", loginJson.getUsername(), getIP(httpRequest));
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, new LoginResponse(channelEntity.getAccessToken(), Long.parseLong(token.get("expireTime")))));
        } catch (BadCredentialsException ex) {
            log.error("failed in login with BadCredentialsException ({})", ex.getMessage());
            throw new InternalServiceException("invalid username password", StatusService.INVALID_USERNAME_PASSWORD, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("failed in login with Exception ({})", ex.getMessage());
            throw new InternalServiceException("general error", StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    private void authenticate(String username, String password) {
        try {
            log.info("start authenticate for username ({})...", username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, username + "M@hd!" + password));
            log.info("success authenticate for username ({})", username);
        } catch (DisabledException ex) {
            log.error("failed authenticate for username ({}), with DisabledException ({})", username, ex.getMessage());
            throw new BadCredentialsException("USER_DISABLED", ex);
        } catch (BadCredentialsException ex) {
            log.error("failed authenticate for username ({}), with BadCredentialsException ({})", username, ex.getMessage());
            throw new BadCredentialsException("INVALID_CREDENTIALS for username (" + username + ")", ex);
        }
    }

    private boolean checkExpiration(ChannelEntity channelEntity) {
        log.info("start check expiration for channel ({})...", channelEntity.getUsername());
        if (channelEntity.getAccessToken() != null) {
            try {
                return jwtTokenUtil.getExpirationDateFromToken(channelEntity.getAccessToken()).after(new Date());
            } catch (Exception ex) {
                log.error("failed check expiration for channel ({}), token is expired with error ({})",
                        channelEntity.getUsername(), ex.getMessage());
            }
        }
        return false;
    }

    @Timed(description = "time taken to logout channel")
    @PostMapping(path = "/logout", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "خروج از حساب")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> logout() {
        log.info("start logout channel by ({}), ip({})", context.getChannelEntity().getUsername(), context.getClientIp());
        channelService.logout(context.getChannelEntity());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("masoud" + "M@hd!" + "masoud1367"));
    }
}
