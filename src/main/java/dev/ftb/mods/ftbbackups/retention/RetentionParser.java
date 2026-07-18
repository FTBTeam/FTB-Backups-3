package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.FTBBackups;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import net.minecraft.resources.ResourceLocation;

public class RetentionParser {
    /**
     * Parses the input string and returns a RetentionRule if possible.
     * <p>
     * Supported formats are:
     * - "ftbbackups:latest <count>" for LatestRetentionRule
     * - "ftbbackups:period <period> [count]" for PeriodRetentionRule
     *
     * @return A RetentionRule object if the input is valid, null otherwise.
     */
    public static RetentionRule parse(String input) {
        var parts = input.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid retention rule format. Expected 'type ...args', got '" + input + "'");
        }

        var type = parts[0].trim().toLowerCase();
        var args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length); // slice the array to get the arguments

        var typeAsId = ResourceLocation.tryParse(type);
        if (typeAsId == null) {
            throw new IllegalArgumentException("Invalid retention rule type: " + type);
        }

        var parser = FTBBackups.retentionRegistry().fromId(typeAsId);
        if (parser == null) {
            throw new IllegalArgumentException("Unknown retention rule type: " + type);
        }

        return parser.parse(args);
    }
}
