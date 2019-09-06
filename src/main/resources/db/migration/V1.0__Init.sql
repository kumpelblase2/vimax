PRAGMA foreign_keys = ON;

CREATE TABLE batch_job_execution_seq (
    id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE batch_job_instance (
    job_instance_id INTEGER PRIMARY KEY AUTOINCREMENT ,
    version BIGINT,
    job_name VARCHAR(100) NOT NULL,
    job_key VARCHAR(32) NOT NULL,
    UNIQUE (job_name, job_key)
);

CREATE TABLE batch_job_execution (
    job_execution_id INTEGER PRIMARY KEY AUTOINCREMENT ,
    version BIGINT,
    job_instance_id BIGINT NOT NULL
        CONSTRAINT job_inst_exec_fk
            REFERENCES batch_job_instance,
    create_time TIMESTAMP NOT NULL,
    start_time TIMESTAMP DEFAULT NULL,
    end_time TIMESTAMP DEFAULT NULL,
    status VARCHAR(10),
    exit_code VARCHAR(2500),
    exit_message VARCHAR(2500),
    last_updated TIMESTAMP,
    job_configuration_location VARCHAR(2500)
);

CREATE TABLE batch_job_execution_context (
    job_execution_id INTEGER NOT NULL PRIMARY KEY
        CONSTRAINT job_exec_ctx_fk
            REFERENCES batch_job_execution,
    short_context VARCHAR(2500) NOT NULL,
    serialized_context VARCHAR(16777216)
);

CREATE TABLE batch_job_execution_params (
    job_execution_id INTEGER NOT NULL
        CONSTRAINT job_exec_params_fk
            REFERENCES batch_job_execution,
    type_cd VARCHAR(6) NOT NULL,
    key_name VARCHAR(100) NOT NULL,
    string_val VARCHAR(250),
    date_val TIMESTAMP DEFAULT NULL,
    long_val BIGINT,
    double_val DOUBLE,
    identifying CHARACTER(1) NOT NULL
);

CREATE TABLE batch_job_seq (
    id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE batch_step_execution (
    step_execution_id INTEGER PRIMARY KEY AUTOINCREMENT ,
    version BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    job_execution_id BIGINT NOT NULL
        CONSTRAINT job_exec_step_fk
            REFERENCES batch_job_execution,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP DEFAULT NULL,
    status VARCHAR(10),
    commit_count BIGINT,
    read_count BIGINT,
    filter_count BIGINT,
    write_count BIGINT,
    read_skip_count BIGINT,
    write_skip_count BIGINT,
    process_skip_count BIGINT,
    rollback_count BIGINT,
    exit_code VARCHAR(2500),
    exit_message VARCHAR(2500),
    last_updated TIMESTAMP
);

CREATE TABLE batch_step_execution_context (
    step_execution_id INTEGER NOT NULL PRIMARY KEY
        CONSTRAINT step_exec_ctx_fk
            REFERENCES batch_step_execution,
    short_context VARCHAR(2500) NOT NULL,
    serialized_context VARCHAR(16777216)
);

CREATE TABLE batch_step_execution_seq (
    id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE library (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER,
    path VARCHAR(255)
);

CREATE TABLE metadata (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    ordering VARCHAR(255),
    read_only INTEGER NOT NULL,
    system_specified INTEGER,
    display_order INTEGER,
    type INTEGER,
    options TEXT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER
);

CREATE TABLE video (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER,
    location VARCHAR(255),
    name VARCHAR(255),
    selected_thumbnail INTEGER,
    library_id INTEGER
        CONSTRAINT video_library_fk
            REFERENCES library
);

CREATE TABLE video_metadata(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    definition_id INTEGER CONSTRAINT video_metadata_def_fk REFERENCES metadata ON DELETE CASCADE,
    video_id INTEGER CONSTRAINT video_metadata_id_fk REFERENCES video ON DELETE CASCADE,
    value TEXT
);

CREATE TABLE thumbnail (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    location VARCHAR(255),
    video_id INTEGER NOT NULL CONSTRAINT video_thumbnail_video_fk REFERENCES video
);

CREATE TABLE playlist (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER
);

CREATE TABLE playlist_videos (
    playlist_id INTEGER NOT NULL
        CONSTRAINT playlist_videos_playlist_fk
            REFERENCES playlist ON DELETE CASCADE,
    videos_id INTEGER NOT NULL
        CONSTRAINT playlist_videos_video_fk
            REFERENCES video ON DELETE CASCADE
);
