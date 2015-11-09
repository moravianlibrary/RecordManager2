SELECT ftm.fulltext
FROM dedup_record dr
  JOIN harvested_record hr ON hr.dedup_record_id = dr.id
  JOIN fulltext_monography ftm ON hr.id = ftm.harvested_record_id
WHERE dr.id = :dedupRecordId
ORDER BY ftm.harvested_record_id, ftm.order_in_monography