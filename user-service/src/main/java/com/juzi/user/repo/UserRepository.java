package com.juzi.user.repo;

import com.juzi.user.pojo.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author codejuzi
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserName(String username);

    User findByUserPhone(String phoneNumber);

    @Modifying
    @Transactional
    @Query(
            value = "update user set user_phone = ?1 where id = ?2",
            nativeQuery = true)
    void updateUserPhoneById(String phoneNumber, Integer id);

}
