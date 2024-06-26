package de.ait.secondlife.services.emails;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.OfferService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferBlocked extends TemplateService {
    private final OfferService offerService;
    private final AuthService authService;

    @Value("${application.fe.host}")
    private String applicationHost;

    @Override
    protected String getTemplateName() {
        return  "offer_blocked_mail.ftlh";
    }

    @Override
    protected Map<String, Object> getTemplateModel() throws AuthException {
        Map<String, Object> model = new HashMap<>();

        User user = (User) authService.getAuthenticatedUser(Role.ROLE_USER, notification.getAuthenticatedUserId());
        model.put("name", user.getFullName());

        Long offerId = notification.getContextId();
        Offer offer = offerService.findById(offerId);
        model.put("link", composeEndpointUrl(offer));

        return model;
    }

    @Override
    public String getSubject() {
        return "Pay attention, you offer has been blocked by Administrator!";
    }

    private String composeEndpointUrl(Offer offer) {
        return applicationHost + "/#/offers/" + offer.getId();
    }
}
