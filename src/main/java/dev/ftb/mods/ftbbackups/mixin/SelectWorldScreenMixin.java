package dev.ftb.mods.ftbbackups.mixin;

import dev.ftb.mods.ftbbackups.client.BackupsClient;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin {
    @Inject(method = "repositionElements", at = @At("RETURN"))
    void onRepositionElements(CallbackInfo ci) {
        // since SelectWorldScreen overrides repositionElement(), the Neo ScreenEvent.Init.Post event isn't fired
        BackupsClient.repositionRestoreButton();
    }
}
