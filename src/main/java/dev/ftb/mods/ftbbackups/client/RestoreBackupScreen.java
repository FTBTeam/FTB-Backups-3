package dev.ftb.mods.ftbbackups.client;

import dev.ftb.mods.ftbbackups.Backups;
import dev.ftb.mods.ftbbackups.api.Backup;
import dev.ftb.mods.ftbbackups.api.IArchivalPlugin;
import dev.ftb.mods.ftbbackups.archival.ArchivePluginManager;
import dev.ftb.mods.ftbbackups.config.FTBBackupsServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class RestoreBackupScreen extends Screen {
    private Button restoreButton;
    private BackupsList backupsList;
    private EditBox searchBox;
    private long totalFiles;
    private final AtomicLong currentFile = new AtomicLong(0L);
    private String restoringPath = "";
    private int completedTimer = 0;
    private String lastError = "";

    private static final Identifier SUCCESS = Identifier.withDefaultNamespace("pending_invite/accept");
    private static final Identifier FAILURE = Identifier.withDefaultNamespace("pending_invite/reject");

    static final int UPPER_HEIGHT = 80;
    static final int LOWER_HEIGHT = 40;
    private final Screen prevScreen;

    protected RestoreBackupScreen(Screen prevScreen, Component title) {
        super(title);

        this.prevScreen = prevScreen;
    }

    @Override
    protected void init() {
        super.init();

        backupsList = new BackupsList(minecraft, width, height - UPPER_HEIGHT - LOWER_HEIGHT, UPPER_HEIGHT);

        searchBox = new EditBox(font, width / 2 - 160 / 2, 40, 160, 20, Component.empty());
        searchBox.setResponder(backupsList::onFilterChanged);

        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, btn -> onClose())
                .size(100, 20).pos(width / 2 - 130, height - 30).build());

        restoreButton = Button.builder(Component.translatable("ftbbackups3.gui.restore_now"), btn -> onActivate())
                .size(150, 20).pos(width / 2 - 20, height - 30).build();
        addRenderableWidget(restoreButton);
        restoreButton.active = false;

        addRenderableWidget(searchBox);
        addRenderableWidget(backupsList);
    }

    private void onActivate() {
        BackupsList.Entry selected = backupsList.getSelected();

        if (selected != null) {
            minecraft.setScreen(new ConfirmScreen(confirmed -> doRestore(confirmed, selected),
                    Component.translatable("ftbbackups3.gui.confirm_restore", selected.backup.worldName(), selected.backup.fileId()),
                    Component.translatable("ftbbackups3.gui.confirm_restore.line_2"))
            );
        }
    }

    private void doRestore(boolean confirmed, BackupsList.Entry selected) {
        minecraft.setScreen(this);

        if (confirmed) {
            Path backupsFolder = FTBBackupsServerConfig.getBackupFolder();

            Backup backup = selected.backup;

            IArchivalPlugin plugin = ArchivePluginManager.clientInstance().getPlugin(backup.archivalPlugin());

            if (plugin != null) {
                CompletableFuture.runAsync(() -> {
                    IArchivalPlugin.RestorationContext ctx = new RestoreContext(backupsFolder.resolve(backup.fileId()), FMLPaths.GAMEDIR.get(), Backups.LOGGER, this);
                    totalFiles = backup.fileCount();
                    currentFile.set(0L);
                    Backups.LOGGER.info("STUB: do restore! {} -> {}", ctx.archivePath(), ctx.destinationFolder());
                    try {
                        plugin.restoreArchive(ctx);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).whenComplete((unused, ex) -> {
                    if (ex != null) {
                        Backups.LOGGER.error("backup restoration failed: {} -> {}", ex.getClass(), ex.getMessage());
                        lastError = ex.getMessage();
                    } else {
                        Backups.LOGGER.info("backup restoration complete!");
                        lastError = "";
                    }
                    totalFiles = 0L;
                    currentFile.set(0L);
                    restoringPath = "";
                    completedTimer = 50;
                });
            } else {
                Backups.LOGGER.info("unknown archival plugin {}!", backup.archivalPlugin());
            }
        }
    }

    @Override
    protected void setInitialFocus() {
        setInitialFocus(searchBox);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(prevScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return totalFiles == 0L && super.shouldCloseOnEsc();
    }

    @Override
    public void tick() {
        super.tick();

        if (completedTimer > 0) {
            completedTimer--;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);

        renderExtractionProgress(guiGraphics, totalFiles);
    }

    private void renderExtractionProgress(GuiGraphics guiGraphics, long total) {
        if (total == 0L && completedTimer == 0) {
            return;
        }

        int pw = guiGraphics.guiWidth() * 3 / 4;
        int ph = guiGraphics.guiHeight() / 4;
        int px = (guiGraphics.guiWidth() - pw) / 2;
        int py = (guiGraphics.guiHeight() - ph) / 2;

        guiGraphics.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF404040);
        guiGraphics.fill(px, py, px + pw, py + ph, 0xFFC0C0C0);

        if (total > 0L) {
            guiGraphics.drawString(font, Component.translatable("ftbbackups3.gui.restore.in_progress"), px + 5, py + 5, 0xFF404040, false);
            guiGraphics.drawString(font, Component.literal(restoringPath), px + 5, py + ph - font.lineHeight - 5, 0xFF404040, false);

            // progress bar
            int sh = ph / 4;
            int sy = py + (ph - sh) / 2;
            guiGraphics.fill(px + 10, sy, px + pw - 10, sy + sh, 0xFF202020);
            int sw = (int) ((pw - 24) * currentFile.get() / total);
            guiGraphics.fill(px + 12, sy + 2, px + 10 + sw, sy + sh - 2, 0xFF0040C0);
        } else if (completedTimer > 0) {
            if (lastError.isEmpty()) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SUCCESS, px + 10, py + (ph - 32) / 2, 32, 32, 0xFFFFFFFF);
                guiGraphics.drawString(font, Component.translatable("ftbbackups3.gui.restore.success"), px + 50, py + (ph - font.lineHeight) / 2, 0xFF404040, false);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, FAILURE, px + 10, py + (ph - 32) / 2, 32, 32, 0xFFFFFFFF);
                guiGraphics.drawString(font, Component.translatable("ftbbackups3.gui.restore.failure", lastError), px + 50, py + (ph - font.lineHeight) / 2, 0xFFC04040, false);
            }
        }
    }

    private class BackupsList extends AbstractSelectionList<BackupsList.Entry> {
        public BackupsList(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 30);

            addChildren("");
        }

        private void addChildren(String filter) {
            String filterL = filter.toLowerCase(Locale.ROOT);
            List<Entry> entries = Backups.getClientInstance().backups().stream()
                    .filter(b -> b.success() && b.fileCount() > 0
                            && (filter.isEmpty() || b.worldName().toLowerCase(Locale.ROOT).contains(filterL))
                    )
                    .map(Entry::new)
                    .sorted((o1, o2) -> o2.backup.compareTo(o1.backup))
                    .toList();

            // TODO controls to alter sort order
            replaceEntries(entries);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput ignored) {
        }

        @Override
        public int getRowWidth() {
            return 400;
        }

        @Override
        protected int scrollBarX() {
            return width / 2 + 200;
        }

        @Override
        public void setSelected(@Nullable RestoreBackupScreen.BackupsList.Entry entry) {
            RestoreBackupScreen.this.restoreButton.active = entry != null;
            super.setSelected(entry);
        }

        private void onFilterChanged(String filter) {
            addChildren(filter);
        }

        private class Entry extends AbstractSelectionList.Entry<Entry> {
            private static final String DATE_FORMAT = "MMM d, yyyy HH:mm a";

            private final Backup backup;

            public Entry(Backup backup) {
                this.backup = backup;
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
                BackupsList.this.setSelected(this);

                if (isDoubleClick) {
                    RestoreBackupScreen.this.onActivate();
                    return true;
                }

                return super.mouseClicked(event, false);
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int x, int y, boolean mouseOver, float partialTick) {
                Font font = Minecraft.getInstance().font;

                int startX = getContentX() + 80;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                ZonedDateTime ldt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(backup.time()), ZoneId.systemDefault());

                int y0 = getContentY() + 5;
                int lh = font.lineHeight + 2;

                guiGraphics.drawString(font, Component.translatable(backup.worldName()), startX, y0, 0xFFFFFFFF);
                guiGraphics.drawString(font, Component.literal(ldt.format(formatter)), startX + 10, y0 + lh, 0xFFC0C0C0);

                if (mouseOver) {
                    guiGraphics.setTooltipForNextFrame(font, List.of(
                            Component.translatable("ftbbackups3.gui.list.file",
                                    Component.literal(backup.fileId()).withStyle(ChatFormatting.GRAY)),
                            Component.translatable("ftbbackups3.gui.list.plugin",
                                    Component.literal(backup.archivalPlugin().toString()).withStyle(ChatFormatting.GRAY))
                    ), Optional.empty(), x, y);
                }
            }
        }
    }

    private record RestoreContext(Path archivePath, Path destinationFolder, Logger logger, RestoreBackupScreen screen) implements IArchivalPlugin.RestorationContext {
        @Override
        public void notifyProcessingFile(String filename) {
            screen.currentFile.incrementAndGet();
            screen.restoringPath = filename;
            logger.debug("restore file: {}", filename);
        }
    }
}
