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
                                                             @RequestParam("country") String country,
                                                             @RequestParam("isJson") boolean isJson,
                                                             HttpServletRequest request) throws InternalServiceException {
        AddServerResponse excelAddProfileToUnitResponse = serverService.batchInsert(file, serverIp, serverName, protocol, isJson, country);
        return ResponseEntity.status(HttpStatus.OK).body(excelAddProfileToUnitResponse);
    }


    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "ایحاد دسته ای کاربران", description = "add user by xlsx format")
    @Timed(description = "menu duration add user")
    @PostMapping(path = "addServerWithTemplate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AddServerResponse> batchServerInsert(
            @Valid @NotNull @RequestParam("template") MultipartFile template,
            @Valid @NotNull @RequestParam("urls") MultipartFile urls,
            @RequestParam("serverName") String serverName,
            @RequestParam("serverIp") String serverIp,
            @RequestParam("protocol") String protocol,
            @RequestParam("country") String country,
            @RequestParam("keyName") String keyName,
            HttpServletRequest request) throws InternalServiceException {
        AddServerResponse excelAddProfileToUnitResponse = serverService.batchInsertWithTemplate(template, urls, serverIp, serverName, protocol, true, country, keyName);
        return ResponseEntity.status(HttpStatus.OK).body(excelAddProfileToUnitResponse);
    }


    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = Helper.encodePassword(passwordEncoder, "0077847660", "0077847660");
        System.out.println(password);
    }

}
