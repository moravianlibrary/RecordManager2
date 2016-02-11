SELECT COALESCE(SUM(LENGTH(ftm.fulltext)), 0)
FROM dedup_record dr
  JOIN harvested_record hr ON hr.dedup_record_id = dr.id
  JOIN fulltext_monography ftm ON hr.id = ftm.harvested_record_id
WHERE dr.id = :dedupRecordId