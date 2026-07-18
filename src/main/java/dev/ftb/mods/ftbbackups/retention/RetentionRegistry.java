package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.api.retention.RetentionRuleParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

public class RetentionRegistry {
    private final HashMap<ResourceLocation, RetentionRuleParser> retentionRules = new HashMap<>();
    private boolean initialized = false;

    @ApiStatus.Internal
    public void init() {
        if (initialized) {
            throw new IllegalStateException("RetentionRegistry is already initialized");
        }

        initialized = true;
        register(LatestRetentionRule.ID, LatestRetentionRule::parse);
        register(PeriodRetentionRule.ID, PeriodRetentionRule::parse);
    }

    public void register(ResourceLocation id, RetentionRuleParser parser) {
        if (retentionRules.containsKey(id)) {
            throw new IllegalArgumentException("Retention rule with id " + id + " is already registered");
        }

        retentionRules.put(id, parser);
    }

    public RetentionRuleParser fromId(ResourceLocation id) {
        return retentionRules.get(id);
    }
}
