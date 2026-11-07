package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.Backup;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Set;
import java.util.stream.Collectors;

public record MinimumAgeRule(int age, Unit unit) implements RetentionRule {
    public static final ResourceLocation ID = FTBBackups.id("minimum_age");
    public static final Codec<MinimumAgeRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("age").forGetter(MinimumAgeRule::age),
            Codec.STRING.xmap(Unit::fromString, Unit::toString).fieldOf("unit").forGetter(MinimumAgeRule::unit)
    ).apply(instance, MinimumAgeRule::new));

    @Override
    public ResourceLocation id() {
        return ID;
    }

    /**
     * Any files outside the minimum age will are up for deletion. This rule does not guarantee that any files will be kept, it only guarantees that files younger than the minimum age will be kept.
     *
     * @param backups the list of backup objects to apply the retention rule to
     * @return the set of backup objects that should be kept
     */
    @Override
    public Set<Backup> computeToKeep(Set<Backup> backups) {
        var cutoff = LocalDateTime.now(ZoneOffset.UTC).minus(age, unit.unit);
        var cutoffMillis = cutoff.toInstant(ZoneOffset.UTC).toEpochMilli();

        return backups.stream()
                .filter(backup -> backup.time() >= cutoffMillis)
                .collect(Collectors.toSet());
    }

    @Override
    public Codec<? extends RetentionRule> codec() {
        return CODEC;
    }

    private enum Unit {
        SECONDS(Set.of("s", "sec"), ChronoUnit.SECONDS),
        MINUTES(Set.of("m", "min"), ChronoUnit.MINUTES),
        HOURS(Set.of("h", "hr"), ChronoUnit.HOURS),
        DAYS(Set.of("d", "day"), ChronoUnit.DAYS),
        WEEKS(Set.of("w", "wk"), ChronoUnit.WEEKS),
        MONTHS(Set.of("mo", "mon"), ChronoUnit.MONTHS),
        YEARS(Set.of("y", "yr"), ChronoUnit.YEARS);

        private final Set<String> aliases;
        private final TemporalUnit unit;

        Unit(Set<String> aliases, TemporalUnit unit) {
            this.aliases = aliases;
            this.unit = unit;
        }

        public static Unit fromString(String str) {
            for (Unit unit : values()) {
                // Try the direct name first. Then aliases if that fails.
                if (unit.name().equalsIgnoreCase(str)) {
                    return unit;
                }

                if (unit.aliases.contains(str.toLowerCase())) {
                    return unit;
                }
            }

            throw new IllegalArgumentException("Unknown unit: " + str);
        }

        public String toString() {
            return name().toLowerCase();
        }
    }
}
