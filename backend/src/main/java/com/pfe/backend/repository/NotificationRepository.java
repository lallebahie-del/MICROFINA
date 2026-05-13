package com.pfe.backend.repository;

import com.microfina.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserLogin(String userLogin, Pageable pageable);

    long countByUserLoginAndLuFalse(String userLogin);
}
