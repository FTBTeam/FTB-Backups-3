package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Set;

public record LatestRetentionRule(int count) implements RetentionRule {
    public static final ResourceLocation ID = FTBBackups.id("latest");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Set<Path> apply(Set<Path> backups) {
        return Set.of();
    }

    @Override
    public String asString() {
        return "latest: " + count;
    }

    public static LatestRetentionRule parse(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid number of arguments for latest retention rule. Expected 1, got " + args.length);
        }

        try {
            int count = Integer.parseInt(args[0]);
            return new LatestRetentionRule(count);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid count for latest retention rule: " + args[0]);
        }
    }
}
