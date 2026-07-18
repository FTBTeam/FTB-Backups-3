package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

public class RetentionRegistry {
    private final HashMap<ResourceLocation, Codec<? extends RetentionRule>> retentionRules = new HashMap<>();
    private boolean initialized = false;

    @ApiStatus.Internal
    public void init() {
        if (initialized) {
            throw new IllegalStateException("RetentionRegistry is already initialized");
        }

        initialized = true;
        register(LatestRetentionRule.ID, LatestRetentionRule.CODEC);
        register(PeriodRetentionRule.ID, PeriodRetentionRule.CODEC);
    }

    public void register(ResourceLocation id, Codec<? extends RetentionRule> codec) {
        if (retentionRules.containsKey(id)) {
            throw new IllegalArgumentException("Retention rule with id " + id + " is already registered");
        }

        retentionRules.put(id, codec);
    }

    public Codec<? extends RetentionRule> fromId(ResourceLocation id) {
        return retentionRules.get(id);
    }
}
