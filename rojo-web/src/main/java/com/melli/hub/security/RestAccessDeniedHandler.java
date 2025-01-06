package com.melli.hub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.melli.hub.domain.master.entity.StatusEntity;
import com.melli.hub.domain.response.base.ErrorDetail;
import com.melli.hub.service.StatusService;
import com.melli.hub.utils.Helper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final Helper helper;
    private final StatusService statusService;

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        StatusEntity statusEntity = statusService.findByCode(String.valueOf(StatusService.USER_NOT_PERMISSION));
        ErrorDetail errorDetail = new ErrorDetail(statusEntity.getPersianDescription(), StatusService.USER_NOT_PERMISSION);

        response.getWriter().print(ow.writeValueAsString(helper.fillBaseResponse(false, new Date(), errorDetail)));
    }
}
