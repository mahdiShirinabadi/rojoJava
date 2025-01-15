package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {
    ChannelEntity findByUsername(String username);
    ChannelEntity findByUsernameAndStatus(String email, int status);
    Page<ChannelEntity> findAll(Specification<ChannelEntity> spec, Pageable pageable);
}
