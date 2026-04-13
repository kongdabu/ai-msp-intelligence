-- @Lob 제거 후 bytea 타입으로 남은 컬럼을 text/varchar로 변환
-- convert_from(col, 'UTF8'): bytea에 저장된 UTF-8 바이트를 문자열로 복원

DO $$
BEGIN

  -- article.title: bytea → varchar(500)
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'article' AND column_name = 'title') = 'bytea' THEN
    ALTER TABLE article ALTER COLUMN title TYPE varchar(500)
      USING convert_from(title, 'UTF8');
  END IF;

  -- article.summary: bytea → varchar(500)
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'article' AND column_name = 'summary') = 'bytea' THEN
    ALTER TABLE article ALTER COLUMN summary TYPE varchar(500)
      USING convert_from(summary, 'UTF8');
  END IF;

  -- article.original_content: bytea → text
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'article' AND column_name = 'original_content') = 'bytea' THEN
    ALTER TABLE article ALTER COLUMN original_content TYPE text
      USING convert_from(original_content, 'UTF8');
  END IF;

  -- article.source_name: bytea → varchar(200)
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'article' AND column_name = 'source_name') = 'bytea' THEN
    ALTER TABLE article ALTER COLUMN source_name TYPE varchar(200)
      USING convert_from(source_name, 'UTF8');
  END IF;

  -- insight.content: bytea → text
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'insight' AND column_name = 'content') = 'bytea' THEN
    ALTER TABLE insight ALTER COLUMN content TYPE text
      USING convert_from(content, 'UTF8');
  END IF;

  -- insight.title: bytea → varchar(200)
  IF (SELECT data_type FROM information_schema.columns
      WHERE table_name = 'insight' AND column_name = 'title') = 'bytea' THEN
    ALTER TABLE insight ALTER COLUMN title TYPE varchar(200)
      USING convert_from(title, 'UTF8');
  END IF;

END $$;
