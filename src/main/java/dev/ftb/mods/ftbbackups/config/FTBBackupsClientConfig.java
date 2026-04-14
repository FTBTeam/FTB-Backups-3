package dev.ftb.mods.ftbbackups.config;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.EnumValue;
import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftblibrary.util.PanelPositioning;

public interface FTBBackupsClientConfig {
    String KEY = FTBBackups.MOD_ID + "-client";
    Config CONFIG = Config.create(KEY).standardTopLevelComment(FTBBackups.MOD_NAME, KEY, true);

    Config OVERLAY = CONFIG.addGroup("overlay");
    BooleanValue SHOW_OVERLAY = OVERLAY.addBoolean("show_overlay", true);
    EnumValue<PanelPositioning> OVERLAY_POS = OVERLAY.addEnum("overlay_pos", PanelPositioning.NAME_MAP, PanelPositioning.TOP_LEFT);
    IntValue OVERLAY_INSET_X = OVERLAY.addInt("overlay_inset_x", 4);
    IntValue OVERLAY_INSET_Y = OVERLAY.addInt("overlay_inset_y", 4);
}
