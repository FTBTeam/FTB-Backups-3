# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.4]

### Added
* Added "excluded_files" server config setting to specify files which should be in the backup archive
  * Use the standard Java PathMatcher system; entries should be either "glob:<file-glob>" or "regex:<regular-expression>" 
  * By default, includes "glob:session.lock" and "glob:*.neoforge-tmp"

### Fixed
* The ZIP archiver no longer aborts if any file can't be included (now just logs an error and continues)

## [21.1.3]

### Fixed
* Allow clients to connect to server if mod is disabled (FTB_BACKUPS_DISABLED) on server but not client

## [21.1.2]

### Added
* Added support for disabling mod via environment variable 
  * If "FTB_BACKUPS_DISABLED" is set (to anything), the mod does nothing
  * Note: FTB Library 2101.1.15+ is needed to hide the sidebar button
  * Intended for use if some hosted backup solution is in use and FTB Backups 3 is already in the modpack

### Fixed
* Fix for restoring SSP worlds which have been completely deleted
* Fixed "unknown backup plugin" error when doing forced backups on server shutdown

## [21.1.1]

### Added
* Initial release of FTB Backups 3, a major rewrite and update for Minecraft 1.21.1 of the original FTB Backups mod
  * Note that this mod is not related to FTB Backups 2 in any way
