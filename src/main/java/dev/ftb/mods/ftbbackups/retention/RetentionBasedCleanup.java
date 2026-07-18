package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import dev.ftb.mods.ftbbackups.config.FTBBackupsServerConfig;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class RetentionBasedCleanup {
    /**
     * Run over each of the retention rules and apply them to the backup path.
     * Hold a list of files we'll keep, and files we remove. If a file is kept by one rule but removed by another, it should be kept.
     *
     * @param backupPath the path to the backup directory
     */
    public void apply(Path backupPath) {
        var rules = FTBBackupsServerConfig.RETENTION_POLICIES.get();
        if (rules.isEmpty()) {
            return; // No retention rules means we don't delete anything
        }

        Set<Path> allBackups = Set.of(); // This should be populated with the actual backup files in the backupPath

        Set<Path> filesToKeep = new HashSet<>();
        for (RetentionRule rule : rules) {
            filesToKeep.addAll(rule.apply(allBackups));
        }

        System.out.println("Files to keep: " + filesToKeep);
        // TOOD: Implement the logic to delete files that are not in filesToKeep from backupPath
    }
}
