package com.juzi.user.repo;

import com.juzi.user.pojo.po.OAuth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author codejuzi
 */
@Repository
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Integer> {

    OAuth2Client findByClientId(String clientId);

    /**
     * 根据clientId修改client secret
     *
     * @param secret   new client secret
     * @param clientId client id
     */
    @Modifying
    @Transactional
    @Query(
            value = "update oauth_client_details set client_secret = ?1 where client_id = ?2",
            nativeQuery = true)
    void updateSecretByClientId(String secret, String clientId);
}
