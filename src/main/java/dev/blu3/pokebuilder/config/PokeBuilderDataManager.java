package dev.blu3.pokebuilder.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.blu3.pokebuilder.config.types.*;

import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.blu3.pokebuilder.PokeBuilder.dataManager;


public class PokeBuilderDataManager {
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().enableComplexMapKeySerialization().create();

    private static final File mainDir = new File("./config/pokebuilder");
    private static final File configDir = new File(mainDir, "/config");
    private static final File dataDir = new File(mainDir, "/data");

    private final File generalFile = new File(configDir, "general.json");
    private final File pricesFile = new File(configDir, "prices.json");
    private final File messagesFile = new File(configDir, "messages.json");
    private final File guiTextFile = new File(configDir, "guitext.json");
    private final File guiButtonFile = new File(configDir, "guibutton.json");
    private final File tokenDataFile = new File(dataDir, "balance_data.json");

    private PokeBuilderGeneralConfig general = new PokeBuilderGeneralConfig();
    private PokeBuilderPricesConfig prices = new PokeBuilderPricesConfig();
    private PokeBuilderMessagesConfig messages = new PokeBuilderMessagesConfig();
    private PokeBuilderGUITextConfig guiText = new PokeBuilderGUITextConfig();
    private PokeBuilderGUIButtonConfig guiButton = new PokeBuilderGUIButtonConfig();
    private PokeBuilderTokenData tokenData = new PokeBuilderTokenData();

    public void load(Optional<CommandSourceStack> optSender) throws IOException {
        try {
            if (!mainDir.exists()) {
                mainDir.mkdirs();
            }
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            general = (PokeBuilderGeneralConfig) handleFile(generalFile, general);
            prices = (PokeBuilderPricesConfig) handleFile(pricesFile, prices);
            messages = (PokeBuilderMessagesConfig) handleFile(messagesFile, messages);
            guiText = (PokeBuilderGUITextConfig) handleFile(guiTextFile, guiText);
            guiButton = (PokeBuilderGUIButtonConfig) handleFile(guiButtonFile, guiButton);
            tokenData = (PokeBuilderTokenData) handleFile(tokenDataFile, tokenData);

            optSender.ifPresent(sender -> sender.sendSystemMessage(Component.literal("§a[!] Reloaded PokeBuilder configs.")));


        } catch (Exception ex) {
            optSender.ifPresent(sender -> sender.sendFailure(Component.literal("[!] An error occurred while loading PokeBuilder configs, check console for errors.")));
            ex.printStackTrace();
        }
    }

    public Object handleFile(File file, Object obj) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(obj));
            fw.close();
        } else {
            FileReader fr = new FileReader(file);
            obj = gson.fromJson(fr, TypeToken.of(obj.getClass()).getType());
            fr.close();
        }
        return obj;
    }

    private void updateDataFile() {
        CompletableFuture.runAsync(() -> {
            try {
                FileWriter fw = new FileWriter(tokenDataFile);
                fw.write(gson.toJson(tokenData));
                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public boolean hasTokenData(ServerPlayer player) {
        if (!dataManager.getGeneral().useLegacyTokens) {
            try {
                EconomyService service = Impactor.instance().services().provide(EconomyService.class);
                Currency currency = service.currencies().currency(Key.key(this.general.impactorCurrencyKey)).get();
                return service.hasAccount(currency, player.getUUID()).get();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return tokenData.data.get(player.getUUID()) != null;
    }

    public @NotNull BigDecimal getTokens(ServerPlayer player) {
        if (!hasTokenData(player)) {
            setTokens(player, 0);
        }
        if (!dataManager.getGeneral().useLegacyTokens) {
            try {
                EconomyService service = Impactor.instance().services().provide(EconomyService.class);
                Currency currency = service.currencies().currency(Key.key(this.general.impactorCurrencyKey)).get();
                return service.account(currency, player.getUUID()).get().balance();
            } catch (Exception e) {
                e.printStackTrace();
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.valueOf(tokenData.data.get(player.getUUID()));
    }

    public void setTokens(ServerPlayer player, int num) {
        if (!dataManager.getGeneral().useLegacyTokens) {
            try {
                EconomyService service = Impactor.instance().services().provide(EconomyService.class);
                Currency currency = service.currencies().currency(Key.key(this.general.impactorCurrencyKey)).get();
                service.account(currency, player.getUUID()).get().set(BigDecimal.valueOf(num));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tokenData.data.put(player.getUUID(), num);
            updateDataFile();
        }
    }

    public void addTokens(ServerPlayer player, int num) {
        if (!dataManager.getGeneral().useLegacyTokens) {
            try {
                EconomyService service = Impactor.instance().services().provide(EconomyService.class);
                Currency currency = service.currencies().currency(Key.key(this.general.impactorCurrencyKey)).get();
                service.account(currency, player.getUUID()).get().set(getTokens(player).add(BigDecimal.valueOf(num)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tokenData.data.put(player.getUUID(), this.getTokens(player).intValue() + num);
            updateDataFile();
        }
    }

    public void removeTokens(ServerPlayer player, int num) {
        if (!dataManager.getGeneral().useLegacyTokens) {
            try {
                EconomyService service = Impactor.instance().services().provide(EconomyService.class);
                Currency currency = service.currencies().currency(Key.key(this.general.impactorCurrencyKey)).get();
                service.account(currency, player.getUUID()).get().set(getTokens(player).subtract(BigDecimal.valueOf(num)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tokenData.data.put(player.getUUID(), tokenData.data.get(player.getUUID()) - num);
            updateDataFile();
        }
    }

    public PokeBuilderGeneralConfig getGeneral() {
        return general;
    }

    public PokeBuilderPricesConfig getPrices() {
        return prices;
    }

    public PokeBuilderMessagesConfig getMessages() {
        return messages;
    }

    public PokeBuilderGUITextConfig getGuiText() {
        return guiText;
    }

    public PokeBuilderGUIButtonConfig getGuiButton() {
        return guiButton;
    }
}
