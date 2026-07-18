package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

public record PeriodRetentionRule(Period period, int count) implements RetentionRule {
    public static final ResourceLocation ID = FTBBackups.id("period");
    public static final Codec<PeriodRetentionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // TODO: A swear there is a codec for enums.
            Codec.STRING.xmap(Period::fromString, Period::toString).fieldOf("period").forGetter(PeriodRetentionRule::period),
            Codec.INT.fieldOf("count").forGetter(PeriodRetentionRule::count)
    ).apply(instance, PeriodRetentionRule::new));

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Set<Path> apply(Set<Path> backups) {
        var byPeriod = groupedByPeriod(backups);

        Set<Path> toKeep = new HashSet<>();
        for (Map.Entry<Integer, List<Path>> byPeriodEntry : byPeriod.entrySet()) {
            var backupsForPeriod = byPeriodEntry.getValue();
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
    private Map<Integer, List<Path>> groupedByPeriod(Set<Path> backups) {
        Map<Integer, List<Pair<LocalDateTime, Path>>> grouped = new HashMap<>();

        for (Path backup : backups) {
            var timestamp = parseTimestampFromFile(backup.getFileName().toString());

            // Determine the period number based on the specified period type
            int periodNumber = period.toKeyable(timestamp);
            grouped.computeIfAbsent(periodNumber, k -> new ArrayList<>()).add(Pair.of(timestamp, backup));
        }

        // Now, for each group, we should order the backups by their timestamp so the latest one is first.
        return grouped.entrySet().stream()
                .collect(HashMap::new, (m, e) -> {
                    e.getValue().sort((a, b) -> b.left().compareTo(a.left())); // Sort descending by timestamp
                    m.put(e.getKey(), e.getValue().stream().map(Pair::right).toList());
                }, HashMap::putAll);
    }

    public static LocalDateTime parseTimestampFromFile(String backup) {
        // Converts: 2026-04-08-17-54-53.extension to a date;
        String[] parts = backup.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid backup file name: " + backup);
        }

        try {
            return FORMATTER.parse(parts[0], LocalDateTime::from);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timestamp in backup file name: " + backup, e);
        }
    }

    public enum Period {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY;

        private static final List<Period> VALUES = List.of(Period.values());

        public static Period fromString(String period) {
            return VALUES.stream()
                    .filter(p -> p.toString().equalsIgnoreCase(period))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid period: " + period));
        }

        /**
         * Converts a LocalDateTime to an integer that can be used as a key for grouping backups by period. The year and
         * other relevant data is merged into the int to ensure uniqueness across years.
         *
         * @param timestamp the LocalDateTime to convert
         * @return an integer representing the period key
         */
        public int toKeyable(LocalDateTime timestamp) {
            return switch (this) {
                case DAILY -> timestamp.getYear() * 1000 + timestamp.getDayOfYear();
                case WEEKLY -> timestamp.get(WeekFields.ISO.weekBasedYear()) * 100 + timestamp.get(WeekFields.ISO.weekOfWeekBasedYear());
                case MONTHLY -> timestamp.getYear() * 100 + timestamp.getMonthValue();
                case YEARLY -> timestamp.getYear();
            };
        }

        public String toString() {
            return this.name().toLowerCase();
        }

        public static List<String> getAllPeriods() {
            return VALUES.stream().map(Period::toString).toList();
        }
    }
}
