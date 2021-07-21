CREATE TABLE IF NOT EXISTS rolls (
    id SERIAL PRIMARY KEY,
    user_id TEXT,
    channel_id INTEGER,
    command TEXT,
    modifier INTEGER,
    total INTEGER,
    each JSON,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

CREATE INDEX IF NOT EXISTS rolls_user_channel_idx ON rolls (user_id, channel_id);

--;;

CREATE FUNCTION limit_user_channel_rows() RETURNS trigger AS $limit_user_channel_rows$
    BEGIN
        DELETE FROM rolls WHERE id IN (
            WITH cte_rolls_to_delete AS (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        PARTITION BY user_id, channel_id
                        ORDER BY created_at DESC
                    ) AS row_number
                FROM 
                    rolls
                WHERE 
                    user_id = NEW.user_id AND
                    channel_id = NEW.channel_id
            )
            SELECT id FROM cte_rolls_to_delete WHERE row_number > 10
        );
        RETURN NULL;
    END;
$limit_user_channel_rows$ LANGUAGE plpgsql;

--;;

CREATE TRIGGER limit_user_channel_rows_trigger AFTER INSERT ON rolls
    FOR EACH ROW EXECUTE FUNCTION limit_user_channel_rows();
