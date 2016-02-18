SELECT COALESCE(SUM(LENGTH(ftk.fulltext)), 0)
FROM dedup_record dr
  JOIN harvested_record hr ON hr.dedup_record_id = dr.id
  JOIN fulltext_kramerius ftk ON hr.id = ftk.harvested_record_id
WHERE dr.id = :dedupRecordId
