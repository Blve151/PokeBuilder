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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.StringUtils;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.Collections;

import static dev.blu3.pokebuilder.PokeBuilder.dataManager;
import static dev.blu3.pokebuilder.utils.ButtonUtils.*;
import static dev.blu3.pokebuilder.utils.Utils.*;
import static net.minecraft.network.chat.Component.literal;


public class PokeBuilderUI {

    public static GooeyPage menuUI(ServerPlayer player) {
        PlaceholderButton placeholder = new PlaceholderButton();

        Button pc = GooeyButton.builder()
                .display(setStackName(new ItemStack(CobblemonItems.PC), dataManager.getGuiText().pcMenu))
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
                .display(setStackName(new ItemStack(Items.ARROW), dataManager.getGuiText().pcFirstPage))
                .onClick(action -> UIManager.openUIForcefully(player, pcUI(player)))
                .build();

        LinkedPageButton previous = LinkedPageButton.builder()
                .display(setStackName(new ItemStack(Items.ARROW), dataManager.getGuiText().previous))
                .linkType(LinkType.Previous)
                .build();

        Button back = GooeyButton.builder()
                .display(setStackName(new ItemStack(Items.GOLDEN_CARROT), dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, menuUI(player))).build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(setStackName(new ItemStack(Items.CARROT), dataManager.getGuiText().pcNextPage))
                .linkType(LinkType.Next)
                .build();


        Button last = GooeyButton.builder()
                .display(setStackName(new ItemStack(Items.CARROT), dataManager.getGuiText().pcLastPage))
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
                .display(setStackName(new ItemStack(Items.CARROT), dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, menuUI(player))).build();

        ItemStack shinyAttr = new ItemStack(CobblemonItems.LIGHT_BALL);
        shinyAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        shinyAttr.set(DataComponents.CUSTOM_NAME, literal(regex(dataManager.getGuiText().shiny)));
        shinyAttr.set(DataComponents.CUSTOM_DATA, null);

        if(listContainsPokemon(dataManager.getGeneral().shinyBlacklistedPokemon, pokemon)){
            shinyAttr.set(DataComponents.LORE, new ItemLore(Collections.singletonList(Component.literal(regex(dataManager.getGuiText().attrPokeDisabled)))));
        } else if (pokemon.getShiny() && !dataManager.getGeneral().allowShinyUnset) {
            shinyAttr.set(DataComponents.LORE, new ItemLore(Collections.singletonList(Component.literal(regex(dataManager.getGuiText().isShiny)))));
        }else{
            shinyAttr.set(DataComponents.LORE, new ItemLore(Collections.emptyList()));
        }

        Button shiny = GooeyButton.builder()
                .display(shinyAttr)
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
                .build();

        ItemStack natureAttr = new ItemStack(CobblemonItems.MODEST_MINT);
        natureAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        natureAttr.set(DataComponents.CUSTOM_NAME, literal(regex(dataManager.getGuiText().nature)));
        natureAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(listContainsPokemon(dataManager.getGeneral().natureBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));
        Button nature = GooeyButton.builder()
                .display(natureAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().natureBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, naturesUI(player));
                    }
                })
                .build();

        ItemStack levelAttr = new ItemStack(CobblemonItems.RARE_CANDY);
        levelAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        levelAttr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().level));
        levelAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(pokemon.getLevel() == Cobblemon.config.getMaxPokemonLevel() ? dataManager.getGuiText().isMaxLevel : listContainsPokemon(dataManager.getGeneral().levelBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));
        Button level = GooeyButton.builder()
                .display(levelAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().levelBlacklistedPokemon, pokemon) && pokemon.getLevel() != Cobblemon.config.getMaxPokemonLevel()) {
                        UIManager.openUIForcefully(player, levelsUI(player));
                    }

                })
                .build();


        ItemStack ballAtr = new ItemStack(CobblemonItems.POKE_BALL);
        ballAtr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        ballAtr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ball));
        ballAtr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(listContainsPokemon(dataManager.getGeneral().ballBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));

        Button ball = GooeyButton.builder()
                .display(ballAtr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ballBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, ballsUI(player));
                    }
                })
                .build();

        ItemStack ivAttr = new ItemStack(CobblemonItems.IRON);
        ivAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        ivAttr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ivs));
        ivAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));
        Button ivs = GooeyButton.builder()
                .display(ivAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, statSelectionUI(player, BuilderAttribute.IVS));
                    }
                })
                .build();

        ItemStack evAttr = new ItemStack(CobblemonItems.CARBOS);
        evAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        evAttr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().evs));
        evAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(listContainsPokemon(dataManager.getGeneral().evBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));
        Button evs = GooeyButton.builder()
                .display(evAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().ivBlacklistedPokemon, pokemon)) {
                        UIManager.openUIForcefully(player, statSelectionUI(player, BuilderAttribute.EVS));
                    }
                })
                .build();


        boolean apply = canChangeGender(pokemon);

        ItemStack genderAttr = new ItemStack(CobblemonItems.CHOICE_SCARF);
        genderAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        genderAttr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().gender));
        genderAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(literal(!apply ? dataManager.getGuiText().genderChange : listContainsPokemon(dataManager.getGeneral().genderBlacklistedPokemon, pokemon) ? dataManager.getGuiText().attrPokeDisabled : ""))));
        Button gender = GooeyButton.builder()
                .display(genderAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().genderBlacklistedPokemon, pokemon) && apply) {
                        UIManager.openUIForcefully(player, genderUI(player));
                    }
                })
                .build();


        int validAbilities = pokemon.getSpecies().getAbilities().getMapping().keySet().size();
        ItemStack abilityAttr = new ItemStack(CobblemonItems.ABILITY_CAPSULE);
        abilityAttr.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        abilityAttr.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ability));
        abilityAttr.set(DataComponents.LORE,
                new ItemLore(Collections.singletonList(
                        validAbilities < 2 ? literal(dataManager.getGuiText().abilityChange) :
                                listContainsPokemon(dataManager.getGeneral().abilityBlacklistedPokemon, pokemon) ? literal(dataManager.getGuiText().attrPokeDisabled) : Component.empty()
                )));
        Button ability = GooeyButton.builder()
                .display(abilityAttr)
                .onClick(action -> {
                    if (!listContainsPokemon(dataManager.getGeneral().abilityBlacklistedPokemon, pokemon) && validAbilities > 1) {
                        UIManager.openUIForcefully(player, abilityUI(player));
                    }
                })
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

        ItemStack stack = new ItemStack(Items.RED_WOOL);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().confirmDeny));
        stack.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal(dataManager.getGuiText().confirmDenyLore))));
        Button deny = GooeyButton.builder()
                .display(stack)
                //.title(dataManager.getGuiText().confirmDeny)
                //.lore(Collections.singletonList(dataManager.getGuiText().confirmDenyLore))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player)))
                .build();


        ItemStack stack2 = new ItemStack(Items.GREEN_WOOL);
        stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().confirmAccept));
        GooeyButton.Builder confirmBuilder = GooeyButton.builder()
                .display(stack2);
                //.title(dataManager.getGuiText().confirmAccept);

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

                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(shinyText)
                )));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), shinyText))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setShiny(setShiny);
                                player.sendSystemMessage(literal(finalMsg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case NATURE:
                Nature nature = clickedData.currentNature;
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setNature.replace("<nature>", Utils.parseLang(nature.getDisplayName())))
                )));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost), dataManager.getGuiText().setNature.replace("<nature>", Utils.parseLang(nature.getDisplayName()))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setNature(nature);
                                String msg = dataManager.getMessages().natureSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<nature>", Utils.parseLang(nature.getDisplayName()));
                                player.sendSystemMessage(literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }

                        }).build();
                break;
            case LEVEL:
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setLevels.replace("<levels>", "" + clickedData.currentLevel))
                )));
                int level = clickedData.currentLevel;
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost), dataManager.getGuiText().setLevels.replace("<levels>", "" + level)))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setLevel(pokemon.getLevel() + level);
                                String msg = dataManager.getMessages().lvlInc;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<level>", "" + level);
                                player.sendSystemMessage(literal(msg));
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
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setBall.replace("<ball>", ballName))
                )));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setBall.replace("<ball>", ballName))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setCaughtBall(ball);
                                pokemon.updateAspects();
                                String msg = dataManager.getMessages().ballSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<ball>", ballName);
                                player.sendSystemMessage(literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case IVS:
                if (isIncrease) {
                    stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                            literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                            literal(dataManager.getGuiText().setIncIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))
                    )));
                    confirmBuilder.display(stack2)
                            //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setIncIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    ivs.set(currentStat, ivs.get(currentStat) + currentStatNum);
                                    String msg = dataManager.getMessages().ivInc;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<iv>", "" + currentStatNum);
                                    player.sendSystemMessage(literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                } else {
                    stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                            literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                            literal(dataManager.getGuiText().setDecIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))
                    )));
                    confirmBuilder.display(stack2)
                            //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setDecIVs.replace("<iv>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    ivs.set(currentStat, ivs.get(currentStat) - currentStatNum);
                                    String msg = dataManager.getMessages().ivDec;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<iv>", "" + currentStatNum);
                                    player.sendSystemMessage(literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                }
                break;
            case MAX_IVS:
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setMaxIVs)
                )));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setMaxIVs)))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                for (Stats baseStat : baseStats) {
                                    ivs.set(baseStat, 31);
                                }
                                String msg = dataManager.getMessages().ivMax;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                player.sendSystemMessage(literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case EVS:
                if (isIncrease) {
                    stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                            literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                            literal(dataManager.getGuiText().setIncEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))
                    )));
                    confirmBuilder.display(stack2)
                            //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setIncEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    evs.set(currentStat, evs.get(currentStat) + currentStatNum);
                                    String msg = dataManager.getMessages().evInc;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<ev>", "" + currentStatNum);
                                    player.sendSystemMessage(literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                } else {
                    stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                            literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                            literal(dataManager.getGuiText().setDecEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))
                    )));
                    confirmBuilder.display(stack2)
                            //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setDecEVs.replace("<ev>", "" + currentStatNum).replace("<stat>", statName))))
                            .onClick(action -> {
                                if (canAfford(player, finalCost)) {
                                    evs.set(currentStat, evs.get(currentStat) - currentStatNum);
                                    String msg = dataManager.getMessages().evDec;
                                    msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                    msg = msg.replace("<stat>", statName);
                                    msg = msg.replace("<ev>", "" + currentStatNum);
                                    player.sendSystemMessage(literal(msg));
                                    UIManager.openUIForcefully(player, attributesUI(player));
                                }
                            }).build();
                }
                break;
            case GENDER:
                Gender gender = clickedData.currentGender;
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setGender.replace("<gender>", gender.name().toLowerCase())))));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setGender.replace("<gender>", gender.name().toLowerCase()))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setGender(gender);
                                String msg = dataManager.getMessages().genderSet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<gender>", gender.name().toLowerCase());
                                player.sendSystemMessage(literal(msg));
                                UIManager.openUIForcefully(player, attributesUI(player));
                            }
                        }).build();
                break;
            case ABILITY:
                Ability ability = clickedData.currentAbility;
                String abStr = Utils.parseLang(ability.getDisplayName());
                stack2.set(DataComponents.LORE, new ItemLore(Arrays.asList(
                        literal(dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)),
                        literal(dataManager.getGuiText().setAbility.replace("<ability>", abStr))
                )));
                confirmBuilder.display(stack2)
                        //.lore(Arrays.asList((dataManager.getGuiText().tokenCost.replace("<cost>", "" + finalCost)), (dataManager.getGuiText().setAbility.replace("<ability>", abStr))))
                        .onClick(action -> {
                            if (canAfford(player, finalCost)) {
                                pokemon.setAbility$common(ability);
                                String msg = dataManager.getMessages().abilitySet;
                                msg = msg.replace("<pokemon>", pokemon.getSpecies().getName());
                                msg = msg.replace("<ability>", abStr);
                                player.sendSystemMessage(literal(msg));
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back
                //))
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
                ItemStack stack2 = new ItemStack(CobblemonItems.KINGS_ROCK);
                stack2.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().maxIVs));
                GooeyButton.Builder maxIVBuilder = GooeyButton.builder()
                        .display(stack2)
                        //.title(((dataManager.getGuiText().maxIVs)))
                        .onClick(action -> {
                            UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.MAX_IVS, dataManager.getPrices().maxIVCost));
                        });

                if(Utils.isMaxIVs(pokemon)){
                    stack2.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal(dataManager.getGuiText().isMaxIVs))));
                    maxIVBuilder.display(stack2);
                    //maxIVBuilder.lore(Collections.singletonList(dataManager.getGuiText().isMaxIVs));
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        String spacedStat = clickedData.currentStat.getDisplayName().getString();

        ItemStack stack2 = new ItemStack(Items.GREEN_STAINED_GLASS);
        stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())));
        GooeyButton.Builder increaseBuilder = GooeyButton.builder()
                .display(stack2)
                //.title((dataManager.getGuiText().statIncrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())))
                .onClick(action -> {
                    clickedData.isIncrease = true;
                    setClickedData(player, clickedData);
                    UIManager.openUIForcefully(player, numSelection(player, attr, BuilderSelection.INCREASE));
                });

        ItemStack stack3 = new ItemStack(Items.RED_STAINED_GLASS);
        stack3.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statDecrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())));
        GooeyButton.Builder decreaseBuilder = GooeyButton.builder()
                .display(stack3)
                //.title((dataManager.getGuiText().statDecrease.replace("<stat>", spacedStat).replace("<attr>", attr.getName())))
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> {
                    UIManager.openUIForcefully(player, attributesUI(player));

                    UIManager.openUIForcefully(player, attributesUI(player));
                }).build();

        ItemStack rareCandy = new ItemStack(CobblemonItems.RARE_CANDY);
        rareCandy.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        rareCandy.set(DataComponents.CUSTOM_NAME, literal("§d§l+1"+ attr.getName()));
        GooeyButton.Builder oneStatBuilder = GooeyButton.builder().display(rareCandy);
        rareCandy.set(DataComponents.CUSTOM_NAME, literal("§d§l+10"+ attr.getName()));
        GooeyButton.Builder tenStatBuilder = GooeyButton.builder().display(rareCandy);
        rareCandy.set(DataComponents.CUSTOM_NAME, literal("§d§l+25"+ attr.getName()));
        GooeyButton.Builder twentyFiveStatBuilder = GooeyButton.builder().display(rareCandy);

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
                updateBuilderTitle(oneStatBuilder, dataManager.getGuiText().statIncreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName));
                updateBuilderTitle(tenStatBuilder, dataManager.getGuiText().statIncreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName));
                updateBuilderTitle(twentyFiveStatBuilder, dataManager.getGuiText().statIncreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName));
                //oneStatBuilder.title((dataManager.getGuiText().statIncreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                //tenStatBuilder.title((dataManager.getGuiText().statIncreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                //twentyFiveStatBuilder.title((dataManager.getGuiText().statIncreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName)));
            }
            case DECREASE -> {
                updateBuilderTitle(oneStatBuilder, dataManager.getGuiText().statDecreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName));
                updateBuilderTitle(tenStatBuilder, dataManager.getGuiText().statDecreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName));
                updateBuilderTitle(twentyFiveStatBuilder, dataManager.getGuiText().statDecreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName));
               // oneStatBuilder.title((dataManager.getGuiText().statDecreaseOne.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                //tenStatBuilder.title((dataManager.getGuiText().statDecreaseTen.replace("<stat>", spacedStat).replace("<attr>", attrName)));
                //twentyFiveStatBuilder.title((dataManager.getGuiText().statDecreaseTwentyFive.replace("<stat>", spacedStat).replace("<attr>", attrName)));
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        GooeyButton.Builder oneLevelBuider = GooeyButton.builder();
                //.title((dataManager.getGuiText().statIncreaseOneLevel));

        GooeyButton.Builder fiveLevelBuider = GooeyButton.builder();
                //.title((dataManager.getGuiText().statIncreaseFiveLevel));

        GooeyButton.Builder tenLevelBuider = GooeyButton.builder();
                //.title((dataManager.getGuiText().statIncreaseTenLevel));

        ItemStack oneLevelStack;
        ItemStack fiveLevelStack;
        ItemStack tenLevelStack;
        if (pokemon.getLevel() + 1 > Cobblemon.config.getMaxPokemonLevel()) {
            oneLevelStack = new ItemStack(Blocks.BARRIER);
            oneLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseOneLevel));
            oneLevelStack.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal(dataManager.getGuiText().resultMaxLevel))));
            oneLevelBuider
                    .display(oneLevelStack);
                    //.lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            oneLevelStack = new ItemStack(CobblemonItems.RARE_CANDY);
            oneLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseOneLevel));
            oneLevelBuider
                    .display(oneLevelStack)
                    .onClick(action -> {
                        clickedData.currentLevel = 1;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.LEVEL, dataManager.getPrices().oneLevelCost));
                    });
        }

        if (pokemon.getLevel() + 5 > Cobblemon.config.getMaxPokemonLevel()) {
            fiveLevelStack = new ItemStack(Blocks.BARRIER);
            fiveLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseFiveLevel));
            fiveLevelStack.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal(dataManager.getGuiText().resultMaxLevel))));
            fiveLevelBuider
                    .display(fiveLevelStack);
                    //.lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            fiveLevelStack = new ItemStack(CobblemonItems.RARE_CANDY);
            fiveLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseFiveLevel));

            fiveLevelBuider
                    .display(fiveLevelStack)
                    .onClick(action -> {
                        clickedData.currentLevel = 5;
                        setClickedData(player, clickedData);
                        UIManager.openUIForcefully(player, confirmationUI(player, BuilderAttribute.LEVEL, dataManager.getPrices().fiveLevelCost));
                    });
        }

        if (pokemon.getLevel() + 10 > Cobblemon.config.getMaxPokemonLevel()) {
            tenLevelStack = new ItemStack(Blocks.BARRIER);
            tenLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseTenLevel));
            tenLevelStack.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal(dataManager.getGuiText().resultMaxLevel))));
            tenLevelBuider
                    .display(tenLevelStack);
                    //.lore(Collections.singletonList((dataManager.getGuiText().resultMaxLevel)));
        } else {
            tenLevelStack = new ItemStack(CobblemonItems.RARE_CANDY);
            tenLevelStack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().statIncreaseTenLevel));
            tenLevelStack.set(DataComponents.LORE,null);
            tenLevelBuider
                    .display(tenLevelStack)
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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        ItemStack stack2 = new ItemStack(Items.ARROW);
        stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().previous));
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(stack2)
                //.title(dataManager.getGuiText().previous)
                .linkType(LinkType.Previous)
                .build();

        ItemStack stack3 = new ItemStack(Items.ARROW);
        stack3.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().pcNextPage));
        LinkedPageButton next = LinkedPageButton.builder()
                .display(stack3)
                //.title(dataManager.getGuiText().pcNextPage)
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
        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();

        ItemStack stack2 = new ItemStack(Items.LIGHT_BLUE_WOOL);
        stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().male));
        GooeyButton.Builder maleBuilder = GooeyButton.builder()
                //.title((dataManager.getGuiText().male))
                .display(stack2);

        ItemStack stack3 = new ItemStack(Items.PINK_WOOL);
        stack3.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().female));
        GooeyButton.Builder femaleBuilder = GooeyButton.builder()
                //.title((dataManager.getGuiText().female))
                .display(stack3);


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

        ItemStack stack = new ItemStack(Items.CARROT);
        stack.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().back));
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        Button back = GooeyButton.builder()
                .display(stack)
                //.title((dataManager.getGuiText().back))
                .onClick(action -> UIManager.openUIForcefully(player, attributesUI(player))).build();


        ItemStack stack2 = new ItemStack(Items.BARRIER);
        stack2.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ability1));
        stack2.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal("§cNot available."))));
        GooeyButton.Builder ab1Builder = GooeyButton.builder()
                //.title((dataManager.getGuiText().ability1))
                .display(stack2)
                //.lore(Collections.singletonList(("§cNot available.")))
                .onClick(action -> {});

        ItemStack stack3 = new ItemStack(Items.BARRIER);
        stack3.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ability2));
        stack3.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal("§cNot available."))));

        GooeyButton.Builder ab2Builder = GooeyButton.builder()
                //.title((dataManager.getGuiText().ability2))
                .display(stack3)
                //.lore(Collections.singletonList(("§cNot available.")))
                .onClick(action -> {});

        ItemStack stack4 = new ItemStack(Items.BARRIER);
        stack4.set(DataComponents.CUSTOM_NAME, literal(dataManager.getGuiText().ability3));
        stack4.set(DataComponents.LORE, new ItemLore(Collections.singletonList(literal("§cNot available."))));
        GooeyButton.Builder ab3Builder = GooeyButton.builder()
                //.title((dataManager.getGuiText().ability3))
                .display(stack4)
                //.lore(Collections.singletonList(("§cNot available.")))
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



