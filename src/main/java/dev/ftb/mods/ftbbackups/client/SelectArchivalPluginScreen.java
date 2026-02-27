package dev.ftb.mods.ftbbackups.client;

import dev.ftb.mods.ftbbackups.archival.ArchivePluginManager;
import dev.ftb.mods.ftbbackups.config.ArchivalPluginConfig;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.icon.Color4IRenderer;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SelectArchivalPluginScreen extends AbstractButtonListScreen {
    private final ArchivalPluginConfig config;
    private final ConfigCallback callback;

    public SelectArchivalPluginScreen(ArchivalPluginConfig config, ConfigCallback callback) {
        this.config = config;
        this.callback = callback;

        setTitle(Component.translatable("ftbbackups3.general.archival_plugin"));
        showBottomPanel(false);
        showCloseButton(true);
    }

    @Override
    public void addButtons(Panel panel) {
        ArchivePluginManager.clientInstance().plugins().keySet()
                .forEach(k -> panel.add(new PluginButton(panel, k)));
    }

    @Override
    public boolean onClosedByKey(Key key) {
        if (super.onClosedByKey(key)) {
            callback.save(false);
            return true;
        }

        return false;
    }

    @Override
    protected void doCancel() {
        callback.save(false);
    }

    @Override
    protected void doAccept() {
        callback.save(true);
    }

    private class PluginButton extends SimpleTextButton {
        private final Identifier value;

        PluginButton(Panel panel, Identifier value) {
            super(panel, Component.literal(value.toString()), Color4I.empty());
            this.value = value;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            if (isMouseOver) {
                Color4IRenderer.INSTANCE.render(Color4I.WHITE.withAlpha(30), graphics, x, y, w, h);
            }
            Color4IRenderer.INSTANCE.render(Color4I.GRAY.withAlpha(40), graphics, x, y + h, w, 1);
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            playClickSound();
            if (config.updateValue(value.toString())) {
                callback.save(true);
            }
        }
    }
}
