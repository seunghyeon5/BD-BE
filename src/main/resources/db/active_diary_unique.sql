-- 기존 데이터에서 같은 사용자/날짜에 활성 일기가 여러 건이면 먼저 정리해야 합니다.
SELECT user_id, diary_date, COUNT(*) AS active_count
FROM diaries
WHERE DEL_YN = 'N'
GROUP BY user_id, diary_date
HAVING COUNT(*) > 1;

-- 기존 uk_diary_user_date 인덱스가 남아 있다면 한 번만 삭제합니다.
-- ALTER TABLE diaries DROP INDEX uk_diary_user_date;

-- 삭제된 일기는 NULL, 활성 일기는 1이 되어 활성 일기만 날짜별로 유일하게 유지됩니다.
ALTER TABLE diaries
    ADD COLUMN active_diary_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN DEL_YN = 'N' THEN 1 ELSE NULL END) STORED,
    ADD UNIQUE KEY uk_diary_active_user_date (user_id, diary_date, active_diary_key);
