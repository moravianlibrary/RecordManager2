DELETE FROM record_link WHERE harvested_record_id IN (
SELECT
  id
FROM
  harvested_record hr JOIN
  record_link rl ON rl.harvested_record_id = hr.id
WHERE
  hr.deleted IS NOT NULL
)
