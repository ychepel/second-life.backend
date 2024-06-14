package de.ait.secondlife.domain.entity;

import de.ait.secondlife.constants.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull(message = "User Id cannot be null")
    private User user;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    @NotBlank(message = "Notification_type name cannot be empty")
    private NotificationType notificationType;
}
