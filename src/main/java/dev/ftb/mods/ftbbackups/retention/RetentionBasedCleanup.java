package dev.ftb.mods.ftbbackups.retention;

import dev.ftb.mods.ftbbackups.api.Backup;
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
        var rules = FTBBackupsServerConfig.RETENTION_POLICIES.get().values();
        if (rules.isEmpty()) {
            return; // No retention rules means we don't delete anything
        }

        Set<Backup> allBackups = Set.of(); // This should be populated with the actual backup files in the backupPath

        Set<Backup> backupsToKeep = new HashSet<>();
        for (RetentionRule rule : rules) {
            backupsToKeep.addAll(rule.computeToKeep(allBackups));
        }

        System.out.println("Files to keep: " + backupsToKeep);
        // TOOD: Implement the logic to delete files that are not in filesToKeep from backupPath
    }
}
