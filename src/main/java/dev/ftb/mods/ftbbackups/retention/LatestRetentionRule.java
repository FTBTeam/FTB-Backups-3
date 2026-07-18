package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.Backup;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public record LatestRetentionRule(int count) implements RetentionRule {
    public static final ResourceLocation ID = FTBBackups.id("latest");

    public static final Codec<LatestRetentionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("count").forGetter(LatestRetentionRule::count)
    ).apply(instance, LatestRetentionRule::new));

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Set<Backup> computeToKeep(Set<Backup> backups) {
        return backups.stream()
                .sorted(Comparator.comparing(Backup::time).reversed())
                .limit(count)
                .collect(Collectors.toSet());
    }

    @Override
    public Codec<? extends RetentionRule> codec() {
        return CODEC;
    }
}
