package dev.ftb.mods.ftbbackups.config;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetentionRuleListValue extends BaseValue<Map<ResourceLocation, RetentionRule>> {
    public RetentionRuleListValue(@Nullable SNBTConfig config, String key, Map<ResourceLocation, RetentionRule> def) {
        super(config, key, def);
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: " + defaultValue);
        tag.comment(key, String.join("\n", s));

        CompoundTag values = new CompoundTag();
        for (Map.Entry<ResourceLocation, RetentionRule> entry : get().entrySet()) {
            RetentionRule rule = entry.getValue();
            CompoundTag ruleOptions = encodeRule(rule);
            values.put(entry.getKey().toString(), ruleOptions);
        }
        tag.put(key, values);
    }

    @Override
    public void read(SNBTCompoundTag snbtCompoundTag) {
        Map<ResourceLocation, RetentionRule> rules = new HashMap<>();

        var values = snbtCompoundTag.getCompound(key);
        for (String key : values.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null) {
                throw new IllegalArgumentException("Invalid retention rule ID: " + key);
            }

            var value = values.getCompound(key);
            Codec<? extends RetentionRule> codec = FTBBackups.retentionRegistry().fromId(id);
            if (codec == null) {
                throw new IllegalArgumentException("No codec registered for retention rule ID: " + id);
            }

            RetentionRule rule = codec.parse(NbtOps.INSTANCE, value).getOrThrow();
            if (rule == null) {
                throw new IllegalArgumentException("Failed to decode retention rule for ID: " + id);
            }

            rules.put(id, rule);
        }

        set(rules);
    }

    private static <T extends RetentionRule> CompoundTag encodeRule(T rule) {
        @SuppressWarnings("unchecked")
        Codec<T> codec = (Codec<T>) rule.codec();
        return (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, rule).getOrThrow();
    }
}
