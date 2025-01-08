package dev.blu3.pokebuilder.utils;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cobblemon.mod.common.api.pokemon.Natures.INSTANCE;
import static dev.blu3.pokebuilder.utils.Utils.baseStats;
import static dev.blu3.pokebuilder.utils.Utils.isHA;


public class PokeLoreUtils {
    private static final List<String> statColours = Lists.newArrayList("§a", "§c", "§6", "§d", "§2", "§b");

    public static List<String> getDesc(Pokemon pokemon, boolean includeInfoName) {
        List<String> lore = new ArrayList<>();


        if(includeInfoName){
            lore.add(getPokeInfoName(pokemon));
        }

        lore.add("§3Ability: §7" + getAbility(pokemon));
        lore.addAll(getNature(pokemon));
        lore.add("§3Gender: §7" + getGender(pokemon, false));
        lore.add("§3Happiness: §7" + pokemon.getFriendship());
        lore.add("§3Ball: §7" + new ItemStack(pokemon.getCaughtBall().item()).getDisplayName().getString().replaceAll("[\\[\\]]", ""));


        lore.add("§3Held Item: §7" + (pokemon.heldItem().isEmpty() ? "None." : pokemon.heldItem().getDisplayName().getString()));
        lore.add("§3Original Trainer: §7" + pokemon.getOriginalTrainerName());

        lore.add("");
        lore.add("§3Moves: §7" + getMoves(pokemon));

        lore.add("");
        lore.add("§3IVs: §7" + getIVs(false, pokemon));
        lore.add(getIVs(true, pokemon));
        lore.add("");
        lore.add("§3EVs: §7" + getEVs(false, pokemon));
        lore.add(getEVs(true, pokemon));
        return lore;
    }

    public static String getPokeInfoName(Pokemon pokemon) {
        String species = pokemon.getSpecies().getName();
        String name = "§e" + species + " " + getGender(pokemon, true) + " §9Lvl: " + pokemon.getLevel();
        if (pokemon.getShiny()) name += " §7(§6Shiny§7)";
        return name;
    }

    private static String getGender(Pokemon pokemon, boolean symbolOnly) {
        return switch (pokemon.getGender()) {
            case MALE -> symbolOnly ? "§b♂" : "§bMale ♂";
            case FEMALE -> symbolOnly ? "§d♀" : "§dFemale ♀";
            default -> symbolOnly ? "§8⚤" : "§8Genderless ⚤";
        };
    }

    private static String getAbility(Pokemon pokemon) {
        String abStr = Utils.parseLang(pokemon.getAbility().getDisplayName());
        return isHA(pokemon) ? abStr + " §7(§6HA§7)" : abStr;
    }

    private static String getMoves(Pokemon pokemon) {
        StringBuilder movesBuilder = new StringBuilder();
        for (Move move : pokemon.getMoveSet().getMoves()) {
            String moveName = move.getDisplayName().getString();
            if (!moveName.equals("")) {
                movesBuilder.append("§e").append(moveName).append("§3 / ");
            } else {
                movesBuilder.append("§e" + "§cNone §3/");
            }
        }
        String moves = movesBuilder.toString();
        moves = moves.substring(0, moves.length() - 2);
        return moves;
    }


    private static String getStatString (Stats stat){
        return switch (stat) {
            case HP -> "HP";
            case ATTACK -> "Atk";
            case DEFENCE -> "Def";
            case SPECIAL_ATTACK -> "Sp.Atk";
            case SPECIAL_DEFENCE -> "Sp.Def";
            case SPEED -> "Spe";
            default -> "";
        };
    }


    private static String getIVs(boolean detail, Pokemon pokemon) {
        String ivStr = "§3-";
        IVs ivs = pokemon.getIvs();
        if (!detail) {
            int totalIVs = 0;
            for (Map.Entry<? extends Stat, ? extends Integer> iv : ivs) {
                totalIVs += iv.getValue();
            }
            String ivPercent = (Math.floor((double) totalIVs / 186 * 10000d) / 100d) + "";
            ivStr = totalIVs + "/186 §3(§9" + ivPercent + "%" + "§3)";
        } else {
            for (Stats baseStat : baseStats) {
                ivStr += " " + statColours.get(baseStats.indexOf(baseStat)) + ivs.get(baseStat) + " " + getStatString(baseStat);
                ivStr += " §3/ ";
            }
            ivStr = ivStr.substring(0, ivStr.length() - 5);
        }
        return ivStr;
    }

    private static String getEVs(boolean detail, Pokemon pokemon) {
        String evStr = "§3-";
        EVs evs = pokemon.getEvs();
        if (!detail) {
            int totalEVs = 0;
            for (Map.Entry<? extends Stat, ? extends Integer> ev : evs) {
                totalEVs += ev.getValue();
            }
            String evPercent = (Math.floor((double) totalEVs / 510 * 10000d) / 100d) + "";
            evStr = totalEVs + "/510 §3(§9" + evPercent + "%" + "§3)";
        } else {
            for (Stats baseStat : baseStats) {
                evStr += " " + statColours.get(baseStats.indexOf(baseStat)) + evs.get(baseStat) + " " + getStatString(baseStat);
                evStr += " §3/ ";
            }
            evStr = evStr.substring(0, evStr.length() - 5);
        }
        return evStr;
    }

    private static String getNatureString (Nature natureType){
        String nature = Utils.parseLang(natureType.getDisplayName());
        if (INSTANCE.getLONELY().equals(natureType)) {
            nature += " §3(§a+Atk §3: §c-Def§3)";
        } else if (INSTANCE.getBRAVE().equals(natureType)) {
            nature += " §3(§a+Atk §3: §c-Spe§3)";
        } else if (INSTANCE.getADAMANT().equals(natureType)) {
            nature += " §3(§a+Atk §3: §c-Sp.Atk§3)";
        } else if (INSTANCE.getNAUGHTY().equals(natureType)) {
            nature += " §3(§a+Atk §3: §c-Sp.Def§3)";
        } else if (INSTANCE.getBOLD().equals(natureType)) {
            nature += " §3(§a+Def §3: §c-Atk§3)";
        } else if (INSTANCE.getRELAXED().equals(natureType)) {
            nature += " §3(§a+Def §3: §c-Spe§3)";
        } else if (INSTANCE.getIMPISH().equals(natureType)) {
            nature += " §3(§a+Def §3: §c-Sp.Atk§3)";
        } else if (INSTANCE.getLAX().equals(natureType)) {
            nature += " §3(§a+Def §3: §c-Sp.Def§3)";
        } else if (INSTANCE.getTIMID().equals(natureType)) {
            nature += " §3(§a+Spe §3: §c-Atk§3)";
        } else if (INSTANCE.getHASTY().equals(natureType)) {
            nature += " §3(§a+Spe §3: §c-Def§3)";
        } else if (INSTANCE.getJOLLY().equals(natureType)) {
            nature += " §3(§a+Spe §3: §c-Sp.Atk§3)";
        } else if (INSTANCE.getNAIVE().equals(natureType)) {
            nature += " §3(§a+Spe §3: §c-Sp.Def§3)";
        } else if (INSTANCE.getMODEST().equals(natureType)) {
            nature += " §3(§a+Sp.Atk §3: §c-Atk§3)";
        } else if (INSTANCE.getMILD().equals(natureType)) {
            nature += " §3(§a+Sp.Atk §3: §c-Def§3)";
        } else if (INSTANCE.getQUIET().equals(natureType)) {
            nature += " §3(§a+Sp.Atk §3: §c-Spe§3)";
        } else if (INSTANCE.getRASH().equals(natureType)) {
            nature += " §3(§a+Sp.Atk §3: §c-Sp.Def§3)";
        } else if (INSTANCE.getCALM().equals(natureType)) {
            nature += " §3(§a+Sp.Def §3: §c-Atk§3)";
        } else if (INSTANCE.getGENTLE().equals(natureType)) {
            nature += " §3(§a+Sp.Def §3: §c-Def§3)";
        } else if (INSTANCE.getSASSY().equals(natureType)) {
            nature += " §3(§a+Sp.Def §3: §c-Spe§3)";
        } else if (INSTANCE.getCAREFUL().equals(natureType)) {
            nature += " §3(§a+Sp.Def §3: §c-Sp.Atk§3)";
        } else {
            nature += " §3(§7Neutral§3)";
        }
        return nature;
    }

    private static List<String> getNature(Pokemon pokemon) {
        List<String> natureStrings = new ArrayList<>();
        natureStrings.add("§3Nature: §7" + getNatureString(pokemon.getNature()));
        if(pokemon.getMintedNature() != null){
            natureStrings.add("§3Nature §e(Mint)§3: §7" + getNatureString(pokemon.getMintedNature()));
        }
        return natureStrings;
    }


}
