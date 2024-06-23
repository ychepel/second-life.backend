package de.ait.secondlife.services.emails;

import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Registration extends TemplateService {

    private final ConfirmationService confirmationService;
    private final AuthService authService;

    @Override
    public String getSubject() {
        return "Registration: We need you to confirm your e-mail address";
    }

    @Value("${application.be.host}")
    private String apiHost;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    protected String getTemplateName() {
        return "confirmation_registration_mail.ftlh";
    }

    @Override
    protected Map<String, Object> getTemplateModel() throws AuthException {
        Map<String, Object> model = new HashMap<>();

        User user = (User) authService.getAuthenticatedUser(Role.ROLE_USER, notification.getAuthenticatedUserId());
        model.put("name", user.getFullName());

        String code = confirmationService.generateConfirmationCode(user);
        model.put("link", composeEndpointUrl(user) + code);

        return model;
    }

    private String composeEndpointUrl(User user) {
        return apiHost + contextPath + "/v1/users/" + user.getId() + "/set-active?code=";
    }
}
