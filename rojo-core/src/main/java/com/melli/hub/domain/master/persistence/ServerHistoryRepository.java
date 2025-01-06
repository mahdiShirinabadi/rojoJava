package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ServerEntity;
import com.melli.hub.domain.master.entity.ServerHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ServerHistoryRepository extends CrudRepository<ServerHistoryEntity, Long> {
}
