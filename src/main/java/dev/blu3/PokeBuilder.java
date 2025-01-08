package dev.blu3;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.blu3.pokebuilder.config.PokeBuilderDataManager;
import dev.blu3.pokebuilder.ui.PokeBuilderUI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static dev.blu3.pokebuilder.utils.Utils.regex;
import static net.minecraft.commands.Commands.argument;

public class PokeBuilder implements ModInitializer {
	public static final String MOD_ID = "pokebuilder";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	public static PokeBuilderDataManager dataManager = new PokeBuilderDataManager();

	public static HashMap<String, Block> stringPaneMap = new HashMap<>();

	private static final String BASE_PERMISSION = "pokebuilder.command.";

	@Override
	public void onInitialize() {
		stringPaneMap.put("red", StainedGlassPaneBlock.byItem(Items.RED_STAINED_GLASS_PANE));
		stringPaneMap.put("cyan", StainedGlassPaneBlock.byItem(Items.CYAN_STAINED_GLASS_PANE));
		stringPaneMap.put("lime", StainedGlassPaneBlock.byItem(Items.LIME_STAINED_GLASS_PANE));
		stringPaneMap.put("pink", StainedGlassPaneBlock.byItem(Items.PINK_STAINED_GLASS_PANE));
		stringPaneMap.put("gray", StainedGlassPaneBlock.byItem(Items.GRAY_STAINED_GLASS_PANE));
		stringPaneMap.put("blue", StainedGlassPaneBlock.byItem(Items.BLUE_STAINED_GLASS_PANE));
		stringPaneMap.put("white", StainedGlassPaneBlock.byItem(Items.WHITE_STAINED_GLASS_PANE));
		stringPaneMap.put("brown", StainedGlassPaneBlock.byItem(Items.BROWN_STAINED_GLASS_PANE));
		stringPaneMap.put("green", StainedGlassPaneBlock.byItem(Items.GREEN_STAINED_GLASS_PANE));
		stringPaneMap.put("black", StainedGlassPaneBlock.byItem(Items.BLACK_STAINED_GLASS_PANE));
		stringPaneMap.put("orange", StainedGlassPaneBlock.byItem(Items.ORANGE_STAINED_GLASS_PANE));
		stringPaneMap.put("yellow", StainedGlassPaneBlock.byItem(Items.YELLOW_STAINED_GLASS_PANE));
		stringPaneMap.put("purple", StainedGlassPaneBlock.byItem(Items.PURPLE_STAINED_GLASS_PANE));
		stringPaneMap.put("magenta", StainedGlassPaneBlock.byItem(Items.MAGENTA_STAINED_GLASS_PANE));
		stringPaneMap.put("lightblue", StainedGlassPaneBlock.byItem(Items.LIGHT_BLUE_STAINED_GLASS_PANE));
		stringPaneMap.put("lightgray", StainedGlassPaneBlock.byItem(Items.LIGHT_GRAY_STAINED_GLASS_PANE));
		try {
			dataManager.load(Optional.empty());
		} catch (IOException e) {
			throw new RuntimeException(e);

		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("pokebuilder")
					.requires(source -> canUseCommand(source, "pokebuilder") && source.getEntity() instanceof ServerPlayer)
					.executes(context -> {
						CommandSourceStack source = context.getSource(); // Use the CommandSourceStack from the context
						ServerPlayer player = (ServerPlayer) source.getEntity();
						UIManager.openUIForcefully(player, PokeBuilderUI.menuUI(player));
						return 1;
					})
					.then(RequiredArgumentBuilder.<CommandSourceStack, ServerPlayer>argument("player", EntityArgument.player())
							.requires(source -> canUseCommand(source, "pokebuilder.other"))
							.executes(context -> {
								ServerPlayer player = EntityArgument.getPlayer(context, "player"); // Directly resolve the player
								UIManager.openUIForcefully(player, PokeBuilderUI.menuUI(player));
								return 1;
							})
					)
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("pbreload").executes(())));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("tokens").executes(())).then(class_2170.method_9244("player", (ArgumentType)class_2186.method_9305()).executes(()))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("tokensset").then(class_2170.method_9244("player", (ArgumentType)class_2186.method_9305()).then(class_2170.method_9244("amount", (ArgumentType) IntegerArgumentType.integer()).executes(())))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("tokensadd").then(class_2170.method_9244("player", (ArgumentType)class_2186.method_9305()).then(class_2170.method_9244("amount", (ArgumentType)IntegerArgumentType.integer()).executes(())))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("tokensremove").then(class_2170.method_9244("player", (ArgumentType)class_2186.method_9305()).then(class_2170.method_9244("amount", (ArgumentType)IntegerArgumentType.integer()).executes(())))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("tokenspay").then(class_2170.method_9244("player", (ArgumentType)class_2186.method_9305()).then(((RequiredArgumentBuilder)class_2170.method_9244("amount", (ArgumentType)IntegerArgumentType.integer()).requires(class_2168::method_43737)).executes(())))));
	}

	private void sendBalanceMessage(ServerPlayer tokenPlayer, CommandSourceStack source) {
		String msg = (dataManager.getMessages()).othersToken;
		msg = msg.replace("<playerName>", tokenPlayer.getName().getString());
		msg = msg.replace("<tokens>", "" + dataManager.getTokens(tokenPlayer));
		source.sendSystemMessage(Component.literal(regex(msg)));
	}

	private boolean canUseCommand(Object source, String name) {
		CommandSourceStack stack = (CommandSourceStack) source;
		if (!Permissions.check(stack, BASE_PERMISSION + name)) {
			stack.sendFailure(Component.literal("You don't have permission to run this command!"));
			return false;
		}
		return true;
	}
}