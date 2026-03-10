CREATE OR REPLACE VIEW first_harvest_by_region_year AS
WITH first_harvest AS (
    SELECT
        ic.id AS import_conf_id,
        l.region,
        ic.id_prefix,
        COALESCE(MIN(bje.end_time), MIN(to_param.date_val)) AS first_harvest_date
    FROM batch_job_instance bji
    JOIN batch_job_execution bje ON bji.job_instance_id = bje.job_instance_id
    JOIN batch_job_execution_params conf_id_param
        ON conf_id_param.job_execution_id = bje.job_execution_id
       AND conf_id_param.key_name = 'configurationId'
    LEFT JOIN batch_job_execution_params to_param
        ON to_param.job_execution_id = bje.job_execution_id
       AND to_param.key_name = 'to'
    LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
    JOIN import_conf ic ON ic.id = conf_id_param.long_val
    JOIN library l ON l.id = ic.library_id
    WHERE bji.job_name IN (
        'oaiHarvestJob',
        'oaiReharvestJob',
        'oaiPartitionedHarvestJob',
        'cosmotronHarvestJob',
        'oaiHarvestOneByOneJob',
        'importRecordsJob',
        'multiImportRecordsJob', 
        'importOaiRecordsJob', 
        'downloadAndImportRecordsJob', 
        'zakonyProLidiHarvestJob'
        -- sem můžeš přidat další joby
    )
      AND (ohc.set_spec IS NULL OR ohc.set_spec <> 'PLM')
      AND ic.id_prefix NOT LIKE 'kram-%'
      AND ic.id_prefix NOT LIKE 'sfx%'
      AND ic.id_prefix !='bookport'
      AND ic.id not in (554,557,613)
    GROUP BY ic.id, l.region, ic.id_prefix
),
src AS (
    SELECT
        EXTRACT(YEAR FROM first_harvest_date) AS rok,
        region,
        id_prefix
    FROM first_harvest
    WHERE first_harvest_date IS NOT NULL
)
SELECT
    rok::text,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PR'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PR'), ''),
            ')'
    ) AS pr,
    CONCAT(
            COUNT(*) FILTER (WHERE region='JM'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='JM'), ''),
            ')'
    ) AS jm,
    CONCAT(
            COUNT(*) FILTER (WHERE region='SC'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='SC'), ''),
            ')'
    ) AS sc,
    CONCAT(
            COUNT(*) FILTER (WHERE region='KV'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='KV'), ''),
            ')'
    ) AS kv,
    CONCAT(
            COUNT(*) FILTER (WHERE region='MS'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='MS'), ''),
            ')'
    ) AS ms,
    CONCAT(
            COUNT(*) FILTER (WHERE region='VY'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='VY'), ''),
            ')'
    ) AS vy,
    CONCAT(
            COUNT(*) FILTER (WHERE region='KH'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='KH'), ''),
            ')'
    ) AS kh,
    CONCAT(
            COUNT(*) FILTER (WHERE region='LI'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='LI'), ''),
            ')'
    ) AS li,
    CONCAT(
            COUNT(*) FILTER (WHERE region='JC'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='JC'), ''),
            ')'
    ) AS jc,
    CONCAT(
            COUNT(*) FILTER (WHERE region='OL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='OL'), ''),
            ')'
    ) AS ol,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PA'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PA'), ''),
            ')'
    ) AS pa,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PL'), ''),
            ')'
    ) AS pl,
    CONCAT(
            COUNT(*) FILTER (WHERE region='ZL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='ZL'), ''),
            ')'
    ) AS zl,
    CONCAT(
            COUNT(*) FILTER (WHERE region='US'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='US'), ''),
            ')'
    ) AS us,
    CONCAT(
            COUNT(*) FILTER (WHERE region='bibliography'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='bibliography'), ''),
            ')'
    ) AS bibliography,
    CONCAT(
            COUNT(*) FILTER (WHERE region IS NULL),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region IS NULL), ''),
            ')'
    ) AS no_region,
    CONCAT(
            COUNT(*),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix), ''),
            ')'
    ) AS all_regions
FROM src
GROUP BY rok
UNION ALL
-- řádek pro všechny roky dohromady
SELECT
    'ALL_YEARS' AS rok,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PR'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PR'), ''),
            ')'
    ) AS pr,
    CONCAT(
            COUNT(*) FILTER (WHERE region='JM'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='JM'), ''),
            ')'
    ) AS jm,
    CONCAT(
            COUNT(*) FILTER (WHERE region='SC'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='SC'), ''),
            ')'
    ) AS sc,
    CONCAT(
            COUNT(*) FILTER (WHERE region='KV'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='KV'), ''),
            ')'
    ) AS kv,
    CONCAT(
            COUNT(*) FILTER (WHERE region='MS'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='MS'), ''),
            ')'
    ) AS ms,
    CONCAT(
            COUNT(*) FILTER (WHERE region='VY'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='VY'), ''),
            ')'
    ) AS vy,
    CONCAT(
            COUNT(*) FILTER (WHERE region='KH'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='KH'), ''),
            ')'
    ) AS kh,
    CONCAT(
            COUNT(*) FILTER (WHERE region='LI'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='LI'), ''),
            ')'
    ) AS li,
    CONCAT(
            COUNT(*) FILTER (WHERE region='JC'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='JC'), ''),
            ')'
    ) AS jc,
    CONCAT(
            COUNT(*) FILTER (WHERE region='OL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='OL'), ''),
            ')'
    ) AS ol,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PA'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PA'), ''),
            ')'
    ) AS pa,
    CONCAT(
            COUNT(*) FILTER (WHERE region='PL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='PL'), ''),
            ')'
    ) AS pl,
    CONCAT(
            COUNT(*) FILTER (WHERE region='ZL'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='ZL'), ''),
            ')'
    ) AS zl,
    CONCAT(
            COUNT(*) FILTER (WHERE region='US'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='US'), ''),
            ')'
    ) AS us,
    CONCAT(
            COUNT(*) FILTER (WHERE region='bibliography'),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region='bibliography'), ''),
            ')'
    ) AS bibliography,
    CONCAT(
            COUNT(*) FILTER (WHERE region IS NULL),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix)
                     FILTER (WHERE region IS NULL), ''),
            ')'
    ) AS no_region,
    CONCAT(
            COUNT(*),
            ' (',
            COALESCE(STRING_AGG(id_prefix, ', ' ORDER BY id_prefix), ''),
            ')'
    ) AS all_regions
FROM src
ORDER BY rok;