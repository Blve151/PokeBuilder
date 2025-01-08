package dev.blu3.pokebuilder.ui;


import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.*;

import dev.blu3.pokebuilder.enums.BuilderAttribute;
import dev.blu3.pokebuilder.enums.BuilderSelection;
import dev.blu3.pokebuilder.utils.PokeBuilderClickedData;
import dev.blu3.pokebuilder.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;

import static dev.blu3.pokebuilder.PokeBuilder.dataManager;
import static dev.blu3.pokebuilder.utils.ButtonUtils.*;
import static dev.blu3.pokebuilder.utils.Utils.*;


public class PokeBuilderUI {

    public static GooeyPage menuUI(ServerPlayer player) {
        PlaceholderButton placeholder = new PlaceholderButton();

        Button pc = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.PC))
                .title(dataManager.getGuiText().pcMenu)
                .onClick(action -> UIManager.openUIForcefully(player, pcUI(player)))
                .build();


        ChestTemplate template = ChestTemplate.builder(3)
                .checker(0, 0, 3, 9,
                        colouredPane(dataManager.getGuiButton().pane1Pane2.getA()),
                        colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .rectangle(1, 1, 1, 6, placeholder)
                .set(1, 7, pc)
                .build();

        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .title(dataManager.getGuiText().normalTitle);

        return PaginationHelper.createPagesFromPlaceholders(template, getPokeButtonList(player, true), pageBuilder);
    }

    public static GooeyPage pcUI(ServerPlayer player) {

        PlaceholderButton placeholder = new PlaceholderButton();

        Button first = GooeyButton.builder()
                .display(new ItemStack(Items.ARROW))
                .title(dataManager.getGuiText().pcFirstPage)
                .onClick(action -> UIManager.openUIForcefully(player, pcUI(player)))
                .build();

        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.ARROW))
                .title(dataManager.getGuiText().previous)
                .linkType(LinkType.Previous)
                .build();

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.GOLDEN_CARROT))
                .title(dataManager.getGuiText().back)
                .onClick(action -> UIManager.openUIForcefully(player, menuUI(player))).build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title(dataManager.getGuiText().pcNextPage)
                .linkType(LinkType.Next)
                .build();

        Button last = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title(dataManager.getGuiText().pcLastPage)
                .onClick(action -> {
                    LinkedPage linkedPage = (LinkedPage) action.getPage();
                    linkedPage = linkedPage.getNext();
                    if (linkedPage != null) {
                        while (linkedPage.getNext() != null) {
                            linkedPage = linkedPage.getNext();
                        }
                        UIManager.openUIForcefully(player, linkedPage);
                    }
                })
                .build();


        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9,
                        colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .rectangle(0, 1, 5, 6, placeholder)
                .set(0, 8, first)
                .set(1, 8, previous)
                .set(2, 8, back)
                .set(3, 8, next)
                .set(4, 8, last)
                .build();

        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .title(dataManager.getGuiText().pcTitle);

        return PaginationHelper.createPagesFromPlaceholders(template, (getPokeButtonList(player, false)), pageBuilder);
    }

    public static GooeyPage attributesUI(ServerPlayer player) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;
        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title(dataManager.getGuiText().back)
                .onClick(action -> UIManager.openUIForcefully(player, menuUI(player))).build();

        Button shiny = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.LIGHT_BALL))
                .title(dataManager.getGuiText().shiny)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().shinyBlacklistedPokemon, pokemon)) {
                        if (pokemon.getShiny()) {
                            if (dataManager.getGeneral().allowShinyUnset) {
                                UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.SHINY, dataManager.getPrices().unsetShinyCost));
                            }
                        } else {
                            UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.SHINY, dataManager.getPrices().shinyCost));
                        }
                    }
                })
                .lore(Collections.singletonList(listContainsPokemon(dataManager.getGeneral().shinyBlacklistedPokemon, pokemon) ? (dataManager.getGuiText().attrPokeDisabled) : pokemon.getShiny() && !dataManager.getGeneral().allowShinyUnset ? (dataManager.getGuiText().isShiny) : null))
                .build();

        Button nature = GooeyButton.builder()
                .display(removeTooltips(new ItemStack(CobblemonItems.CHERI_BERRY)))
                .title(dataManager.getGuiText().nature)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().natureBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, naturesUI(player));
                    }
                })
                .lore(Collections.singletonList(listContainsPokemon(dataManager.getGeneral().natureBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : null))
                .build();

        Button level = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.RARE_CANDY))
                .title(dataManager.getGuiText().level)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().levelBlacklistedPokemon, pokemon) && pokemon.getLevel() != Cobblemon.config.getMaxPokemonLevel()) {
                        UIManager.openUIForcefully(player, levelsUI(player));
                    }

                })
                .lore(Collections.singletonList(pokemon.getLevel() == Cobblemon.config.getMaxPokemonLevel() ? dataManager.getGuiText().isMaxLevel : listContainsPokemon(dataManager.getGeneral().levelBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : null))
                .build();


        Button ball = GooeyButton.builder()
                .display(removeTooltips(new ItemStack(CobblemonItems.POKE_BALL)))
                .title(dataManager.getGuiText().ball)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ballBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, ballsUI(player));
                    }
                })
                .lore(Collections.singletonList(listContainsPokemon(dataManager.getGeneral().ballBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : null))
                .build();

        Button ivs = GooeyButton.builder()
                .display(removeTooltips(new ItemStack(CobblemonItems.IRON)))
                .title(dataManager.getGuiText().ivs)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, statSelectionUI(player, BuilderAttribute.IVS));
                    }
                })
                .lore(Collections.singletonList(listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon) ? (dataManager.getGuiText().attrPokeDisabled) : null))
                .build();

        Button evs = GooeyButton.builder()
                .display(removeTooltips(new ItemStack(CobblemonItems.CARBOS)))
                .title(dataManager.getGuiText().evs)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, statSelectionUI(player, BuilderAttribute.EVS));
                    }
                })
                .lore(Collections.singletonList(listContainsPokemon(dataManager.getGeneral().evBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : null))
                .build();


        boolean apply = canChangeGender(pokemon);

        Button gender = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.CHOICE_SCARF))
                .title(dataManager.getGuiText().gender)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().genderBlacklistedPokemon, pokemon) && apply) {
                        UIManager.openUIForcefully(player, genderUI(player));
                    }
                })
                .lore(Collections.singletonList(!apply ? dataManager.getGuiText().genderChange : listContainsPokemon(dataManager.getGeneral().genderBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : null))
                .build();

        int validAbilities = pokemon.getSpecies().getAbilities().getMapping().keySet().size();
        Button ability = GooeyButton.builder()
                .display(new ItemStack(CobblemonItems.ABILITY_CAPSULE))
                .title(dataManager.getGuiText().ability)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().abilityBlacklistedPokemon, pokemon) && validAbilities > 1) {
                        UIManager.openUIForcefully(player, abilityUI(player));
                    }
                })
                .lore(Collections.singletonList(validAbilities < 2 ? dataManager.getGuiText().abilityChange : listContainsPokemon(dataManager.getGeneral().abilityBlacklistedPokemon, pokemon) ? (dataManager.getGuiText().attrPokeDisabled) : null))
                .build();

//        Button breedlock = GooeyButton.builder()
//                .display(new ItemStack(PixelmonBlocks.ranchBlock))
//                .title((dataManager.getGuiText().breedLock))
//                .onClick(action -> {
//                    if (!listContainsPokemon(dataManager.getGeneral().breedlockBlacklistedPokemon, pokemon) && !pokemon.hasSpecFlag("unbreedable")) {
//                        UIManager.openUIForcefully(player, confirmationUI(player, EnumPokeBuilderAttribute.BREEDLOCK, dataManager.getPrices().breedLockCost));
//                    }
//                })
//                .lore(Collections.singletonList(pokemon.hasSpecFlag("unbreedable") ? (dataManager.getGuiText().breedChange) : listContainsPokemon(dataManager.getGeneral().breedlockBlacklistedPokemon, pokemon) ? (dataManager.getGuiText().attrPokeDisabled) : null))
//                .build();

        ChestTemplate.Builder template = ChestTemplate.builder(6)
                .checker(0, 0, 6, 9,
                        colouredPane(dataManager.getGuiButton().pane1Pane2.getA()),
                        colouredPane(dataManager.getGuiButton().pane1Pane2.getB()));

        if (dataManager.getGuiButton().shinyButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().shinyButtonLocation.getA(), dataManager.getGuiButton().shinyButtonLocation.getB()
                    , shiny);
        }

        if (dataManager.getGuiButton().natureButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().natureButtonLocation.getA(), dataManager.getGuiButton().natureButtonLocation.getB()
                    , nature);
        }

        if (dataManager.getGuiButton().levelButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().levelButtonLocation.getA(), dataManager.getGuiButton().levelButtonLocation.getB()
                    , level);
        }


        if (dataManager.getGuiButton().ballButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().ballButtonLocation.getA(), dataManager.getGuiButton().ballButtonLocation.getB()
                    , ball);
        }

        if (dataManager.getGuiButton().ivButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().ivButtonLocation.getA(), dataManager.getGuiButton().ivButtonLocation.getB()
                    , ivs);
        }

        if (dataManager.getGuiButton().evButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().evButtonLocation.getA(), dataManager.getGuiButton().evButtonLocation.getB()
                    , evs);
        }

        if (dataManager.getGuiButton().pokemonButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().pokemonButtonLocation.getA(), dataManager.getGuiButton().pokemonButtonLocation.getB()
                    , getPokeDescButton(pokemon));
        }

        if (dataManager.getGuiButton().genderButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().genderButtonLocation.getA(), dataManager.getGuiButton().genderButtonLocation.getB()
                    , gender);
        }

        if (dataManager.getGuiButton().abilityButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().abilityButtonLocation.getA(), dataManager.getGuiButton().abilityButtonLocation.getB()
                    , ability);
        }

//        if (dataManager.getGuiButton().breedLockButtonLocation.getA() != -1) {
//            template.set(dataManager.getGuiButton().breedLockButtonLocation.getA(), dataManager.getGuiButton().breedLockButtonLocation.getB()
//                    ,breedlock);
//        }

        if (dataManager.getGuiButton().backButtonLocation.getA() != -1) {
            template.set(dataManager.getGuiButton().backButtonLocation.getA(), dataManager.getGuiButton().backButtonLocation.getB()
                    , back);
        }

        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .template(template.build())
                .title(dataManager.getGuiText().attrTitle);

        return pageBuilder.build();

    }

    public static GooeyPage confirmationUI(ServerPlayer player, BuilderAttribute attr, int cost) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        boolean isIncrease = clickedData.isIncrease;
        Pokemon pokemon = clickedData.currentPokemon;
        IVs ivs = pokemon.getIvs();
        EVs evs = pokemon.getEvs();
        int currentStatNum = clickedData.currentStatNum;
        Stats currentStat = clickedData.currentStat;
        String statName = "";
        if (currentStat != null) currentStat.getDisplayName().getString();

        Button deny = GooeyButton.builder()
                .display(new ItemStack(Blocks.RED_WOOL))
                .title(dataManager.getGuiText().confirmDeny)
                .lore(Collections.singletonList(dataManager.getGuiText().confirmDenyLore))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player)))
                .build();


        GooeyButton.Builder confirmBuilder = GooeyButton.builder()
                .display(new ItemStack(Blocks.GREEN_WOOL))
                .title(dataManager.getGuiText().confirmAccept);

        if (isMultipliedCost(pokemon)) {
            cost = (int) (cost * dataManager.getPrices().priceMultiplier);
        }
        final int finalCost = cost;

        switch (attr) {
            case SHINY:
                boolean setShiny;
                String shinyText;
                String cfgMsg;
                if (pokemon.getShiny()) {
                    shinyText = regex(dataManager.getGuiText().removeShiny);
                    setShiny = false;
                    cfgMsg = dataManager.getMessages().shinyUnset;
                } else {
                    shinyText = dataManager.getGuiText().setShiny;
                    setShiny = true;
                    cfgMsg = dataManager.getMessages().shinySet;
                }
                cfgMsg = cfgMsg.replace("<pokemon>", pokemon.getSpecies().getName());
                final String finalMsg = cfgMsg;

                confirmBuilder
                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), shinyText))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setShiny(setShiny);
                                player.sendSystemMessage(Component.literal(finalMsg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case NATURE:
                Nature nature = clickedData.currentNature;
                confirmBuilder
                        .lore(Arrays.asList(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost), dataManager.getGuiText().setNature.replace("<nature>", Utils.parseLang(nature.getDisplayName()))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setNature(nature);
                                String msg = dataManager.getMessages().natureSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<nature>", Utils.parseLang(nature.getDisplayName()));
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }

                        }).build();
                break;
            case LEVEL:
                int level = clickedData.currentLevel;
                confirmBuilder
                        .lore(Arrays.asList(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost), dataManager.getGuiText().setLevels.replace("<levels>", "" + level)))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setLevel(pokemon.getLevel() + level);
                                String msg = dataManager.getMessages().lvlInc;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<level>", "" + level);
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                                PokemonEntity pe = pokemon.getEntity();
                                if (pe != null && pe.getEvolutionEntity() != null) {
                                    pe.setEvolutionEntity(pe.getEvolutionEntity());
                                }
                            }
                        }).build();
                break;
            case BALL:
                PokeBall ball = clickedData.currentBall;
                String ballName = new ItemStack(ball.item()).getDisplayName().getString().replaceAll("[\\[\\]]", "");
                confirmBuilder
                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setBall.replace("<ball>", ballName))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setCaughtBall(ball);
                                pokemon.updateAspects();
                                String msg = dataManager.getMessages().ballSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<ball>", ballName);
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case IVS:
                if (isIncrease) {
                    confirmBuilder
                            .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setIncIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    ivs.set(currentStat, ivs.get(currentStat) + currentStatNum);
                                    String msg = dataManager.getMessages().ivInc;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<iv>", "" + currentStatNum);
                                    player.sendSystemMessage(Component.literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                } else {
                    confirmBuilder
                            .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setDecIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    ivs.set(currentStat, ivs.get(currentStat) - currentStatNum);
                                    String msg = dataManager.getMessages().ivDec;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<iv>", "" + currentStatNum);
                                    player.sendSystemMessage(Component.literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                }
                break;
            case MAX_IVS:
                confirmBuilder
                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setMaxIVs)))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                for (Stats baseStat : baseStats) {
                                    ivs.set(baseStat, 31);
                                }
                                String msg = dataManager.getMessages().ivMax;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case EVS:
                if (isIncrease) {
                    confirmBuilder
                            .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setIncEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    evs.set(currentStat, evs.get(currentStat) + currentStatNum);
                                    String msg = dataManager.getMessages().evInc;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<ev>", "" + currentStatNum);
                                    player.sendSystemMessage(Component.literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                } else {
                    confirmBuilder
                            .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setDecEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    evs.set(currentStat, evs.get(currentStat) - currentStatNum);
                                    String msg = dataManager.getMessages().evDec;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<ev>", "" + currentStatNum);
                                    player.sendSystemMessage(Component.literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                }
                break;
            case GENDER:
                Gender gender = clickedData.currentGender;
                confirmBuilder
                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setGender.replace("<gender>", gender.name().toLowerCase()))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setGender(gender);
                                String msg = dataManager.getMessages().genderSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<gender>", gender.name().toLowerCase());
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case ABILITY:
                Ability ability = clickedData.currentAbility;
                String abStr = Utils.parseLang(ability.getDisplayName());
                confirmBuilder
                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setAbility.replace("<ability>", abStr))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setAbility$common(ability);
                                String msg = dataManager.getMessages().abilitySet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<ability>", abStr);
                                player.sendSystemMessage(Component.literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
//            case BREEDLOCK:
//                confirmBuilder
//                        .lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setBreedLock)))
//                        .onClick(action -> {
//                            if (canAfford(player, finalCost)) {
//                                pokemon.addSpecFlag("unbreedable");
//                                String msg = dataManager.getMessages().breedLockSet;
//                                msg = msg.replace("<pokemon>", pokemon.getSpecies().name);
//                                player.sendMessage(TextSerializer.parse(msg));
//                                UIManager.openUIForcefully(player, attributesUI(player));
//                            }
//                        }).build();
//                break;
        }

        Button confirm = confirmBuilder.build();

        ChestTemplate template = ChestTemplate.builder(3)
                .checker(0, 0, 3, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(1, 2, confirm)
                .set(1, 4, getPokeDescButton(pokemon))
                .set(1, 6, deny)
                .build();


        String titleString = dataManager.getGuiText().attrNameTitle;
        titleString = titleString.replace("<attribute>", attr.getName());

        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .template(template)
                .title(titleString);

        return pageBuilder.build();
    }


    public static GooeyPage statSelectionUI(ServerPlayer player, BuilderAttribute attr) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back
                ))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        ChestTemplate.Builder templateBuilder = ChestTemplate.builder(3)
                .checker(0, 0, 3, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(1, 1, getStatSelectionButton(clickedData, CobblemonItems.POWER_WEIGHT, dataManager.getGuiText().statHP, Stats.HP, player, attr))
                .set(1, 2, getStatSelectionButton(clickedData, CobblemonItems.POWER_BRACER, dataManager.getGuiText().statAttack, Stats.ATTACK, player, attr))
                .set(1, 3, getStatSelectionButton(clickedData, CobblemonItems.POWER_BELT, dataManager.getGuiText().statDefence, Stats.DEFENCE, player, attr))
                .set(1, 4, getStatSelectionButton(clickedData, CobblemonItems.POWER_LENS, dataManager.getGuiText().statSpAttack, Stats.SPECIAL_ATTACK, player, attr))
                .set(1, 5, getStatSelectionButton(clickedData, CobblemonItems.POWER_BAND, dataManager.getGuiText().statSpDefence, Stats.SPECIAL_DEFENCE, player, attr))
                .set(1, 6, getStatSelectionButton(clickedData, CobblemonItems.POWER_ANKLET, dataManager.getGuiText().statSpeed, Stats.SPEED, player, attr))
                .set(2, 4, back);

        switch (attr) {
            case IVS:
                GooeyButton.Builder maxIVBuilder = GooeyButton.builder()
                        .display(new ItemStack((CobblemonItems.KINGS_ROCK)))
                        .title(((dataManager.getGuiText().maxIVs)))
                        .onClick(action -> {
                            UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.MAX_IVS, dataManager.getPrices().maxIVCost));
                        });

                if(Utils.isMaxIVs(pokemon)){
                    maxIVBuilder.lore(Collections.singletonList(dataManager.getGuiText().isMaxIVs));
                    maxIVBuilder.onClick(() -> {});
                }

                templateBuilder.set(1, 7, maxIVBuilder.build());

                return LinkedPage.builder()
                        .template(templateBuilder.build())
                        .title(dataManager.getGuiText().ivStatTitle).build();
            case EVS:
                return LinkedPage.builder()
                        .template(templateBuilder.build())
                        .title(dataManager.getGuiText().evStatTitle).build();
            default:
                return LinkedPage.builder()
                        .template(templateBuilder.build()).build();
        }
    }

    public static GooeyPage increaseDecreaseUI(ServerPlayer player, BuilderAttribute attr) {

        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        String spacedStat = clickedData.currentStat.getDisplayName().getString();

        GooeyButton.Builder increaseBuilder = GooeyButton.builder()
                .display(new ItemStack(Blocks.GREEN_STAINED_GLASS))
                .title((dataManager.getGuiText().statIncrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())))
                .onClick(action -> {
                    clickedData.isIncrease = true;
                    setClickedData(player, clickedData);
                    UIManager.openUIForcefully(player, numSelection(player, attr, BuilderSelection.INCREASE));
                });

        GooeyButton.Builder decreaseBuilder = GooeyButton.builder()
                .display(new ItemStack(Blocks.RED_STAINED_GLASS))
                .title((dataManager.getGuiText().statDecrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())))
                .onClick(action -> {
                    clickedData.isIncrease = false;
                    setClickedData(player, clickedData);
                    UIManager.openUIForcefully(player, numSelection(player, attr, BuilderSelection.DECREASE));
                });

        checkIncButton(player, increaseBuilder, attr);
        checkDecButton(player, decreaseBuilder, attr);

        Button increase = increaseBuilder.build();

        Button decrease = decreaseBuilder.build();

        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(4, 4, back)
                .set(2, 2, increase)
                .set(2, 6, decrease)
                .build();

        String titleString = dataManager.getGuiText().attrNameTitle;
        titleString = titleString.replace("<attribute>", attr.getName());

        return LinkedPage.builder()
                .template(template)
                .title((titleString)).build();
    }

    public static GooeyPage numSelection(ServerPlayer player, BuilderAttribute attr, BuilderSelection selection) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;
        boolean isIncrease = clickedData.isIncrease;

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> {
                    UIManager.openUIForcefully(player, attributesUI(player));

                    UIManager.openUIForcefully(player, attributesUI(player));
                }).build();

        GooeyButton.Builder oneStatBuilder = GooeyButton.builder();

        GooeyButton.Builder tenStatBuilder = GooeyButton.builder();

        GooeyButton.Builder twentyFiveStatBuilder = GooeyButton.builder();

        oneStatBuilder.onClick(action -> {
            clickedData.currentStatNum = 1;
            setClickedData(player, clickedData);
            switch (attr) {
                case IVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().oneIVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().oneIVDecCost));
                    }
                    break;
                case EVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().oneEVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().oneEVDecCost));
                    }
                    break;
            }
        });

        tenStatBuilder.onClick(action -> {
            clickedData.currentStatNum = 10;
            setClickedData(player, clickedData);
            switch (attr) {
                case IVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().tenIVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().tenIVDecCost));
                    }
                    break;
                case EVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().tenEVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().tenEVDecCost));
                    }
                    break;
            }
        });

        twentyFiveStatBuilder.onClick(action -> {
            clickedData.currentStatNum = 25;
            setClickedData(player, clickedData);
            switch (attr) {
                case IVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().twentyFiveIVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().twentyFiveIVDecCost));
                    }
                    break;
                case EVS:
                    if (isIncrease) {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().twentyFiveEVIncCost));
                    } else {
                        UIManager.openUIForcefully(player, confirmationUI(player, attr, dataManager.getPrices().twentyFiveEVDecCost));
                    }
                    break;
            }
        });

        String spacedStat = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(clickedData.currentStat.name().replaceAll("\\d+", "")), " ");
        String attrName = attr.getName().substring(0, attr.getName().length() - 1);
        switch (selection) {
            case INCREASE -> {
                oneStatBuilder.title((dataManager.getGuiText().statIncreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                tenStatBuilder.title((dataManager.getGuiText().statIncreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                twentyFiveStatBuilder.title((dataManager.getGuiText().statIncreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName)));
            }
            case DECREASE -> {
                oneStatBuilder.title((dataManager.getGuiText().statDecreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                tenStatBuilder.title((dataManager.getGuiText().statDecreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                twentyFiveStatBuilder.title((dataManager.getGuiText().statDecreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName)));
            }
        }

        switch (attr) {
            case IVS -> checkIVStatButton(player, selection, oneStatBuilder, tenStatBuilder, twentyFiveStatBuilder);
            case EVS -> checkEVStatButton(player, selection, oneStatBuilder, tenStatBuilder, twentyFiveStatBuilder);
        }


        Button oneStat = oneStatBuilder.build();
        Button tenStat = tenStatBuilder.build();
        Button twentyFiveStat = twentyFiveStatBuilder.build();


        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .fill(colouredPane("gray"))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(2, 2, oneStat)
                .set(2, 4, tenStat)
                .set(2, 6, twentyFiveStat)
                .set(4, 4, back)
                .build();

        String titleString = dataManager.getGuiText().attrNameTitle;
        titleString = titleString.replace("<attribute>", attr.getName());

        return LinkedPage.builder()
                .template(template)
                .title((titleString)).build();
    }


    public static GooeyPage naturesUI(ServerPlayer player) {

        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;

        PlaceholderButton placeholder = new PlaceholderButton();

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        ChestTemplate template = ChestTemplate.builder(6)
                .checker(0, 0, 6, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .rectangle(1, 1, 4, 7, placeholder)
                .set(0, 4, getPokeDescButton(pokemon))
                .set(5, 4, back)
                .build();


        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .title(((dataManager.getGuiText().natureTitle)));

        return PaginationHelper.createPagesFromPlaceholders(template, (getMintButtons(pokemon)), pageBuilder);
    }

    public static GooeyPage levelsUI(ServerPlayer player) {

        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        GooeyButton.Builder oneLevelBuider = GooeyButton.builder()
                .title((dataManager.getGuiText().statIncreaseOneLevel));

        GooeyButton.Builder fiveLevelBuider = GooeyButton.builder()
                .title((dataManager.getGuiText().statIncreaseFiveLevel));

        GooeyButton.Builder tenLevelBuider = GooeyButton.builder()
                .title((dataManager.getGuiText().statIncreaseTenLevel));

        if (pokemon.getLevel() + 1 > Cobblemon.config.getMaxPokemonLevel()) {
            oneLevelBuider
                    .display(new ItemStack(Blocks.BARRIER))
                    .lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            oneLevelBuider
                    .display(new ItemStack(CobblemonItems.RARE_CANDY))
                    .onClick(action -> {
                        clickedData.currentLevel = 1;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.LEVEL, dataManager.getPrices().oneLevelCost));
                    });
        }

        if (pokemon.getLevel() + 5 > Cobblemon.config.getMaxPokemonLevel()) {
            fiveLevelBuider
                    .display(new ItemStack(Blocks.BARRIER))
                    .lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            fiveLevelBuider
                    .display(new ItemStack(CobblemonItems.RARE_CANDY))
                    .onClick(action -> {
                        clickedData.currentLevel = 5;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.LEVEL, dataManager.getPrices().fiveLevelCost));
                    });
        }

        if (pokemon.getLevel() + 10 > Cobblemon.config.getMaxPokemonLevel()) {
            tenLevelBuider
                    .display(new ItemStack(Blocks.BARRIER))
                    .lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            tenLevelBuider
                    .display(new ItemStack(CobblemonItems.RARE_CANDY))
                    .onClick(action -> {
                        clickedData.currentLevel = 10;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.LEVEL, dataManager.getPrices().tenLevelCost));
                    });
        }

        Button oneLevel = oneLevelBuider.build();
        Button fiveLevel = fiveLevelBuider.build();
        Button tenLevel = tenLevelBuider.build();


        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(2, 2, oneLevel)
                .set(2, 4, fiveLevel)
                .set(2, 6, tenLevel)
                .set(4, 4, back)
                .build();

        return LinkedPage.builder()
                .template(template)
                .title(((dataManager.getGuiText().levelTitle)))
                .build();
    }

    public static GooeyPage ballsUI(ServerPlayer player) {
        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;
        PlaceholderButton placeholder = new PlaceholderButton();

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.ARROW))
                .title(dataManager.getGuiText().previous)
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.ARROW))
                .title(dataManager.getGuiText().pcNextPage)
                .linkType(LinkType.Next)
                .build();

        ChestTemplate template = ChestTemplate.builder(6)
                .checker(0, 0, 6, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .rectangle(1, 1, 4, 7, placeholder)
                .set(0, 4, getPokeDescButton(pokemon))
                .set(5, 4, back)
                .set(5, 3, previous)
                .set(5, 5, next)
                .build();

        LinkedPage.Builder pageBuilder = LinkedPage.builder()
                .title((dataManager.getGuiText().ballTitle));

        return PaginationHelper.createPagesFromPlaceholders(template, (getBallButtons(pokemon)), pageBuilder);
    }


    public static GooeyPage genderUI(ServerPlayer player) {

        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;
        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        GooeyButton.Builder maleBuilder = GooeyButton.builder()
                .title((dataManager.getGuiText().male))
                .display(new ItemStack(Blocks.LIGHT_BLUE_WOOL));

        GooeyButton.Builder femaleBuilder = GooeyButton.builder()
                .title((dataManager.getGuiText().female))
                .display(new ItemStack(Blocks.PINK_WOOL));


        checkGenderButton(pokemon, player, maleBuilder, femaleBuilder);

        Button male = maleBuilder.build();
        Button female = femaleBuilder.build();

        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(2, 2, male)
                .set(2, 6, female)
                .set(4, 4, back)
                .build();

        return LinkedPage.builder()
                .template(template)
                .title((dataManager.getGuiText().genderTitle))
                .build();
    }

    public static GooeyPage abilityUI(ServerPlayer player) {

        PokeBuilderClickedData clickedData = getClickedData(player);
        Pokemon pokemon = clickedData.currentPokemon;

        Button back = GooeyButton.builder()
                .display(new ItemStack(Items.CARROT))
                .title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();


        GooeyButton.Builder ab1Builder = GooeyButton.builder()
                .title((dataManager.getGuiText().ability1))
                .display(new ItemStack(Blocks.BARRIER))
                .lore(Collections.singletonList(("§cNot available.")))
                .onClick(action -> {});

        GooeyButton.Builder ab2Builder = GooeyButton.builder()
                .title((dataManager.getGuiText().ability2))
                .display(new ItemStack(Blocks.BARRIER))
                .lore(Collections.singletonList(("§cNot available.")))
                .onClick(action -> {});

        GooeyButton.Builder ab3Builder = GooeyButton.builder()
                .title((dataManager.getGuiText().ability3))
                .display(new ItemStack(Blocks.BARRIER))
                .lore(Collections.singletonList(("§cNot available.")))
                .onClick(action -> {});

        checkAbilityButton(pokemon, player, ab1Builder, ab2Builder, ab3Builder);

        Button ab1 = ab1Builder.build();
        Button ab2 = ab2Builder.build();
        Button ab3 = ab3Builder.build();


        ChestTemplate template = ChestTemplate.builder(5)
                .checker(0, 0, 5, 9, colouredPane(dataManager.getGuiButton().pane1Pane2.getA()), colouredPane(dataManager.getGuiButton().pane1Pane2.getB()))
                .set(0, 4, getPokeDescButton(pokemon))
                .set(2, 2, ab1)
                .set(2, 4, ab2)
                .set(2, 6, ab3)
                .set(4, 4, back)
                .build();

        return LinkedPage.builder()
                .template(template)
                .title((dataManager.getGuiText().abilityTitle))
                .build();
    }
}



