package dev.blu3.pokebuilder.utils;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
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
        pane.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);

        return GooeyButton.builder().display(pane).build();
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
            PCStore pc;
            pc = Cobblemon.INSTANCE.getStorage().getPC(player);
            for (Pokemon pokemon : pc) {
                pokeList.add(pokemon);
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
                itemStackPhoto.set(DataComponents.CUSTOM_NAME, Component.literal(dataManager.getGuiText().emptySlot));
            } else if (listContainsPokemon(dataManager.getGeneral().blacklistedPokemon, pokeStack)) {
                itemStackPhoto = PokemonItem.from(pokeStack);
                itemStackPhoto.set(DataComponents.CUSTOM_NAME, Component.literal(getPokeInfoName(pokeStack)));
                itemStackPhoto.set(DataComponents.LORE, new ItemLore(Collections.singletonList((Component.literal(dataManager.getGuiText().blacklistedSlot)))));
                Button nullPokes = GooeyButton.builder()
                        .display(itemStackPhoto)
                        .build();
                buttonList.add(nullPokes);
            } else {
                itemStackPhoto = PokemonItem.from(pokeStack);
                itemStackPhoto.set(DataComponents.CUSTOM_NAME, Component.literal(getPokeInfoName(pokeStack)));
                itemStackPhoto.set(DataComponents.LORE,
                        new ItemLore(convertStringListToComponentList(getDesc(pokeStack, false))));
                Button pokes = GooeyButton.builder()
                        .display(itemStackPhoto)
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
        ItemStack itemStack = PokemonItem.from(pokemon);
        itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(getPokeInfoName(pokemon)));
        itemStack.set(DataComponents.LORE,
                new ItemLore(convertStringListToComponentList(getDesc(pokemon,
                        false))));
        return GooeyButton.builder()
                .display(itemStack)
                .build();
    }

    public static Button getStatSelectionButton(PokeBuilderClickedData clickedData, Item item, String display, Stats statType, ServerPlayer player, BuilderAttribute attr) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(display));
        itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return GooeyButton.builder()
                .display(itemStack)
                //.title(display)
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
                ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                itemStack.set(DataComponents.CUSTOM_NAME, Component.literal((dataManager.getGuiText().currentNature)));
                Button existing = GooeyButton.builder()
                        .display(itemStack)
                        .onClick(action -> {
                        })
                        .build();
                mintList.add(existing);
            } else {
                ItemStack mintStack = new ItemStack(mintItem);
                mintStack.set(DataComponents.CUSTOM_NAME, Component.literal("§a" + natureStr + " Nature"));
                Button mint = GooeyButton.builder()
                        .display(mintStack)
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
            ItemStack ballStack = new ItemStack(ballItem);
            ballStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            String ballName = ballStack.getDisplayName().getString().replaceAll("[\\[\\]]", "");
            ballStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d" + ballName));
            GooeyButton.Builder buttonBuilder = GooeyButton.builder();
                    //.title(("§d" + ballName));
            if (pokemon.getCaughtBall().equals(ball)) {
                ballStack.set(DataComponents.CUSTOM_NAME, Component.literal((dataManager.getGuiText().currentBall)));
                buttonBuilder.display(ballStack);

                buttonBuilder
                        //.title((dataManager.getGuiText().currentBall))
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
            ItemStack itemStack = maleBuilder.build().getDisplay();
            itemStack.set(DataComponents.LORE,
                    new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().currentGender))));
            maleBuilder.display(itemStack);
            maleBuilder
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
            ItemStack itemStack = femaleBuilder.build().getDisplay();
            itemStack.set(DataComponents.LORE,
                    new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().currentGender))));
            femaleBuilder.display(itemStack);
            femaleBuilder
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
            ItemStack itemStack = femaleBuilder.build().getDisplay();
            itemStack.set(DataComponents.LORE,
                    new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().currentGender))));
            femaleBuilder.display(itemStack);
            femaleBuilder
                    .onClick(action -> {
                    })
                    .build();

            ItemStack maleStack = maleBuilder.build().getDisplay();
            maleStack.set(DataComponents.LORE,
                    new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().currentGender))));
            maleBuilder.display(maleStack);
            maleBuilder
                    .onClick(action -> {
                    })
                    .build();
        }
    }

    public static void checkAbilityButton(Pokemon pokemon, ServerPlayer player, GooeyButton.Builder ab1Builder, GooeyButton.Builder ab2Builder, GooeyButton.Builder ab3Builder) {
        List<Ability> abilities = new ArrayList<>();
        pokemon.getForm().getAbilities().getMapping().values().forEach(potentialAbilities -> {
            for (PotentialAbility potentialAbility : potentialAbilities) {
                Ability ability = new Ability(potentialAbility.getTemplate(), true, Priority.HIGH);
                if (!abilities.contains(ability)) {
                    abilities.add(ability);
                }
            }
        });

        int count = 0;

        for (Ability ability : abilities) {
            Pokemon clone = pokemon.clone(true, RegistryAccess.EMPTY);
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
            ItemStack itemStack = new ItemStack(CobblemonItems.ABILITY_CAPSULE);
            itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            itemStack.set(DataComponents.LORE,
                    new ItemLore(Collections.singletonList(Component.literal("§eCurrent ability."))));
            builder
                    .display(itemStack)
                    //.lore(Collections.singletonList(("§eCurrent ability.")))
                    .onClick(action -> {
                    })
                    .build();
        }
    }

    private static void buildAbilityOption(GooeyButton.Builder builder, Ability ability, boolean ha, ServerPlayer player) {
        ItemStack itemStack = new ItemStack(CobblemonItems.ABILITY_CAPSULE);
        itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        builder
                .display(setStackName(itemStack, ha ? "§b"
                        + Utils.parseLang(ability.getDisplayName()) + " §7(§6HA§7)" : "§b"
                        + Utils.parseLang(ability.getDisplayName())))
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
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().isMaxIVs))));
                    increaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                } else if (pokemon.getIvs().get(stat) == 31) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().isMaxIVStat))));
                    increaseBuilder
                            .display(new ItemStack(Blocks.BARRIER))
                            .onClick(action -> {
                            });
                }
                break;
            case EVS:
                if (isMaxEVs(pokemon)) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().isMaxEVs))));
                    increaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                } else if (pokemon.getEvs().get(stat) == 252) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal(dataManager.getGuiText().isMaxEVStat))));
                    increaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
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
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE, new ItemLore(
                            Collections.singletonList(Component.literal("§cThis Pokemon can't lower its IVs!"))));
                    decreaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                } else if (ivs.get(stat) == 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE, new ItemLore(
                            Collections.singletonList(Component.literal("§cThis stat can't be lowered!"))));
                    decreaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                break;
            case EVS:
                if (isMinEVs(pokemon)) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE, new ItemLore(
                            Collections.singletonList(Component.literal("§cThis Pokemon can't lower its EVs!"))));
                    decreaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                } else if (pokemon.getEvs().get(stat) == 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE, new ItemLore(
                            Collections.singletonList(Component.literal("§cThis stat can't be lowered!"))));
                    decreaseBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
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
        ItemStack ironStack = new ItemStack(CobblemonItems.IRON);
        ironStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        ironStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+1 "+currentStat.name()));
        oneStatBuilder.display(ironStack);
        ironStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+10 "+currentStat.name()));
        tenStatBuilder.display(ironStack);
        ironStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+25 "+currentStat.name()));
        twentyFiveStatBuilder.display(ironStack);
        switch (selection) {
            case INCREASE -> {
                if (ivs.get(currentStat) + 1 > 31) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max IV."))));
                    oneStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });

                }
                if (ivs.get(currentStat) + 10 > 31) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max IV."))));
                    tenStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });

                }
                if (ivs.get(currentStat) + 25 > 31) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max IV."))));
                    twentyFiveStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
            }
            case DECREASE -> {
                if (ivs.get(currentStat) - 1 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative IVs."))));
                    oneStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (ivs.get(currentStat) - 10 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative IVs."))));
                    tenStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (ivs.get(currentStat) - 25 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative IVs."))));
                    twentyFiveStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
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
        ItemStack calcStack = new ItemStack(CobblemonItems.CALCIUM);
        calcStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        calcStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+1 "+currentStat.name()));
        oneStatBuilder.display(calcStack);
        calcStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+10 "+currentStat.name()));
        tenStatBuilder.display(calcStack);
        calcStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d§l+25 "+currentStat.name()));
        twentyFiveStatBuilder.display(calcStack);

        switch (selection) {
            case INCREASE -> {
                if (evs.get(currentStat) + 1 > 252) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max EVs."))));
                    oneStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (evs.get(currentStat) + 10 > 252) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max EVs."))));
                    tenStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (evs.get(currentStat) + 25 > 252) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches over max EVs."))));
                    twentyFiveStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
            }
            case DECREASE -> {
                if (evs.get(currentStat) - 1 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative EVs."))));
                    oneStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (evs.get(currentStat) - 10 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative EVs."))));
                    tenStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
                if (evs.get(currentStat) - 25 < 0) {
                    ItemStack itemStack = new ItemStack(Blocks.BARRIER);
                    itemStack.set(DataComponents.LORE,
                            new ItemLore(Collections.singletonList(Component.literal("§cResult reaches negative EVs."))));
                    twentyFiveStatBuilder
                            .display(itemStack)
                            .onClick(action -> {
                            });
                }
            }
            default -> {
            }
        }
    }

}
