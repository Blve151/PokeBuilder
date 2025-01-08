package dev.blu3.pokebuilder.config.types;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class PokeBuilderGeneralConfig {
    public boolean useLegacyTokens = true;

    public boolean allowShinyUnset = false;
    public List<String> costMultipliedPokemon = new ArrayList<>();
    public List<String> blacklistedPokemon = Lists.newArrayList("legend", "ultrabeast");
    public List<String> shinyBlacklistedPokemon = new ArrayList<>();
    public List<String> natureBlacklistedPokemon = new ArrayList<>();
    public List<String> levelBlacklistedPokemon = new ArrayList<>();
    public List<String> ballBlacklistedPokemon = new ArrayList<>();
    public List<String> ivBlacklistedPokemon = new ArrayList<>();
    public List<String> evBlacklistedPokemon = new ArrayList<>();
    public List<String> genderBlacklistedPokemon = new ArrayList<>();
    public List<String> abilityBlacklistedPokemon = new ArrayList<>();
//    public List<String> breedlockBlacklistedPokemon = new ArrayList<>();
}
