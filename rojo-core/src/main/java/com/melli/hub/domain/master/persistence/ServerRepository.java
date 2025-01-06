package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ServerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ServerRepository extends CrudRepository<ServerEntity, Long> {
    List<ServerEntity> findAllByStatusOrderByCountUsedAsc(String status);
}
