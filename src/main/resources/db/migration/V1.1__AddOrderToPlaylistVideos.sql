/* We do this to have the best chance at keeping the same order. */
ALTER TABLE playlist_videos
    ADD position INTEGER DEFAULT 0;

UPDATE playlist_videos
SET position =
        (SELECT rowid FROM playlist_videos pv WHERE pv.playlist_id = playlist_videos.playlist_id AND pv.videos_id = playlist_videos.videos_id) -
        (SELECT min(rowid) FROM playlist_videos pv WHERE pv.playlist_id = playlist_videos.playlist_id);

CREATE TEMPORARY TABLE playlist_videos_bak(playlist_id INTEGER, video_id INTEGER, position INTEGER);
INSERT INTO playlist_videos_bak SELECT playlist_id, videos_id as video_id, position FROM playlist_videos;
DROP TABLE playlist_videos;
CREATE TABLE playlist_videos(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    playlist_id INTEGER NOT NULL
        CONSTRAINT playlist_videos_playlist_fk
            REFERENCES playlist ON DELETE CASCADE,
    video_id INTEGER NOT NULL
        CONSTRAINT playlist_videos_video_fk
            REFERENCES video ON DELETE CASCADE,
    position INTEGER NOT NULL DEFAULT 0
);

INSERT INTO playlist_videos(playlist_id, video_id, position) SELECT playlist_id, video_id, position FROM playlist_videos_bak;
