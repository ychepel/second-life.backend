package de.ait.secondlife.domain.entity;

import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "authenticated_user_id")
    private Long authenticatedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_role")
    private Role receiverRole;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;
}
