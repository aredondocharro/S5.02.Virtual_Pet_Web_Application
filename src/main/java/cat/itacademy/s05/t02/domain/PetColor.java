package cat.itacademy.s05.t02.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PetColor {
    PINK, BLACK, WHITE, ORANGE;


    @JsonCreator
    public static PetColor from(String value) {
        return PetColor.valueOf(value.trim().toUpperCase());
    }


    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}