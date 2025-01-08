package dev.blu3.pokebuilder.enums;

public enum BuilderAttribute {
    SHINY("Shiny"),
    NATURE("Nature"),
    LEVEL("Level"),
    BALL("Ball"),
    IVS("IVs"),
    MAX_IVS("Max IVs"),

    EVS("EVs"),
    GENDER("Gender"),
    ABILITY("Ability");

    String name;

    BuilderAttribute(String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }
}
