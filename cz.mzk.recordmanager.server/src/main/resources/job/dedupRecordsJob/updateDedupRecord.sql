UPDATE dedup_record dr SET updated = NOW() WHERE dr.id IN (
SELECT
  rd.id
FROM
  dedup_record rd JOIN
  record_link rl ON rl.dedup_record_id = rd.id JOIN
  harvested_record hr ON hr.id = rl.harvested_record_id
WHERE
  hr.deleted IS NOT NULL
)
