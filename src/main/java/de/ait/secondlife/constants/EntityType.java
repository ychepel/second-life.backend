package de.ait.secondlife.constants;

import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;

public enum EntityType {
    OFFER("offer", 1),

    USER("user",2),

    CATEGORY("category",3);

    private String type;
    private int code;

    EntityType(String type, int code) {
        this.type = type;
        this.code = code;
    }

    public static EntityType get(String type) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getType().equalsIgnoreCase(type))
                return entityType;
        }
        throw new BadEntityTypeException(type);
    }

    public static EntityType get(int code) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getCode()==code)
                return entityType;
        }
        throw new BadEntityTypeException(code);
    }

    public String getType() {
        return type;
    }

    public int getCode() {
        return code;
    }
}
