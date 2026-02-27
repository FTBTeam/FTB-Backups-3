package dev.ftb.mods.ftbbackups.client;

import dev.ftb.mods.ftbbackups.Backups;
import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@Mod(value = FTBBackups.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FTBBackups.MOD_ID, value = Dist.CLIENT)
public class BackupsClient {
    private static BackupProgress backupProgress = BackupProgress.NONE;

    private static int progressTicker = 0;
    private static boolean disabledOnServer = false;

    public BackupsClient() {
        NeoForge.EVENT_BUS.addListener(BackupsClient::onClientDisconnected);
        NeoForge.EVENT_BUS.addListener(BackupsClient::addRestoreButton);
        NeoForge.EVENT_BUS.addListener(BackupsClient::clientTick);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        if (progressTicker > 0 && --progressTicker == 0) {
            backupProgress = BackupProgress.NONE;
        }
    }

    private static void addRestoreButton(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof SelectWorldScreen s) {
            Backups backups = Backups.getClientInstance();
            if (!backups.backups().isEmpty()) {
                Component btnLabel = Component.translatable("ftbbackups3.gui.restore").append("...");
                Component title = Component.translatable("ftbbackups3.gui.restore");
                int w = s.getMinecraft().font.width(btnLabel) + 20;
                event.addListener(Button.builder(btnLabel, b -> Minecraft.getInstance().setScreen(new RestoreBackupScreen(s, title)))
                        .bounds(s.width - w - 10, 22, w, 20)
                        .tooltip(Tooltip.create(Component.translatable("ftbbackups3.gui.restore.tooltip")))
                        .build());
            }
        }
    }

    @SubscribeEvent
    public static void registerGuiLayer(RegisterGuiLayersEvent event) {
        // needs to be done with SubscribeEvent, addListener() is called too late
        if (!FTBBackups.isDisabledByEnvironmentVar()) {
            event.registerAboveAll(BackupOverlayLayer.ID, BackupOverlayLayer.INSTANCE);
        }
    }

    public static void setBackupProgress(int current, int total) {
        backupProgress = new BackupProgress(current, total);
        if (backupProgress.finished()) {
            progressTicker = 40;
        }
    }

    private static void onClientDisconnected(ClientPlayerNetworkEvent.LoggingOut ignoredEvent) {
        backupProgress = BackupProgress.NONE;
        setDisabledOnThisServer(false);
    }

    public static @NotNull MutableComponent progressMessage() {
        return Component.translatable("ftbbackups3.lang.timer_progress", backupProgress.current() * 100 / backupProgress.total(), backupProgress.current(), backupProgress.total());
    }

    public static BackupProgress getBackupProgress() {
        return backupProgress;
    }

    public static void setDisabledOnThisServer(boolean disabled) {
        disabledOnServer = disabled;
        SidebarButtonManager.INSTANCE.getButton(FTBBackups.id("config")).ifPresent(btn -> btn.setForceHidden(disabled));
    }

    public static boolean isDisabledOnThisServer() {
        return disabledOnServer;
    }

    public record BackupProgress(int current, int total) {
        public static final BackupProgress NONE = new BackupProgress(0, 0);

        public boolean inProgress() {
            return total > 0;
        }

        public boolean finished() {
            return current == total;
        }
    }
}
