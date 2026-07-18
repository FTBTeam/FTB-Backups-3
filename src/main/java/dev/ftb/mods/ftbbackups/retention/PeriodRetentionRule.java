package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.Backup;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public record PeriodRetentionRule(Period period, int count) implements RetentionRule {
    public static final ResourceLocation ID = FTBBackups.id("period");
    public static final Codec<PeriodRetentionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // TODO: A swear there is a codec for enums.
            Codec.STRING.xmap(Period::fromString, Period::toString).fieldOf("period").forGetter(PeriodRetentionRule::period),
            Codec.INT.fieldOf("count").forGetter(PeriodRetentionRule::count)
    ).apply(instance, PeriodRetentionRule::new));

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Set<Backup> computeToKeep(Set<Backup> backups) {
        var byPeriod = groupedByPeriod(backups);

        Set<Backup> toKeep = new HashSet<>();
        for (List<Backup> backupsForPeriod : byPeriod.values()) {
            for (int i = 0; i < Math.min(count, backupsForPeriod.size()); i++) {
                toKeep.add(backupsForPeriod.get(i));
            }
        }

        return toKeep;
    }

    @Override
    public Codec<? extends RetentionRule> codec() {
        return CODEC;
    }

    /**
     * Groups the given files by the specified period and returns a map where the key is the period number and the value
     * is the latest backup file name for that period.
     */
    private Map<String, List<Backup>> groupedByPeriod(Set<Backup> backups) {
        Map<String, List<Pair<LocalDateTime, Backup>>> grouped = new HashMap<>();

        for (Backup backup : backups) {
            var timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(backup.time()), ZoneId.systemDefault());

            // Determine the period number based on the specified period type
            String periodNumber = period.toKeyable(timestamp);
            grouped.computeIfAbsent(periodNumber, k -> new ArrayList<>()).add(Pair.of(timestamp, backup));
        }

        // Now, for each group, we should order the backups by their timestamp so the latest one is first.
        return grouped.entrySet().stream()
                .collect(HashMap::new, (m, e) -> {
                    e.getValue().sort((a, b) -> b.left().compareTo(a.left())); // Sort descending by timestamp
                    m.put(e.getKey(), e.getValue().stream().map(Pair::right).toList());
                }, HashMap::putAll);
    }

    public enum Period {
        DAILY(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        WEEKLY(DateTimeFormatter.ofPattern("YYYY-ww")),
        MONTHLY(DateTimeFormatter.ofPattern("yyyy-MM")),
        YEARLY(DateTimeFormatter.ofPattern("yyyy"));

        private static final List<Period> VALUES = List.of(Period.values());

        /**
         * Formatter that provides a string representation of the period in a year/month/day unique format to avoid overlaps
         */
        private final DateTimeFormatter formatter;

        Period(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public static Period fromString(String period) {
            return VALUES.stream()
                    .filter(p -> p.toString().equalsIgnoreCase(period))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid period: " + period));
        }

        /**
         * Returns a string representation of the period that can be used as a key for grouping backups.
         */
        public String toKeyable(LocalDateTime timestamp) {
            return this.formatter.format(timestamp);
        }

        public String toString() {
            return this.name().toLowerCase();
        }

        public static List<String> getAllPeriods() {
            return VALUES.stream().map(Period::toString).toList();
        }
    }
}
