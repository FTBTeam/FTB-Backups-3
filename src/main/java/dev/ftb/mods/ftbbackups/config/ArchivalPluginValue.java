package dev.ftb.mods.ftbbackups.config;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.StringValue;
import org.jspecify.annotations.Nullable;

public class ArchivalPluginValue extends StringValue {
    public ArchivalPluginValue(@Nullable Config config, String key, String def) {
        super(config, key, def);
    }

    @Override
    public @Nullable EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        group.add(key, new ArchivalPluginConfig(), get(), this::set, defaultValue);
        return null;
    }
}
