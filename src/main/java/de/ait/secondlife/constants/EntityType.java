package de.ait.secondlife.constants;

import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;

public enum EntityType {
    OFFER("offer"),

    USER("user"),

    CATEGORY("category");

    private String type;

    EntityType(String type) {
        this.type = type;
    }

    public static EntityType get(String type) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getType().equalsIgnoreCase(type))
                return entityType;
        }
        throw new BadEntityTypeException(type);
    }

    public String getType() {
        return type;
    }
}
