package de.ait.secondlife.constants;

import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;

public enum EntityTypeWithImgs {
    OFFER("offer",5),

    USER("user",1),

    CATEGORY("category",1);

    private String type;
    private int maxCountOfImgs;


    EntityTypeWithImgs(String type, int maxCountOfImgs) {
        this.type = type;
        this.maxCountOfImgs = maxCountOfImgs;
    }

    public static EntityTypeWithImgs get(String type) {
        for (EntityTypeWithImgs entityType : EntityTypeWithImgs.values()) {
            if (entityType.getType().equalsIgnoreCase(type))
                return entityType;
        }
        throw new BadEntityTypeException(type);
    }

    public String getType() {
        return type;
    }

    public int getMaxCountOfImgs() {
        return maxCountOfImgs;
    }
}
