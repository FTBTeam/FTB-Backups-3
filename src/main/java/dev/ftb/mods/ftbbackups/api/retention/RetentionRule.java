package dev.ftb.mods.ftbbackups.api.retention;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Set;

public interface RetentionRule {
    /**
     * @return the unique identifier for this retention rule
     */
    ResourceLocation id();

    /**
     * Consumes a list of backup file names and returns a list of backup file names that should be kept.
     *
     * @param backups the list of backup file names to apply the retention rule to
     * @return the set of backup file names that should be kept
     */
    Set<Path> apply(Set<Path> backups);

    Codec<? extends RetentionRule> codec();
}
