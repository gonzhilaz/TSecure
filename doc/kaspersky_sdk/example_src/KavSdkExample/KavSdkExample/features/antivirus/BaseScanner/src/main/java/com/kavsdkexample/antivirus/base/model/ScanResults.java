/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

public interface ScanResults {
    /**
     * Get UTC time when scan was started
     * @return the difference, measured in milliseconds, between
     *         the time when scan was started and midnight, January 1, 1970 UTC.
     */
    long getScanStartedTime();

    /**
     * Get UTC time when scan was finished
     * @return the difference, measured in milliseconds, between
     *         the time when scan was finished and midnight, January 1, 1970 UTC.
     */
    long getScanFinishedTime();

    /**
     * Get total count of files that were scanned (including skipped). Archive counted as a single file
     * @return count of files that were scanned
     */
    int  getScannedFilesCount();

    /**
     * Get total count of objects that were scanned (including skipped).
     * Generic file is also an object. Archive is an object and its contents is also objects.
     * @return count of objects that were scanned
     */
    int  getScannedObjectsCount();

    /**
     * Get total count of objects that were skipped for some reason (unable to read, excluded by scanner settings and so on)
     * @return count of skipped objects
     */
    int  getSkippedObjectsCount();

    /**
     * Get total count of detected threats during scan
     * @return count of detected threats
     */
    int  getDetectedThreatsCount();

    /**
     * Get total count of malware threats. This is subset from {@link #getDetectedThreatsCount()}
     * @return count of malware threats
     */
    int  getCountOfMalwareThreats();

    /**
     * Get total count of riskware threats (subset from {@link #getDetectedThreatsCount()}).
     * This is not malware. It is usually some potentially unwanted software,
     * that aggressively shows ads and does not have any useful functionality, spying applications
     * and so on/
     * @return count of riskware threats
     */
    int  getCountOfAdwareAndRiskwareThreats();

    /**
     * Get count of files (threats) that were deleted during scan
     * @return count of removed threats
     */
    int  getCountOfDeletedFiles();

    /**
     * Get count of files (threats) that were quarantined during scan
     * @return count of quarantined threats
     */
    int  getCountOfQuarantinedFiles();
}
