package de.ait.secondlife.constants;

import de.ait.secondlife.exception_handling.exceptions.not_found_exception.BadEntityTypeException;

public enum EntityTypeWithImages {
    OFFER("offer",5),

    USER("user",1),

    CATEGORY("category",1);

    private String type;
    private int maxCountOfImages;


    EntityTypeWithImages(String type, int maxCountOfImages) {
        this.type = type;
        this.maxCountOfImages = maxCountOfImages;
    }

    public static EntityTypeWithImages get(String type) {
        for (EntityTypeWithImages entityType : EntityTypeWithImages.values()) {
            if (entityType.getType().equalsIgnoreCase(type))
                return entityType;
        }
        throw new BadEntityTypeException(type);
    }

    public String getType() {
        return type;
    }

    public int getMaxCountOfImages() {
        return maxCountOfImages;
    }
}
