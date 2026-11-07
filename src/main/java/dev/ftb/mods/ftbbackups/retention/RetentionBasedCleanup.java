package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.api.Backup;
import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import dev.ftb.mods.ftbbackups.config.FTBBackupsServerConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RetentionBasedCleanup {
    /**
     * Run over each of the retention rules and apply them to the backup path.
     * Hold a list of files we'll keep, and files we remove. If a file is kept by one rule but removed by another, it should be kept.
     */
    public static void apply(List<Backup> backups) {
        var rules = getApplicableRules();
        if (rules.isEmpty()) {
            return; // No retention rules means we don't delete anything
        }

        Set<Backup> backupsToKeep = new HashSet<>();
        for (RetentionRule rule : rules) {
            backupsToKeep.addAll(rule.computeToKeep(backups));
        }

        System.out.println("Files to keep: " + backupsToKeep);
        Set<Backup> backupsToDelete = new HashSet<>(backups);
        backupsToDelete.removeAll(backupsToKeep);
        System.out.println("Files to delete: " + backupsToDelete);

        // TODO: Actually delete the files in backupsToDelete from the backupPath
        // TODO: Implement builtin rule to prune when the total space is too low to hold new backups.
    }

    /**
     * Either get the user defined retention rules, or implement a default rule that keeps the latest N backups, where N is defined in the config.
     * @return a collection of retention rules to apply
     */
    private static Collection<RetentionRule> getApplicableRules() {
        if (FTBBackupsServerConfig.USE_RETENTION_POLICIES.get()) {
            return FTBBackupsServerConfig.RETENTION_POLICIES.get().values();
        }

        Integer backupsToKeep = FTBBackupsServerConfig.BACKUPS_TO_KEEP.get();
        if (backupsToKeep == 0) {
            return List.of(); // 0 means keep all backups
        }

        return List.of(new LatestRetentionRule(backupsToKeep));
    }
}
