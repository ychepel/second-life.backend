package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllBySentAtIsNull();
}
