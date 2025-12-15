package com.java.sms.repository;

import com.java.sms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        select distinct u
        from User u
        join u.roles r
        where lower(r.name) = lower(:roleName)
    """)
    List<User> findUsersByRoles(@Param("roleName") String roleName);

}
