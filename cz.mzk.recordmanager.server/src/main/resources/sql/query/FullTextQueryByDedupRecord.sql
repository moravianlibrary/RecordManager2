SELECT ftk.fulltext
FROM dedup_record dr
  JOIN harvested_record hr ON hr.dedup_record_id = dr.id
  JOIN fulltext_kramerius ftk ON hr.id = ftk.harvested_record_id
WHERE dr.id = :dedupRecordId
ORDER BY ftk.harvested_record_id, ftk.order_in_document
