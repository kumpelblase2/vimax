ALTER TABLE video ADD COLUMN metadata_values TEXT;
UPDATE video SET metadata_values = (SELECT json_group_object(definition_id, json(value)) FROM (SELECT * FROM video_metadata vm
WHERE vm.video_id = video.id));

DROP TABLE video_metadata;
