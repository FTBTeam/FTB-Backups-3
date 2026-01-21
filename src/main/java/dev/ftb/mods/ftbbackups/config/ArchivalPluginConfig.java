package dev.ftb.mods.ftbbackups.config;

import dev.ftb.mods.ftbbackups.client.SelectArchivalPluginScreen;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;

public class ArchivalPluginConfig extends EditableConfigValue<String> {
    @Override
    public void onClicked(Widget widget, MouseButton mouseButton, ConfigCallback configCallback) {
        if (getCanEdit()) {
            new SelectArchivalPluginScreen(this, configCallback).openGui();
        }
    }
}
