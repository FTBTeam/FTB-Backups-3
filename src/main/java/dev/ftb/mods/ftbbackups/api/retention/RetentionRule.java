package dev.ftb.mods.ftbbackups.api.retention;

import dev.ftb.mods.ftbbackups.api.Backup;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface RetentionRule {
    /**
     * @return the unique identifier for this retention rule
     */
    ResourceLocation id();

    /**
     * Consumes a list of backup objects and returns a list of backup objects that should be kept.
     *
     * @param backups the list of backup objects to apply the retention rule to
     * @return the set of backup objects that should be kept
     */
    Set<Backup> computeToKeep(Set<Backup> backups);

    Codec<? extends RetentionRule> codec();
}
