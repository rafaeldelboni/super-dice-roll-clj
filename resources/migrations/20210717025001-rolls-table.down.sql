DROP TRIGGER limit_user_channel_rows_trigger ON rolls;

--;;

DROP FUNCTION limit_user_channel_rows();

--;;

DROP INDEX IF EXISTS rolls_user_channel_idx;

--;;

DROP TABLE IF EXISTS rolls;
