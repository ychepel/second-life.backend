package de.ait.secondlife.security.services;

import de.ait.secondlife.security.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {

    @Override
    public Role convert(String source) {
        String enumName = "ROLE_" + source.toUpperCase();
        return Role.valueOf(enumName);
    }
}
