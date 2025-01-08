package dev.blu3.pokebuilder.utils;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.item.interactive.MintItem;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.*;
import dev.blu3.pokebuilder.PokeBuilder;
import dev.blu3.pokebuilder.enums.BuilderAttribute;
import dev.blu3.pokebuilder.enums.BuilderSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static dev.blu3.pokebuilder.PokeBuilder.dataManager;
import static dev.blu3.pokebuilder.enums.BuilderAttribute.EVS;
import static dev.blu3.pokebuilder.enums.BuilderAttribute.IVS;
import static dev.blu3.pokebuilder.ui.PokeBuilderUI.*;
import static dev.blu3.pokebuilder.utils.PokeLoreUtils.getDesc;
import static dev.blu3.pokebuilder.utils.PokeLoreUtils.getPokeInfoName;
import static dev.blu3.pokebuilder.utils.Utils.*;

public class ButtonUtils {

    public static Button colouredPane(String paneName) {
        ItemStack pane = new ItemStack(PokeBuilder.stringPaneMap.getOrDefault(paneName, Items.WHITE_STAINED_GLASS_PANE));
        return GooeyButton.builder()
                .title("")
                .display(pane)
                .build();
    }

    public static List<Button> getPokeButtonList(ServerPlayer player, boolean isParty) {
        List<Button> buttonList = new ArrayList<>();
        List<Pokemon> pokeList = new ArrayList<>();

        if (isParty) {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            for (int i = 0; i < 6; i++) {
                pokeList.add(party.get(i));
            }
        } else {
            PCStore pc = null;
            try {
                pc = Cobblemon.INSTANCE.getStorage().getPC(player.getUUID());
            } catch (NoPokemonStoreException ignored) {
            }
            if (pc != null) {
                for (Pokemon pokemon : pc) {
                    pokeList.add(pokemon);
                }
            }
        }

        ItemStack itemStackPhoto;

        for (Pokemon pokeStack : pokeList) {
            if (pokeStack == null) {
                itemStackPhoto = removeTooltips(new ItemStack(CobblemonItems.POKE_BALL));
                Button nullPokes = GooeyButton.builder()
                        .display(itemStackPhoto)
                        .build();
                buttonList.add(nullPokes);
                itemStackPhoto.setHoverName(Component.literal(dataManager.getGuiText().emptySlot));
            } else if (listContainsPokemon(dataManager.getGeneral().blacklistedPokemon, pokeStack)) {
                itemStackPhoto = PokemonItem.from(pokeStack);
                itemStackPhoto.setHoverName(Component.literal(getPokeInfoName(pokeStack)));
                Button nullPokes = GooeyButton.builder()
                        .display(itemStackPhoto)
                        .lore(Collections.singletonList((dataManager.getGuiText().blacklistedSlot)))
                        .build();
                buttonList.add(nullPokes);
            } else {
                itemStackPhoto = PokemonItem.from(pokeStack);
                itemStackPhoto.setHoverName(Component.literal(getPokeInfoName(pokeStack)));
                Button pokes = GooeyButton.builder()
                        .display(itemStackPhoto)
                        .lore(getDesc(pokeStack, false))
                        .onClick(buttonAction -> {
                            PokeBuilderClickedData clickedData = getClickedData(player);
                            clickedData.currentPokemon = pokeStack;
                            setClickedData(player, clickedData);
                            UIManager.openUIForcefully(player, attributesUI(player));
                        })
                        .build();
                buttonList.add(pokes);
            }
        }
        return buttonList;
    }

    public static Button getPokeDescButton(Pokemon pokemon) {
        return GooeyButton.builder()
                .display(PokemonItem.from(pokemon))
                .title(getPokeInfoName(pokemon))
                .lore(getDesc(pokemon, false))
                .build();
    }

    public static Button getStatSelectionButton(PokeBuilderClickedData clickedData, Item item, String display, Stats statType, ServerPlayer player, BuilderAttribute attr) {
        return GooeyButton.builder()
                .display(new ItemStack(item))
                .title(display)
                .onClick(action -> {
                    clickedData.currentStat = statType;
                    setClickedData(player, clickedData);
                    switch (attr) {
                        case IVS -> UIManager.openUIForcefully(player, increaseDecreaseUI(player, IVS));
                        case EVS -> UIManager.openUIForcefully(player, increaseDecreaseUI(player, EVS));
                    }
                }).build();
    }

    public static List<Button> getMintButtons(Pokemon pokemon) {
        List<Button> mintList = new ArrayList<>();
        HashMap<String, HashMap<String, Integer>> priceMap = dataManager.getPrices().specificPrices;
        for (MintItem mintItem : mintItems) {
            String natureStr = new ItemStack(mintItem).getDisplayName().getString().replaceAll("[\\[\\]]", "").replace(" Mint", "");
            Nature nature = Natures.INSTANCE.getNature(natureStr.toLowerCase());
            if (nature == null) continue;
            if (nature.equals(pokemon.getNature())) {
                Button existing = GooeyButton.builder()
                        .display(new ItemStack(Blocks.BARRIER))
                        .title((dataManager.getGuiText().currentNature))
                        .onClick(action -> {
                        })
                        .build();
                mintList.add(existing);
            } else {
                Button mint = GooeyButton.builder()
                        .display(new ItemStack(mintItem))
                        .title("§a" + natureStr + " Nature")
                        .onClick(action -> {
                            ServerPlayer player = action.getPlayer();
                            PokeBuilderClickedData clickedData = getClickedData(player);
                            clickedData.currentNature = nature;
                            setClickedData(player, clickedData);
                            int cost;
                            if (priceMap.get("Natures").containsKey(natureStr)) {
                                cost = priceMap.get("Natures").get(natureStr);
                            } else {
                                cost = dataManager.getPrices().natureCost;
                            }
                            UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.NATURE, cost));
                        })
                        .build();
                mintList.add(mint);
            }
        }
        return mintList;
    }


    public static List<Button> getBallButtons(Pokemon pokemon) {
        List<Button> ballButtons = new ArrayList<>();
        HashMap<String, HashMap<String, Integer>> priceMap = dataManager.getPrices().specificPrices;


        for (PokeBallItem ballItem : ballItems) {
            PokeBall ball = ballItem.getPokeBall();
            ItemStack ballStack = removeTooltips(new ItemStack(ballItem));
            String ballName = ballStack.getDisplayName().getString().replaceAll("[\\[\\]]", "");
            GooeyButton.Builder buttonBuilder = GooeyButton.builder()
                    .title(("§d" + ballName));
            if (pokemon.getCaughtBall().equals(ball)) {
                buttonBuilder
                        .title((dataManager.getGuiText().currentBall))
                        .display(new ItemStack(Blocks.BARRIER))
                        .onClick(action -> {
                        })
                        .build();
            } else {
                buttonBuilder
                        .display(ballStack)
                        .onClick(action -> {
                            ServerPlayer player = action.getPlayer();
                            PokeBuilderClickedData clickedData = getClickedData(player);
                            clickedData.currentBall = ball;
                            setClickedData(player, clickedData);
                            int cost;
                            if (priceMap.get("Balls").containsKey(ballName)) {
                                cost = priceMap.get("Balls").get(ballName);
                            } else {
                                cost = dataManager.getPrices().ballCost;
                            }
                            UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.BALL, cost));

                        })
                        .build();
            }
            Button button = buttonBuilder.build();
            ballButtons.add(button);
        }
        return ballButtons;
    }

    public static void checkGenderButton(Pokemon pokemon, ServerPlayer player, GooeyButton.Builder maleBuilder, GooeyButton.Builder femaleBuilder) {

        if (pokemon.getGender().equals(Gender.MALE)) {
            maleBuilder.
                    lore(Collections.singletonList(dataManager.getGuiText().currentGender))
                    .onClick(action -> {
                    })
                    .build();
        }

        if (!pokemon.getGender().equals(Gender.MALE) && !pokemon.getGender().equals(Gender.GENDERLESS)) {
            maleBuilder
                    .onClick(action -> {
                        PokeBuilderClickedData clickedData = getClickedData(player);
                        clickedData.currentGender = Gender.MALE;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.GENDER, dataManager.getPrices().genderCost));
                    });
        }

        if (pokemon.getGender().equals(Gender.FEMALE)) {
            femaleBuilder.
                    lore(Collections.singletonList(dataManager.getGuiText().currentGender))
                    .onClick(action -> {
                    })
                    .build();
        }

        if (!pokemon.getGender().equals(Gender.FEMALE) && !pokemon.getGender().equals(Gender.GENDERLESS)) {
            femaleBuilder
                    .onClick(action -> {
                        PokeBuilderClickedData clickedData = getClickedData(player);
                        clickedData.currentGender = Gender.FEMALE;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.GENDER, dataManager.getPrices().genderCost));
                    });
        }

        if (pokemon.getGender().equals(Gender.GENDERLESS)) {
            femaleBuilder.
                    lore(Collections.singletonList(dataManager.getGuiText().genderChange))
                    .onClick(action -> {
                    })
                    .build();

            maleBuilder.
                    lore(Collections.singletonList(dataManager.getGuiText().genderChange))
                    .onClick(action -> {
                    })
                    .build();
        }
    }

    public static void checkAbilityButton(Pokemon pokemon, ServerPlayer player, GooeyButton.Builder ab1Builder, GooeyButton.Builder ab2Builder, GooeyButton.Builder ab3Builder) {
        List<Ability> abilities = new ArrayList<>();
        pokemon.getForm().getAbilities().getMapping().values().forEach(potentialAbilities -> {
            for (PotentialAbility potentialAbility : potentialAbilities) {
                Ability ability = new Ability(potentialAbility.getTemplate(), true);
                if (!abilities.contains(ability)) {
                    abilities.add(ability);
                }
            }
        });

        int count = 0;

        for (Ability ability : abilities) {
            Pokemon clone = pokemon.clone(true, true);
            clone.updateAbility(ability);
            final boolean ha = Utils.isHA(clone);
            switch (count) {
                case 0 -> {
                    buildAbilityOption(ab1Builder, ability, ha, player);
                    checkExistingAbility(ab1Builder, ability, pokemon);
                }
                case 1 -> {
                    buildAbilityOption(ab2Builder, ability, ha, player);
                    checkExistingAbility(ab2Builder, ability, pokemon);
                }
                case 2 -> {
                    buildAbilityOption(ab3Builder, ability, ha, player);
                    checkExistingAbility(ab3Builder, ability, pokemon);
                }
            }
            count++;
        }
        count = 0;

        for (Ability ability : abilities) {
            switch (count) {
                case 0 -> checkExistingAbility(ab1Builder, ability, pokemon);
                case 1 -> checkExistingAbility(ab2Builder, ability, pokemon);
                case 2 -> checkExistingAbility(ab3Builder, ability, pokemon);
            }
            count++;
        }
    }

    private static void checkExistingAbility(GooeyButton.Builder builder, Ability ability, Pokemon pokemon) {
        if (pokemon.getAbility().getTemplate().getName().equals(ability.getTemplate().getName())) {
            builder
                    .display(new ItemStack(CobblemonItems.ABILITY_CAPSULE))
                    .lore(Collections.singletonList(("§eCurrent ability.")))
                    .onClick(action -> {
                    })
                    .build();
        }
    }

    private static void buildAbilityOption(GooeyButton.Builder builder, Ability ability, boolean ha, ServerPlayer player) {
        builder
                .display(new ItemStack(CobblemonItems.ABILITY_CAPSULE))
                .lore(Collections.singletonList((ha ? "§b" + Utils.parseLang(ability.getDisplayName()) + " §7(§6HA§7)" : "§b" + Utils.parseLang(ability.getDisplayName()))))
                .onClick(action -> {
                    PokeBuilderClickedData clickedData = getClickedData(player);
                    clickedData.currentAbility = ability;
                    setClickedData(player, clickedData);
                    UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.ABILITY, ha ? dataManager.getPrices().hiddenAbilityCost : dataManager.getPrices().abilityCost));
                })
                .build();
    }

    public static void checkIncButton(ServerPlayer player, GooeyButton.Builder increaseBuilder, BuilderAttribute attr) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;
        Stats stat = clickedData.currentStat;

        switch (attr) {
            case IVS:
                if (isMaxIVs(pokemon)) {
                    increaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList((dataManager.getGuiText().isMaxIVs)));
                } else if (pokemon.getIvs().get(stat) == 31) {
                    increaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList((dataManager.getGuiText().isMaxIVStat)));
                }
                break;
            case EVS:
                if (isMaxEVs(pokemon)) {
                    increaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList((dataManager.getGuiText().isMaxEVs)));
                } else if (pokemon.getEvs().get(stat) == 252) {
                    increaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList((dataManager.getGuiText().isMaxEVStat)));
                }
                break;
            default:
                break;
        }


    }

    public static void checkDecButton(ServerPlayer player, GooeyButton.Builder decreaseBuilder, BuilderAttribute attr) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Stats stat = clickedData.currentStat;
        Pokemon pokemon = clickedData.currentPokemon;
        IVs ivs = pokemon.getIvs();

        switch (attr) {
            case IVS:
                if (isMinIVs(pokemon)) {
                    decreaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cThis Pokemon can't lower its IVs!")));
                } else if (ivs.get(stat) == 0) {
                    decreaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cThis stat can't be lowered!")));
                }
                break;
            case EVS:
                if (isMinEVs(pokemon)) {
                    decreaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cThis Pokemon can't lower its EVs!")));
                } else if (pokemon.getEvs().get(stat) == 0) {
                    decreaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cThis stat can't be lowered!")));
                }
                break;
            default:
                break;
        }
    }

    public static void checkIVStatButton(ServerPlayer player, BuilderSelection selection, GooeyButton.Builder oneStatBuilder, GooeyButton.Builder tenStatBuilder, GooeyButton.Builder twentyFiveStatBuilder) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Stats currentStat = clickedData.currentStat;
        Pokemon pokemon = clickedData.currentPokemon;
        IVs ivs = pokemon.getIvs();
        oneStatBuilder.display(new ItemStack(CobblemonItems.IRON));
        tenStatBuilder.display(new ItemStack(CobblemonItems.IRON));
        twentyFiveStatBuilder.display(new ItemStack(CobblemonItems.IRON));
        switch (selection) {
            case INCREASE -> {
                if (ivs.get(currentStat) + 1 > 31) {
                    oneStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max IV.")));

                }
                if (ivs.get(currentStat) + 10 > 31) {
                    tenStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max IV.")));

                }
                if (ivs.get(currentStat) + 25 > 31) {
                    twentyFiveStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max IV.")));

                }
            }
            case DECREASE -> {
                if (ivs.get(currentStat) - 1 < 0) {
                    oneStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative IVs.")));

                }
                if (ivs.get(currentStat) - 10 < 0) {
                    tenStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative IVs.")));

                }
                if (ivs.get(currentStat) - 25 < 0) {
                    twentyFiveStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative IVs.")));
                }
            }
            default -> {
            }
        }
    }

    public static void checkEVStatButton(ServerPlayer player, BuilderSelection selection, GooeyButton.Builder oneStatBuilder, GooeyButton.Builder tenStatBuilder, GooeyButton.Builder twentyFiveStatBuilder) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Stats currentStat = clickedData.currentStat;
        Pokemon pokemon = clickedData.currentPokemon;
        EVs evs = pokemon.getEvs();
        oneStatBuilder.display(new ItemStack(CobblemonItems.CALCIUM));
        tenStatBuilder.display(new ItemStack(CobblemonItems.CALCIUM));
        twentyFiveStatBuilder.display(new ItemStack(CobblemonItems.CALCIUM));

        switch (selection) {
            case INCREASE -> {
                if (evs.get(currentStat) + 1 > 252) {
                    oneStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max EVs.")));

                }
                if (evs.get(currentStat) + 10 > 252) {
                    tenStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max EVs.")));

                }
                if (evs.get(currentStat) + 25 > 252) {
                    twentyFiveStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches over max EVs.")));

                }
            }
            case DECREASE -> {
                if (evs.get(currentStat) - 1 < 0) {
                    oneStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative EVs.")));

                }
                if (evs.get(currentStat) - 10 < 0) {
                    tenStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative EVs.")));

                }
                if (evs.get(currentStat) - 25 < 0) {
                    twentyFiveStatBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            })
                            .lore(Collections.singletonList(("§cResult reaches negative EVs.")));
                }
            }
            default -> {
            }
        }
    }

}
