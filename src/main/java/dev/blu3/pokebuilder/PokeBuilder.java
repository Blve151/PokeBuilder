package dev.blu3.pokebuilder;


import ca.landonjw.gooeylibs2.api.UIManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.blu3.pokebuilder.config.PokeBuilderDataManager;
import dev.blu3.pokebuilder.ui.PokeBuilderUI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class PokeBuilder implements ModInitializer {

    public static PokeBuilderDataManager dataManager = new PokeBuilderDataManager();

    public static HashMap<String, Item> stringPaneMap = new HashMap<>();
    private static final String BASE_PERMISSION = "pokebuilder.command.";

    @Override
    public void onInitialize() {
        // Cache config pane options
        stringPaneMap.put("red", Blocks.RED_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("cyan", Blocks.CYAN_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("lime", Blocks.LIME_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("pink", Blocks.PINK_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("gray", Blocks.GRAY_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("blue", Blocks.BLUE_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("white", Blocks.WHITE_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("brown", Blocks.BROWN_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("green", Blocks.GREEN_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("black", Blocks.BLACK_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("orange", Blocks.ORANGE_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("yellow", Blocks.YELLOW_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("purple", Blocks.PURPLE_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("magenta", Blocks.MAGENTA_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("lightblue", Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.asItem());
        stringPaneMap.put("lightgray", Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.asItem());

        // Load configuration and token data
        try {
            dataManager.load(Optional.empty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Main UI Command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pokebuilder")
                .executes(context -> {
                    CommandSourceStack src = context.getSource();
                    ServerPlayer player = src.getPlayer();
                    if(!canUseCommand(src, "pokebuilder")) return 1;

                    if (player == null) {
                        src.sendFailure(Component.literal("You need to be a player if you don't provide an argument."));
                        return 1;
                    }
                    UIManager.openUIForcefully(player, PokeBuilderUI.menuUI(player));
                    return 1;
                })
                .then(argument("player", EntityArgument.player())
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if(canUseCommand(src, "other.pokebuilder")){
                                UIManager.openUIForcefully(player, PokeBuilderUI.menuUI(player));
                            } else {
                                src.sendFailure(Component.literal("You need permission to open the UI for another player!"));
                            }
                            return 1;
                        }))));

        // Reload command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pbreload")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            if(!canUseCommand(src, "pbreload")) return 1;

                            try {
                                dataManager.load(Optional.of(src));
                            } catch (IOException ignored) {}
                            return 1;
                        })));

        // Tokens command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tokens")
                .executes(context -> {
                    CommandSourceStack src = context.getSource();
                    if(!canUseCommand(src, "tokens")) return 1;

                    ServerPlayer player = src.getPlayer();
                    if (player == null) {
                        src.sendFailure(Component.literal("You need to be a player if you don't provide an argument."));
                        return 1;
                    }

                    sendBalanceMessage(player, context.getSource(), dataManager.getMessages().othersToken);
                    return 1;
                })
                .then(argument("player", EntityArgument.player())
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if(canUseCommand(src, "other.tokens")){
                                sendBalanceMessage(player, src, dataManager.getMessages().othersToken);
                            } else {
                                src.sendFailure(Component.literal("You need permission to view the token balance of another player!"));
                            }
                            return 1;
                        }))));


        // Tokens Set command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tokensset")
                .then(argument("player", EntityArgument.player())
                        .then(argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    if(!canUseCommand(context.getSource(), "tokensset")) return 1;

                                    int input = IntegerArgumentType.getInteger(context, "amount");
                                    dataManager.setTokens(player, input);
                                    String msg1 = dataManager.getMessages().senderTokensSet;
                                    msg1 = msg1.replace("<tokens>", input + "");
                                    msg1 = msg1.replace("<playerName>", player.getName().getString());
                                    context.getSource().sendSystemMessage(Component.literal(msg1));

                                    String msg2 = dataManager.getMessages().receiverTokensSet;
                                    msg2 = msg2.replace("<tokens>", input + "");
                                    player.sendSystemMessage(Component.literal(msg2));
                                    return 1;
                                })))));

        // Tokens Add command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tokensadd")
                .then(argument("player", EntityArgument.player())
                .then(argument("amount", IntegerArgumentType.integer())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if(!canUseCommand(context.getSource(), "tokensadd")) return 1;

                            int input = IntegerArgumentType.getInteger(context, "amount");
                            dataManager.addTokens(player, input);
                            String msg = dataManager.getMessages().senderTokensAdd;
                            msg = msg.replace("<tokens>", input + "");
                            msg = msg.replace("<playerName>", player.getName().getString());
                            context.getSource().sendSystemMessage(Component.literal(msg));

                            String msgRec1 = dataManager.getMessages().receiverTokensAdd;
                            msgRec1 = msgRec1.replace("<tokens>", input + "");
                            player.sendSystemMessage(Component.literal(msgRec1));
                            String msgRec2 = dataManager.getMessages().currentBal;
                            msgRec2 = msgRec2.replace("<tokens>", "" + dataManager.getTokens(player));
                            player.sendSystemMessage(Component.literal(msgRec2));
                            return 1;
                        })))));

        // Tokens Remove command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tokensremove")
                .then(argument("player", EntityArgument.player())
                .then(argument("amount", IntegerArgumentType.integer())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if(!canUseCommand(context.getSource(), "tokensremove")) return 1;

                            int input = IntegerArgumentType.getInteger(context, "amount");
                            dataManager.removeTokens(player, input);
                            String msg = dataManager.getMessages().senderTokensRemove;
                            msg = msg.replace("<tokens>", input + "");
                            msg = msg.replace("<playerName>", player.getName().getString());
                            context.getSource().sendSystemMessage(Component.literal(msg));

                            String msgRec1 = dataManager.getMessages().receiverTokensRemove;
                            msgRec1 = msgRec1.replace("<tokens>", input + "");
                            player.sendSystemMessage(Component.literal(msgRec1));
                            String msgRec2 = dataManager.getMessages().currentBal;
                            msgRec2 = msgRec2.replace("<tokens>", "" + dataManager.getTokens(player));
                            player.sendSystemMessage(Component.literal(msgRec2));
                            return 1;
                        })))));

        // Tokens Pay command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tokenspay")
                .then(argument("player", EntityArgument.player())
                .then(argument("amount", IntegerArgumentType.integer())
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            if(!canUseCommand(context.getSource(), "tokenspay")) return 1;

                            ServerPlayer player = src.getPlayer();
                            ServerPlayer cmdPlayer = EntityArgument.getPlayer(context, "player");

                            int input = IntegerArgumentType.getInteger(context, "amount");
                            if (player.getName().equals(cmdPlayer.getName())) {
                                cmdPlayer.sendSystemMessage(Component.literal(dataManager.getMessages().cantSelfPay));
                                return 1;
                            }
                            if (input > dataManager.getTokens(cmdPlayer).intValue()) {
                                cmdPlayer.sendSystemMessage(Component.literal(dataManager.getMessages().payTooHigh));
                                return 1;
                            }
                            if (input < 1) {
                                cmdPlayer.sendSystemMessage(Component.literal(dataManager.getMessages().payTooLow));
                                return 1;
                            }

                            dataManager.removeTokens(cmdPlayer, input);
                            sendBalanceMessage(cmdPlayer, cmdPlayer.createCommandSourceStack(), dataManager.getMessages().senderTokensPay);

                            dataManager.addTokens(player, input);
                            sendBalanceMessage(player, player.createCommandSourceStack(), dataManager.getMessages().receiverTokensPay);
                            return 1;
                        })))));
    }

    private void sendBalanceMessage(ServerPlayer tokenPlayer, CommandSourceStack source, String msg) {
        msg = msg.replace("<playerName>", tokenPlayer.getName().getString());
        msg = msg.replace("<tokens>", "" + dataManager.getTokens(tokenPlayer));
        source.sendSystemMessage(Component.literal(msg));
    }

    private boolean canUseCommand(CommandSourceStack source, String name) {
        if (source.getServer().createCommandSourceStack().equals(source)
        || source.hasPermission(4)) return true;
        if (!Permissions.check(source, BASE_PERMISSION + name)) {
            source.sendFailure(Component.literal("You don't have permission to run this command!"));
            return false;
        }
        return true;
    }
}
