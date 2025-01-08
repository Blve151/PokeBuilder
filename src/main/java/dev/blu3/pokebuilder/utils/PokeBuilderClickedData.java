package dev.blu3.pokebuilder.utils;


import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class PokeBuilderClickedData {
    public Pokemon currentPokemon;
    public Nature currentNature;
    public int currentLevel;
    public Stats currentStat;
    public int currentStatNum;
    public PokeBall currentBall;
    public Gender currentGender;
    public Ability currentAbility;
    public boolean isIncrease;
}
