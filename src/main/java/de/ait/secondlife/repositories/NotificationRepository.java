package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Notification findFirstBySentAtIsNull();
}
