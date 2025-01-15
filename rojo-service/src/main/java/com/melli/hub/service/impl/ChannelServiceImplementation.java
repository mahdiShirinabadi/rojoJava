package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.persistence.ChannelRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ChannelService;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@RequiredArgsConstructor
@Log4j2
@Service
public class ChannelServiceImplementation implements ChannelService {
    private final ChannelRepository channelRepository;
    private final Helper helper;
    private final RedisLockService redisLockService;

    @Override
    public ChannelEntity findByUsernameAndStatus(String username, String status) throws InternalServiceException {
        ChannelEntity profileEntity = channelRepository.findByUsernameAndStatus(username, Integer.parseInt(status));
        if (profileEntity == null) {
            throw new InternalServiceException("user not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.FORBIDDEN);
        }
        return profileEntity;

    }

    @Override
    public ChannelEntity findByUsername(String username) {
        log.info("start findByUsername for username({})", username);
        return channelRepository.findByUsername(username);
    }


    @Override
    public ChannelEntity findById(Long id) throws InternalServiceException {
        log.info("find channel by id ({})", id);
        Optional<ChannelEntity> optional = channelRepository.findById(id);
        return optional.orElseThrow(() -> new InternalServiceException("channel not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.FORBIDDEN));
    }

    @Override
    public ChannelEntity save(ChannelEntity channelEntity) {
        return channelRepository.save(channelEntity);
    }



    private void updateChannelEntity(
            String updatedByUsername,
            ChannelEntity channel,
            Boolean trust,
            Boolean sign,
            String publicKey,
            String ip,
            String status,
            String firstName,
            String lastName,
            long expireTimeDuration
    ) {
        channel.setUpdatedBy(updatedByUsername);
        channel.setUpdatedAt(new Date());
        channel.setTrust(Boolean.TRUE.equals(trust) ? 1 : 0);
        channel.setSign(Boolean.TRUE.equals(sign) ? 1 : 0);
        channel.setPublicKey(publicKey);
        channel.setIp(ip);
        channel.setStatus(Integer.parseInt(status));
        channel.setFirstName(firstName);
        channel.setLastName(lastName);
        channel.setExpireTimeDuration(expireTimeDuration);
    }


    private Specification<ChannelEntity> getProfileEntityPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<ChannelEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        predicates.add(criteriaBuilder.isNull(root.get("endTime")));
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<ChannelEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }

    @Override
    public void logout(ChannelEntity channelEntity) {
        log.info("start logout channel({})", channelEntity.getUsername());
        channelEntity.setUpdatedBy(channelEntity.getUsername());
        channelEntity.setUpdatedAt(new Date());
        channelEntity.setAccessToken(null);
        channelEntity.setRefreshToken(null);
        channelRepository.save(channelEntity);
    }

    public static JSONObject getXrayOutboundFromURI(String uri) {
        try {
            // Parse the URI
            URI parsedUri = new URI(uri);

            // Extract protocol (e.g., vmess, vless, trojan)
            String protocol = parsedUri.getScheme();

            // Decode the configuration (usually Base64 encoded)
            String decodedConfig = new String(Base64.getDecoder().decode(parsedUri.getAuthority()));

            // Parse the decoded configuration into a JSON object
            JSONObject configJson = new JSONObject(decodedConfig);

            // Generate outbound configuration based on the protocol
            JSONObject outbound = new JSONObject();
            outbound.put("protocol", protocol);

            JSONObject settings = new JSONObject();
            JSONObject server = new JSONObject();
            server.put("address", configJson.getString("add")); // Server address
            server.put("port", configJson.getInt("port"));      // Server port

            if ("vmess".equals(protocol) || "vless".equals(protocol)) {
                settings.put("vnext", new JSONArray().put(new JSONObject()
                        .put("address", configJson.getString("add"))
                        .put("port", configJson.getInt("port"))
                        .put("users", new JSONArray().put(new JSONObject()
                                .put("id", configJson.getString("id"))
                                .put("alterId", configJson.optInt("aid", 0))
                                .put("security", configJson.optString("scy", "auto"))
                        ))
                ));
            } else if ("trojan".equals(protocol)) {
                settings.put("servers", new JSONArray().put(new JSONObject()
                        .put("address", configJson.getString("add"))
                        .put("port", configJson.getInt("port"))
                        .put("password", configJson.getString("password"))
                ));
            }

            outbound.put("settings", settings);

            // Add transport and TLS settings if available
            if (configJson.has("net")) {
                JSONObject streamSettings = new JSONObject();
                streamSettings.put("network", configJson.getString("net"));

                if (configJson.has("tls")) {
                    JSONObject tlsSettings = new JSONObject();
                    tlsSettings.put("security", configJson.getString("tls"));
                    streamSettings.put("security", tlsSettings);
                }

                outbound.put("streamSettings", streamSettings);
            }

            return outbound;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        System.out.println(getXrayOutboundFromURI("vless://25fbcc73-4b22-4bc3-9485-3c0fda6b33e4@WwW.spEEdTest.net:2082?type=ws&path=%2F%3Fed%3D1024&host=download.gamecentermahan.info&security=none#WS-Test-b8cu455v"));

    }
}
