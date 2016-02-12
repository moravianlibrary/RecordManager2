/**
 * Package handling all deduplication tasks.
 * 
 * Deduplication is process of mapping {@link cz.mzk.recordmanager.server.model.HarvestedRecord} 
 * into {@link cz.mzk.recordmanager.server.model.DedupRecord}.
 * This relationship is {1..N} to 1.
 * 
 * Whole deduplication is Spring Batch job consisting of many steps, 
 * see {@link cz.mzk.recordmanager.server.dedup.DedupRecordsJobConfig}.
 */
package cz.mzk.recordmanager.server.dedup;