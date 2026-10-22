-- DUR 품목정보 API(getUsjntTabooInfoList03)로 우리 DB 약 502개를 전수 조회한 결과,
-- DB 안의 약끼리 걸리는 공식 병용금기는 아래 1개 성분 쌍(제품 2쌍)뿐이었다. (조회일 2026-10-23)
--   수마트란정50mg(수마트립탄숙신산염) x 프릴리지정30mg/60mg(다폭세틴염산염): 세로토닌성 증후군
-- 공공 DUR 규칙은 기본 성분명(다폭세틴, 수마트립탄)이고 우리 약의 성분은 염 형태 이름이라
-- 성분명이 달라 규칙이 적용되지 않았다. 원본 규칙의 위험도·설명을 그대로 복사해 염 형태 성분에 연결한다.
-- 재실행해도 안전하다(이미 연결돼 있으면 추가하지 않는다).
INSERT INTO interaction_rule (ingredient_a_id, ingredient_b_id, risk_level, description)
SELECT a.id, b.id, r.risk_level, r.description
FROM interaction_rule r
JOIN ingredient_master ra ON ra.id = r.ingredient_a_id AND ra.ingredient_name = '다폭세틴'
JOIN ingredient_master rb ON rb.id = r.ingredient_b_id AND rb.ingredient_name = '수마트립탄'
JOIN LATERAL (SELECT id FROM ingredient_master WHERE ingredient_name = '다폭세틴염산염' ORDER BY id LIMIT 1) a ON TRUE
JOIN LATERAL (SELECT id FROM ingredient_master WHERE ingredient_name = '수마트립탄숙신산염' ORDER BY id LIMIT 1) b ON TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM interaction_rule x
    WHERE (x.ingredient_a_id = a.id AND x.ingredient_b_id = b.id)
       OR (x.ingredient_a_id = b.id AND x.ingredient_b_id = a.id)
);
