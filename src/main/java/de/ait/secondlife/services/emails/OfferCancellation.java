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
public class OfferCancellation extends TemplateService {
    private final OfferService offerService;
    private final AuthService authService;

    @Value("${application.fe.host}")
    private String applicationHost;

    @Override
    protected String getTemplateName() {
        return  "offer_canceled_mail.ftlh";
    }

    @Override
    protected Map<String, Object> getTemplateModel() throws AuthException {
        Map<String, Object> model = new HashMap<>();

        User user = (User) authService.getAuthenticatedUser(Role.ROLE_USER, notification.getAuthenticatedUserId());
        model.put("name", user.getUsername());

        Long offerId = notification.getContextId();
        Offer offer = offerService.findById(offerId);
        model.put("link", composeEndpointUrl(offer));


        model.put("active-offers", composeEndpointUrl());

        return model;
    }

    @Override
    public String getSubject() {
        return "Due to some reasons, the owner cancelled the offer!";
    }

    private String composeEndpointUrl(Offer offer) {
        return applicationHost + "/#/offers/" + offer.getId();
    }
    private String composeEndpointUrl() {
        return applicationHost + "/#/offers/all";
    }
}
