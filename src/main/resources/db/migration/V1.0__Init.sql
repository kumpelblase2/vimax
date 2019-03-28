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

CREATE TABLE selection_values (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255)
);

CREATE TABLE metadata_options (
    dtype VARCHAR(31) NOT NULL,
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    default_number_value INTEGER,
    "MAX" INTEGER,
    "MIN" INTEGER,
    step INTEGER,
    default_date_value DATE,
    default_text_value VARCHAR(255),
    suggest INTEGER,
    default_time_value TIME,
    default_timestamp_value TIMESTAMP,
    default_boolean_value INTEGER,
    default_long_value BIGINT,
    default_select_value_id BIGINT REFERENCES selection_values,
    default_tag_values TEXT,
    default_double_value DOUBLE
);

CREATE TABLE metadata (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    ordering VARCHAR(255),
    read_only INTEGER NOT NULL,
    system_specified INTEGER,
    display_order INTEGER,
    type INTEGER,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER,
    options_id INTEGER
        CONSTRAINT metadata_options_id_fk
            REFERENCES metadata_options
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

CREATE TABLE thumbnail (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    location VARCHAR(255),
    video_id INTEGER NOT NULL
        CONSTRAINT video_thumbnail_video_fk
            REFERENCES video
);

CREATE TABLE metadata_value (
    dtype INTEGER NOT NULL,
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    metadata_id INTEGER NOT NULL
        CONSTRAINT metadata_value_metadata_fk
            REFERENCES metadata,
    video_id INTEGER NOT NULL
        CONSTRAINT metadata_value_video_fk
            REFERENCES video,
    number_value INTEGER,
    string_value VARCHAR(255),
    timestamp_value TIMESTAMP,
    taglist_values TEXT,
    date_value DATE,
    duration_value BIGINT,
    boolean_value INTEGER,
    selection_value_id BIGINT
        CONSTRAINT METADATA_VALUE_SELECTION_FK
            REFERENCES selection_values,
    floating_value DOUBLE,
    time_value TIME
);

CREATE TABLE metadata_options_values (
    selection_metadata_options_id INTEGER NOT NULL
        CONSTRAINT metadata_options_metadata_fk
            REFERENCES metadata_options,
    values_id BIGINT NOT NULL UNIQUE
        CONSTRAINT metadata_options_select_fk
            REFERENCES selection_values
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
            REFERENCES playlist,
    videos_id INTEGER NOT NULL
        CONSTRAINT playlist_videos_video_fk
            REFERENCES video
);
