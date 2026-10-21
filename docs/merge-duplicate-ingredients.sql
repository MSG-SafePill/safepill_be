-- 성분 마스터 중복 정리.
-- 같은 이름의 성분이 여러 행이면 가장 작은 id만 남기고 모든 참조를 옮긴 뒤 나머지를 삭제한다.
-- 중복된 행에 병용 규칙이 나뉘어 붙어 있으면 분석이 일부 규칙을 놓치므로, 합치면 규칙이 오히려 늘어난다.
-- 재실행해도 안전하다(중복이 없으면 아무것도 바꾸지 않는다).
BEGIN;

CREATE TEMP TABLE ingredient_dup_map ON COMMIT DROP AS
SELECT id AS dup_id, MIN(id) OVER (PARTITION BY ingredient_name) AS keep_id
FROM ingredient_master;
DELETE FROM ingredient_dup_map WHERE dup_id = keep_id;

UPDATE medicine_ingredient mi SET ingredient_id = m.keep_id
FROM ingredient_dup_map m WHERE mi.ingredient_id = m.dup_id;

UPDATE supplement_ingredient si SET ingredient_id = m.keep_id
FROM ingredient_dup_map m WHERE si.ingredient_id = m.dup_id;

UPDATE interaction_rule r SET ingredient_a_id = m.keep_id
FROM ingredient_dup_map m WHERE r.ingredient_a_id = m.dup_id;

UPDATE interaction_rule r SET ingredient_b_id = m.keep_id
FROM ingredient_dup_map m WHERE r.ingredient_b_id = m.dup_id;

-- 합친 뒤 자기 자신과의 규칙, 내용까지 완전히 같은 중복 규칙 제거
DELETE FROM interaction_rule WHERE ingredient_a_id = ingredient_b_id;

DELETE FROM interaction_rule r USING interaction_rule k
WHERE r.id > k.id
  AND LEAST(r.ingredient_a_id, r.ingredient_b_id) = LEAST(k.ingredient_a_id, k.ingredient_b_id)
  AND GREATEST(r.ingredient_a_id, r.ingredient_b_id) = GREATEST(k.ingredient_a_id, k.ingredient_b_id)
  AND r.risk_level IS NOT DISTINCT FROM k.risk_level
  AND r.description IS NOT DISTINCT FROM k.description;

-- 같은 제품에 같은 성분이 중복 연결된 행 제거
DELETE FROM supplement_ingredient a USING supplement_ingredient b
WHERE a.id > b.id AND a.supplement_id = b.supplement_id AND a.ingredient_id = b.ingredient_id;

DELETE FROM medicine_ingredient a USING medicine_ingredient b
WHERE a.id > b.id AND a.medicine_id = b.medicine_id AND a.ingredient_id = b.ingredient_id;

DELETE FROM ingredient_master WHERE id IN (SELECT dup_id FROM ingredient_dup_map);

COMMIT;
