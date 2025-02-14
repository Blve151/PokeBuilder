package dev.blu3.pokebuilder.utils;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.text.Text;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.item.interactive.MintItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.google.common.collect.Lists;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.blu3.pokebuilder.PokeBuilder.dataManager;

public class Utils {

    private static final String regex = "&(?=[0-9a-ff-or])";
    private static final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    public static String regex(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            line = line.replaceAll(regex, "§");
        }
        return line;
    }

    public static final List<Stats> baseStats = Lists.newArrayList(Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED);
    public static MintItem[] mintItems = {CobblemonItems.ADAMANT_MINT, CobblemonItems.BOLD_MINT, CobblemonItems.BRAVE_MINT, CobblemonItems.CALM_MINT, CobblemonItems.CAREFUL_MINT, CobblemonItems.GENTLE_MINT, CobblemonItems.HASTY_MINT, CobblemonItems.IMPISH_MINT, CobblemonItems.JOLLY_MINT, CobblemonItems.LAX_MINT, CobblemonItems.LONELY_MINT, CobblemonItems.MILD_MINT, CobblemonItems.MODEST_MINT, CobblemonItems.NAIVE_MINT, CobblemonItems.NAUGHTY_MINT, CobblemonItems.QUIET_MINT, CobblemonItems.RASH_MINT, CobblemonItems.RELAXED_MINT, CobblemonItems.SASSY_MINT, CobblemonItems.SERIOUS_MINT, CobblemonItems.TIMID_MINT};
    public static PokeBallItem[] ballItems = {CobblemonItems.POKE_BALL, CobblemonItems.PREMIER_BALL, CobblemonItems.QUICK_BALL, CobblemonItems.REPEAT_BALL, CobblemonItems.ROSEATE_BALL, CobblemonItems.SAFARI_BALL, CobblemonItems.SLATE_BALL, CobblemonItems.SPORT_BALL, CobblemonItems.TIMER_BALL, CobblemonItems.ULTRA_BALL, CobblemonItems.VERDANT_BALL, CobblemonItems.ANCIENT_AZURE_BALL, CobblemonItems.ANCIENT_CITRINE_BALL, CobblemonItems.ANCIENT_FEATHER_BALL, CobblemonItems.ANCIENT_GIGATON_BALL, CobblemonItems.ANCIENT_GREAT_BALL, CobblemonItems.ANCIENT_HEAVY_BALL, CobblemonItems.ANCIENT_IVORY_BALL, CobblemonItems.ANCIENT_JET_BALL, CobblemonItems.ANCIENT_LEADEN_BALL, CobblemonItems.ANCIENT_ORIGIN_BALL, CobblemonItems.ANCIENT_POKE_BALL, CobblemonItems.ANCIENT_ROSEATE_BALL, CobblemonItems.ANCIENT_SLATE_BALL, CobblemonItems.ANCIENT_ULTRA_BALL, CobblemonItems.ANCIENT_VERDANT_BALL, CobblemonItems.ANCIENT_WING_BALL, CobblemonItems.AZURE_BALL, CobblemonItems.BEAST_BALL, CobblemonItems.CHERISH_BALL, CobblemonItems.CITRINE_BALL, CobblemonItems.DIVE_BALL, CobblemonItems.DREAM_BALL, CobblemonItems.DUSK_BALL, CobblemonItems.DUSK_BALL, CobblemonItems.FAST_BALL, CobblemonItems.FRIEND_BALL, CobblemonItems.GREAT_BALL, CobblemonItems.HEAL_BALL, CobblemonItems.HEAVY_BALL, CobblemonItems.LEVEL_BALL, CobblemonItems.LOVE_BALL, CobblemonItems.LURE_BALL, CobblemonItems.LUXURY_BALL, CobblemonItems.MASTER_BALL, CobblemonItems.MOON_BALL, CobblemonItems.NEST_BALL, CobblemonItems.NET_BALL, CobblemonItems.PARK_BALL};

    public static HashMap<String, HashMap<String, Integer>> getNewSpecificPrices() {
        HashMap<String, HashMap<String, Integer>> pricesMap = new HashMap<>();
        HashMap<String, Integer> natures = new HashMap<>();
        HashMap<String, Integer> balls = new HashMap<>();

        natures.put("Adamant", 60);
        natures.put("Jolly", 60);
        natures.put("Timid", 60);

        balls.put("Cherish Ball", 999999);
        balls.put("Master Ball", 35);
        balls.put("Park Ball", 35);

        pricesMap.put("Natures", natures);
        pricesMap.put("Balls", balls);
        return pricesMap;
    }
    public static boolean isHA(Pokemon pokemon) {
        for (PotentialAbility spAb : pokemon.getForm().getAbilities()) {
            if(pokemon.getAbility().getTemplate().getName().equals(spAb.getTemplate().getName())
                    && spAb.getType() == HiddenAbilityType.INSTANCE) return true;
        }
        return false;
    }

    public static String parseLang(String str) {
        return Text.Companion.resolveComponent$common(str).getString();
    }

    private static final HashMap<UUID, PokeBuilderClickedData> clickedDataMap = new HashMap<>();

    public static PokeBuilderClickedData getClickedData(ServerPlayer player) {
        PokeBuilderClickedData clickedData = clickedDataMap.get(player.getUUID());
        if (clickedData == null) {
            clickedData = new PokeBuilderClickedData();
            clickedDataMap.put(player.getUUID(), clickedData);
            return clickedData;
        }
        return clickedData;
    }

    public static boolean listContainsPokemon(List<String> list, Pokemon pokemon) {
        return list.contains(pokemon.getSpecies().getName().toLowerCase())
                || list.contains("legend") && pokemon.isLegendary() ||
                list.contains("ultrabeast") && pokemon.isUltraBeast()
                || list.contains("mythical") && pokemon.isMythical();
    }

    public static ItemStack removeTooltips(ItemStack stack) {
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        return stack;
    }

    public static ItemStack setStackName (ItemStack stack, String name){
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(regex(name)));
        return stack;
    }

    public static boolean canChangeGender(Pokemon pokemon) {
        return !(pokemon.getSpecies().getMaleRatio() == 0) && !pokemon.getGender().equals(Gender.GENDERLESS);
    }
    public static boolean isMultipliedCost(Pokemon pokemon) {
        List<String> multipliedList = dataManager.getGeneral().costMultipliedPokemon;
        return listContainsPokemon(multipliedList, pokemon);
    }

    public static boolean canAfford(ServerPlayer player, int cost) {
        int tokens = dataManager.getTokens(player).intValue();
        if (tokens < cost) {
            player.sendSystemMessage(Component.literal(dataManager.getMessages().cantAfford));
            player.sendSystemMessage(Component.literal(dataManager.getMessages().currentBal.replace("<tokens>", tokens + "")));
            UIManager.closeUI(player);
            return false;
        } else {
            dataManager.removeTokens(player, cost);
            tokens = tokens - cost;
            player.sendSystemMessage(Component.literal(dataManager.getMessages().currentBal.replace("<tokens>", tokens + "")));
            return true;
        }
    }

    public static void setClickedData(ServerPlayer player, PokeBuilderClickedData clickedData) {
        clickedDataMap.put(player.getUUID(), clickedData);
    }

    public static boolean isMaxIVs(Pokemon pokemon) {
        int ivCount = 0;
        for (Stats baseStat : baseStats) {
            ivCount += pokemon.getIvs().get(baseStat);
        }
        return ivCount == 186;
    }

    public static boolean isMaxEVs(Pokemon pokemon) {
        int evCount = 0;
        for (Stats baseStat : baseStats) {
            evCount += pokemon.getEvs().get(baseStat);
        }
        return evCount == 510;
    }

    public static boolean isMinIVs(Pokemon pokemon) {
        int ivCount = 0;
        for (Stats baseStat : baseStats) {
            ivCount += pokemon.getIvs().get(baseStat);
            if(ivCount > 0) return false;
        }
        return ivCount == 0;
    }

    public static boolean isMinEVs(Pokemon pokemon) {
        int evCount = 0;
        for (Stats baseStat : baseStats) {
            evCount += pokemon.getIvs().get(baseStat);
            if(evCount > 0) return false;
        }
        return evCount == 0;
    }

    public static List<Component> convertStringListToComponentList(List<String> list) {
        return list.stream()
                .map(Component::literal)
                .collect(Collectors.toList());
    }

    public static void updateBuilderTitle(GooeyButton.Builder builder, String title) {
        ItemStack displayStack = builder.build().getDisplay();
        displayStack.set(DataComponents.CUSTOM_NAME, Component.literal(title));
        builder.display(displayStack);
    }
}
