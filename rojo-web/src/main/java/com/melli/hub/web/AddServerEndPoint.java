package com.melli.hub.web;

import com.melli.hub.domain.response.base.AddServerResponse;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.base.ServerResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.utils.Helper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Class Name: AddServerEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Validated
@Log4j2
public class AddServerEndPoint extends WebEndPoint {

    private final AuthenticationManager authenticationManager;
    private final ServerService serverService;

    @Timed(description = "inquiry created issue")
    @GetMapping(path = "/getConfig/{deviceName}")
    public ResponseEntity<BaseResponse<ServerResponse>> inquiryIssue(HttpServletRequest request, @PathVariable String deviceName) throws InternalServiceException {
        ServerResponse server = serverService.getServer(deviceName, getIP(request));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, server));
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "ایحاد دسته ای کاربران", description = "add user by xlsx format")
    @Timed(description = "menu duration add user")
    @PostMapping(path = "addServer", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AddServerResponse> batchUserInsert(@Valid @NotNull @RequestParam("file") MultipartFile file,
                                                             @RequestParam("serverName") String serverName,
                                                             @RequestParam("serverIp") String serverIp,
                                                             @RequestParam("protocol") String protocol,
                                                             HttpServletRequest request) throws InternalServiceException {
        AddServerResponse excelAddProfileToUnitResponse = serverService.batchInsert(file, serverIp, serverName, protocol);
        return ResponseEntity.status(HttpStatus.OK).body(excelAddProfileToUnitResponse);
    }


   /* @Operation(summary = "ورود به حساب کاربری")
    @Timed(description = "time taken to login profile")
    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestJson loginJson, HttpServletRequest httpRequest) throws InternalServiceException {
        try {
            authenticate(loginJson.getUsername(), loginJson.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginJson.getUsername());
            ProfileEntity profileEntity = profileService.findByNationalCode(loginJson.getUsername());
            boolean isAfter = checkExpiration(profileEntity);
            if (helper.notInAllowedList(profileEntity.getValidIp(), getIP(httpRequest))) {
                log.error("ip ({}), not exist in valid ip list ({})", getIP(httpRequest), profileEntity.getValidIp());
                throw new InternalServiceException("ip (" + getIP(httpRequest) + ", not exist in valid ip list ( " + profileEntity.getValidIp() + ")", StatusService.INVALID_IP_ADDRESS, HttpStatus.FORBIDDEN);
            }

            if (securityService.isBlock(profileEntity.getId())) {
                log.info("channel ({}) is blocked !!!", loginJson.getUsername());
                throw new InternalServiceException("channel (" + loginJson.getUsername() + ") is blocked", StatusService.CHANNEL_IS_BLOCKED, HttpStatus.FORBIDDEN);
            }

            log.info("start generate token for username ({}), Ip ({})...", loginJson.getUsername(), getIP(httpRequest));
            Map<String, String> token = jwtTokenUtil.generateToken(userDetails.getUsername(), Long.parseLong(settingService.getSetting(SettingService.EXPIRE_TIME_PROFILE).getValue()));
            ProfileAccessTokenEntity profileAccessTokenEntity = new ProfileAccessTokenEntity();
            profileAccessTokenEntity.setProfileEntity(profileEntity);
            profileAccessTokenEntity.setAccessToken(token.get("token"));
            profileAccessTokenService.save(profileEntity, token.get("token"), requestContext.getClientIp(), loginJson.getDeviceName(), loginJson.getAdditionalData());
            log.info("success generate token for username ({}), Ip ({})", loginJson.getUsername(), getIP(httpRequest));
            securityService.resetFailLoginCount(profileEntity);
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, helper.fillLoginResponse(profileEntity, token.get("token"), Long.parseLong(token.get("expireTime")))));
        } catch (ServiceException ex) {
            log.error("failed in login with ServiceException ({})", ex.getMessage());
            throw ex;
        } catch (InternalServiceException ex) {
            log.error("failed in login with InternalServiceException ({})", ex.getMessage());
            throw ex;
        } catch (BadCredentialsException ex) {
            log.error("failed in login with BadCredentialsException ({})", ex.getMessage());
            throw new InternalServiceException("invalid username password", StatusService.INVALID_USERNAME_PASSWORD, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("failed in login with Exception ({})", ex.getMessage());
            throw new InternalServiceException("general error", StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }*/


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


    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = Helper.encodePassword(passwordEncoder, "0077847660", "0077847660");
        System.out.println(password);
    }

}
