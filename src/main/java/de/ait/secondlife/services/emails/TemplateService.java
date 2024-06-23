package de.ait.secondlife.services.emails;

import de.ait.secondlife.domain.entity.Notification;
import de.ait.secondlife.exception_handling.exceptions.EmailTemplateException;
import de.ait.secondlife.services.interfaces.EmailTemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Setter;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Map;

@Setter
public abstract class TemplateService implements EmailTemplateService {

    protected Configuration mailConfiguration;
    protected Notification notification;

    abstract protected String getTemplateName();
    abstract protected Map<String, Object> getTemplateModel() throws Exception;

    @Override
    public String getBody() throws EmailTemplateException {
        try {
            Template template = mailConfiguration.getTemplate(getTemplateName());
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, getTemplateModel());
        } catch (Exception e) {
            throw new EmailTemplateException(e);
        }
    }
}
