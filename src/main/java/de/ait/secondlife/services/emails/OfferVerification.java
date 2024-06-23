package de.ait.secondlife.services.emails;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.services.interfaces.OfferService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferVerification extends TemplateService {

    private final OfferService offerService;

    @Value("${application.fe.host}")
    private String applicationHost;

    @Override
    public String getSubject() {
        return "New offer for verification";
    }

    @Override
    protected String getTemplateName() {
        return "offer_verification_mail.ftlh";
    }

    @Override
    protected Map<String, Object> getTemplateModel() throws AuthException {
        Map<String, Object> model = new HashMap<>();

        Long offerId = notification.getContextId();
        Offer offer = offerService.findById(offerId);
        model.put("link", composeEndpointUrl(offer));

        return model;
    }

    private String composeEndpointUrl(Offer offer) {
        return applicationHost + "/#/admin/offers/" + offer.getId();
    }
}
