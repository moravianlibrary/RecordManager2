-- procedure used for creating record_link entries. 
-- ARGUMENTS:
--   dr_id identifier or dedup_record (Note: value '0' means new dedup_record entry will be created)
--   hr_id identifier of harvested_record
-- RETURN
--    numeric identifier of used dedup_record
CREATE or REPLACE FUNCTION update_record_links(IN dr_id record_link.dedup_record_id%TYPE, IN hr_id record_link.harvested_record_id%TYPE) RETURNS numeric AS '
  DECLARE
    rl_rec RECORD;
    current_hr_id record_link.harvested_record_id%TYPE;
    final_dr_id record_link.dedup_record_id%TYPE;
  BEGIN
    IF dr_id = 0 THEN
      INSERT INTO dedup_record(updated) VALUES (now()) RETURNING id INTO final_dr_id;
    ELSE
      final_dr_id := dr_id;
    END IF;
    SELECT * INTO rl_rec FROM record_link WHERE dedup_record_id = final_dr_id AND harvested_record_id = hr_id;
      IF NOT FOUND THEN
        INSERT INTO record_link(harvested_record_id, dedup_record_id, created) VALUES (hr_id, final_dr_id, now());
        UPDATE dedup_record SET updated = now() WHERE id = final_dr_id;
      END IF;
    RETURN final_dr_id;
  END;
' LANGUAGE plpgsql;