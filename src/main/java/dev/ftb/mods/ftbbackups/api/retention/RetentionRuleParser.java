package dev.ftb.mods.ftbbackups.api.retention;

@FunctionalInterface
public interface RetentionRuleParser {
    RetentionRule parse(String... rule);
}
