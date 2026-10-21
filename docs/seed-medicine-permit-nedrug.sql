-- 제품명 보완으로만 성분이 들어간 약의 함량·허가 상태·ATC 보완.
-- 출처: 식품의약품안전처 의약품안전나라(nedrug.mfds.go.kr) 의약품 제품정보 "원료약품 및 분량".
-- 재실행해도 안전하다(이미 있는 성분/연결은 건너뛴다). 수집일: 2026-10-21
BEGIN;

-- 본렉스이알캡슐30밀리그램(시클로벤자프린염산염) (200903045)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '시클로벤자프린염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '시클로벤자프린염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.030 WHEN 'mg' THEN 30.000 WHEN 'mcg' THEN 30000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '시클로벤자프린염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200903045' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.030 WHEN 'mg' THEN 30.000 WHEN 'mcg' THEN 30000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200903045' AND i.ingredient_name = '시클로벤자프린염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20201001', atc_code = 'M03BX08' WHERE item_seq = '200903045' AND cancel_name IS NULL;

-- 올란프레서정10mg(올란자핀) (200903066)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '올란자핀', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '올란자핀');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '올란자핀' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200903066' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200903066' AND i.ingredient_name = '올란자핀' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20170421', atc_code = 'N05AH03' WHERE item_seq = '200903066' AND cancel_name IS NULL;

-- 올란프레서정5mg (올란자핀) (200903068)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '올란자핀', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '올란자핀');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.005 WHEN 'mg' THEN 5.000 WHEN 'mcg' THEN 5000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '올란자핀' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200903068' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.005 WHEN 'mg' THEN 5.000 WHEN 'mcg' THEN 5000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200903068' AND i.ingredient_name = '올란자핀' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20170421', atc_code = 'N05AH03' WHERE item_seq = '200903068' AND cancel_name IS NULL;

-- 올란프레서정2.5mg (올란자핀) (200903080)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '올란자핀', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '올란자핀');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.500 WHEN 'mcg' THEN 2500.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '올란자핀' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200903080' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.500 WHEN 'mcg' THEN 2500.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200903080' AND i.ingredient_name = '올란자핀' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20170421', atc_code = 'N05AH03' WHERE item_seq = '200903080' AND cancel_name IS NULL;

-- 메펜연질캡슐(메페남산) (200903217)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '메페남산', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '메페남산');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.250 WHEN 'mg' THEN 250.000 WHEN 'mcg' THEN 250000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '메페남산' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200903217' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.250 WHEN 'mg' THEN 250.000 WHEN 'mcg' THEN 250000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200903217' AND i.ingredient_name = '메페남산' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20151231', atc_code = 'M01AG01' WHERE item_seq = '200903217' AND cancel_name IS NULL;

-- 에델피스연질캡슐(디펜히드라민염산염) (200902506)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '디펜히드라민염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '디펜히드라민염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.050 WHEN 'mg' THEN 50.000 WHEN 'mcg' THEN 50000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '디펜히드라민염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200902506' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.050 WHEN 'mg' THEN 50.000 WHEN 'mcg' THEN 50000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200902506' AND i.ingredient_name = '디펜히드라민염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20180320', atc_code = 'N05CM' WHERE item_seq = '200902506' AND cancel_name IS NULL;

-- 한신타미놀캅셀(아세트아미노펜) (200101951)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아세트아미노펜', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아세트아미노펜');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.500 WHEN 'mg' THEN 500.000 WHEN 'mcg' THEN 500000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아세트아미노펜' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200101951' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.500 WHEN 'mg' THEN 500.000 WHEN 'mcg' THEN 500000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200101951' AND i.ingredient_name = '아세트아미노펜' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20190101', atc_code = 'N02BE01' WHERE item_seq = '200101951' AND cancel_name IS NULL;

-- 바로자미연질캡슐(디펜히드라민염산염) (200904019)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '디펜히드라민염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '디펜히드라민염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.050 WHEN 'mg' THEN 50.000 WHEN 'mcg' THEN 50000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '디펜히드라민염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200904019' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.050 WHEN 'mg' THEN 50.000 WHEN 'mcg' THEN 50000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200904019' AND i.ingredient_name = '디펜히드라민염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N05CM' WHERE item_seq = '200904019' AND cancel_name IS NULL;

-- 덱시쿨펜연질캡슐(덱시부프로펜) (200905597)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '덱시부프로펜', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '덱시부프로펜');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '덱시부프로펜' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200905597' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200905597' AND i.ingredient_name = '덱시부프로펜' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20190101', atc_code = 'M01AE14' WHERE item_seq = '200905597' AND cancel_name IS NULL;

-- 덱시카이플러스연질캡슐(덱시부프로펜) (200905472)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '덱시부프로펜', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '덱시부프로펜');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '덱시부프로펜' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200905472' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200905472' AND i.ingredient_name = '덱시부프로펜' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20190101', atc_code = 'M01AE14' WHERE item_seq = '200905472' AND cancel_name IS NULL;

-- 덱스모아연질캡슐(덱시부프로펜) (200904652)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '덱시부프로펜', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '덱시부프로펜');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '덱시부프로펜' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200904652' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.300 WHEN 'mg' THEN 300.000 WHEN 'mcg' THEN 300000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200904652' AND i.ingredient_name = '덱시부프로펜' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20190101', atc_code = 'M01AE14' WHERE item_seq = '200904652' AND cancel_name IS NULL;

-- 린피스정(아스피린) (200905264)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아스피린', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아스피린');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.081 WHEN 'mg' THEN 81.000 WHEN 'mcg' THEN 81000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아스피린' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200905264' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.081 WHEN 'mg' THEN 81.000 WHEN 'mcg' THEN 81000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200905264' AND i.ingredient_name = '아스피린' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20191001', atc_code = 'B01AC06' WHERE item_seq = '200905264' AND cancel_name IS NULL;

-- 넥스핀정(아스피린) (200905198)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아스피린', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아스피린');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.081 WHEN 'mg' THEN 81.000 WHEN 'mcg' THEN 81000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아스피린' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200905198' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.081 WHEN 'mg' THEN 81.000 WHEN 'mcg' THEN 81000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200905198' AND i.ingredient_name = '아스피린' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20140710', atc_code = 'B01AC06' WHERE item_seq = '200905198' AND cancel_name IS NULL;

-- 세티린정(세티리진염산염) (200904774)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '세티리진염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '세티리진염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '세티리진염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200904774' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200904774' AND i.ingredient_name = '세티리진염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20210101', atc_code = 'R06AE07' WHERE item_seq = '200904774' AND cancel_name IS NULL;

-- 베아프렉사정5밀리그램(올란자핀) (201002394)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '올란자핀', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '올란자핀');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.005 WHEN 'mg' THEN 5.000 WHEN 'mcg' THEN 5000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '올란자핀' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201002394' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.005 WHEN 'mg' THEN 5.000 WHEN 'mcg' THEN 5000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201002394' AND i.ingredient_name = '올란자핀' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N05AH03' WHERE item_seq = '201002394' AND cancel_name IS NULL;

-- 넥사졸캡슐40밀리그램(에스오메프라졸) (201003060)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '에스오메프라졸', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '에스오메프라졸');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '에스오메프라졸' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003060' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003060' AND i.ingredient_name = '에스오메프라졸' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20190522', atc_code = 'A02BC05' WHERE item_seq = '201003060' AND cancel_name IS NULL;

-- 에소메드캡슐40밀리그램(에스오메프라졸) (201003062)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '에스오메프라졸', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '에스오메프라졸');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '에스오메프라졸' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003062' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003062' AND i.ingredient_name = '에스오메프라졸' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20200401', atc_code = 'A02BC05' WHERE item_seq = '201003062' AND cancel_name IS NULL;

-- 에스졸캡슐40밀리그램(에스오메프라졸) (201003061)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '에스오메프라졸', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '에스오메프라졸');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '에스오메프라졸' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003061' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.040 WHEN 'mg' THEN 40.000 WHEN 'mcg' THEN 40000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003061' AND i.ingredient_name = '에스오메프라졸' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20200401', atc_code = 'A02BC05' WHERE item_seq = '201003061' AND cancel_name IS NULL;

-- 아데포라정10mg(아데포비어디피복실) (201003185)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아데포비어디피복실', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003185' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003185' AND i.ingredient_name = '아데포비어디피복실' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20200123', atc_code = 'J05AF08' WHERE item_seq = '201003185' AND cancel_name IS NULL;

-- 도파로핀정2밀리그램(로피니롤염산염) (200907789)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '로피니롤염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '로피니롤염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.280 WHEN 'mcg' THEN 2280.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '로피니롤염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200907789' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.280 WHEN 'mcg' THEN 2280.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200907789' AND i.ingredient_name = '로피니롤염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20150630', atc_code = 'N04BC04' WHERE item_seq = '200907789' AND cancel_name IS NULL;

-- 올라핀정2.5밀리그램(올란자핀) (200908352)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '올란자핀', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '올란자핀');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.500 WHEN 'mcg' THEN 2500.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '올란자핀' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200908352' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.002 WHEN 'mg' THEN 2.500 WHEN 'mcg' THEN 2500.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200908352' AND i.ingredient_name = '올란자핀' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N05AH03' WHERE item_seq = '200908352' AND cancel_name IS NULL;

-- 데이프로정10mg(에스시탈로프람옥살산염) (200908438)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '에스시탈로프람옥살산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '에스시탈로프람옥살산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.013 WHEN 'mg' THEN 12.770 WHEN 'mcg' THEN 12770.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '에스시탈로프람옥살산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '200908438' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.013 WHEN 'mg' THEN 12.770 WHEN 'mcg' THEN 12770.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '200908438' AND i.ingredient_name = '에스시탈로프람옥살산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N06AB10' WHERE item_seq = '200908438' AND cancel_name IS NULL;

-- 콜레리스정10밀리그램(로수바스타틴칼슘) (201000964)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '로수바스타틴칼슘', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '로수바스타틴칼슘');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.400 WHEN 'mcg' THEN 10400.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '로수바스타틴칼슘' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201000964' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.400 WHEN 'mcg' THEN 10400.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201000964' AND i.ingredient_name = '로수바스타틴칼슘' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20191001', atc_code = 'C10AA07' WHERE item_seq = '201000964' AND cancel_name IS NULL;

-- 콜레리스정20밀리그램(로수바스타틴칼슘) (201000965)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '로수바스타틴칼슘', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '로수바스타틴칼슘');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.021 WHEN 'mg' THEN 20.800 WHEN 'mcg' THEN 20800.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '로수바스타틴칼슘' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201000965' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.021 WHEN 'mg' THEN 20.800 WHEN 'mcg' THEN 20800.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201000965' AND i.ingredient_name = '로수바스타틴칼슘' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20191001', atc_code = 'C10AA07' WHERE item_seq = '201000965' AND cancel_name IS NULL;

-- 자부딘정100밀리그램(라미부딘) (201001034)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '라미부딘', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '라미부딘');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.100 WHEN 'mg' THEN 100.000 WHEN 'mcg' THEN 100000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '라미부딘' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201001034' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.100 WHEN 'mg' THEN 100.000 WHEN 'mcg' THEN 100000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201001034' AND i.ingredient_name = '라미부딘' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20131023', atc_code = 'J05AF05' WHERE item_seq = '201001034' AND cancel_name IS NULL;

-- 제라부딘정(라미부딘) (201001037)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '라미부딘', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '라미부딘');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.100 WHEN 'mg' THEN 100.000 WHEN 'mcg' THEN 100000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '라미부딘' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201001037' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.100 WHEN 'mg' THEN 100.000 WHEN 'mcg' THEN 100000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201001037' AND i.ingredient_name = '라미부딘' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20200521', atc_code = 'J05AF05' WHERE item_seq = '201001037' AND cancel_name IS NULL;

-- 파로킨정0.25mg(로피니롤염산염) (201001526)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '로피니롤염산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '로피니롤염산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.000 WHEN 'mg' THEN 0.285 WHEN 'mcg' THEN 285.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '로피니롤염산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201001526' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.000 WHEN 'mg' THEN 0.285 WHEN 'mcg' THEN 285.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201001526' AND i.ingredient_name = '로피니롤염산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N04BC04' WHERE item_seq = '201001526' AND cancel_name IS NULL;

-- 갈라닐피알서방캡슐24밀리그램(갈란타민브롬화수소산염) (201001650)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '갈란타민브롬화수소산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '갈란타민브롬화수소산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.023 WHEN 'mg' THEN 23.070 WHEN 'mcg' THEN 23070.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '갈란타민브롬화수소산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201001650' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.023 WHEN 'mg' THEN 23.070 WHEN 'mcg' THEN 23070.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201001650' AND i.ingredient_name = '갈란타민브롬화수소산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N06DA04' WHERE item_seq = '201001650' AND cancel_name IS NULL;

-- 갈라닐피알서방캡슐16밀리그램(갈란타민브롬화수소산염) (201001649)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '갈란타민브롬화수소산염', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '갈란타민브롬화수소산염');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.015 WHEN 'mg' THEN 15.380 WHEN 'mcg' THEN 15380.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '갈란타민브롬화수소산염' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201001649' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.015 WHEN 'mg' THEN 15.380 WHEN 'mcg' THEN 15380.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201001649' AND i.ingredient_name = '갈란타민브롬화수소산염' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '유효기간만료', cancel_date = '20181001', atc_code = 'N06DA04' WHERE item_seq = '201001649' AND cancel_name IS NULL;

-- 헤포빌정10밀리그램(아데포비어디피복실) (201003217)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아데포비어디피복실', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003217' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003217' AND i.ingredient_name = '아데포비어디피복실' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20200909', atc_code = 'J05AF08' WHERE item_seq = '201003217' AND cancel_name IS NULL;

-- 아데비어정10밀리그램(아데포비어디피복실) (201003218)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아데포비어디피복실', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003218' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003218' AND i.ingredient_name = '아데포비어디피복실' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20180312', atc_code = 'J05AF08' WHERE item_seq = '201003218' AND cancel_name IS NULL;

-- 아데바정(아데포비어디피복실) (201003221)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아데포비어디피복실', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003221' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003221' AND i.ingredient_name = '아데포비어디피복실' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20180627', atc_code = 'J05AF08' WHERE item_seq = '201003221' AND cancel_name IS NULL;

-- 리벡실정(아데포비어디피복실) (201003224)
INSERT INTO ingredient_master (ingredient_name, unit, best_time_guide, intake_tip) SELECT '아데포비어디피복실', 'mg', '정보 없음', '정보 없음' WHERE NOT EXISTS (SELECT 1 FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실');
INSERT INTO medicine_ingredient (medicine_id, ingredient_id, dosage) SELECT m.id, i.id, CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, LATERAL (SELECT id, unit FROM ingredient_master WHERE ingredient_name = '아데포비어디피복실' ORDER BY id LIMIT 1) i WHERE m.item_seq = '201003224' AND NOT EXISTS (SELECT 1 FROM medicine_ingredient x WHERE x.medicine_id = m.id AND x.ingredient_id = i.id);
UPDATE medicine_ingredient mi SET dosage = CASE i.unit WHEN 'g' THEN 0.010 WHEN 'mg' THEN 10.000 WHEN 'mcg' THEN 10000.000 ELSE NULL END FROM medicine_master m, ingredient_master i WHERE m.item_seq = '201003224' AND i.ingredient_name = '아데포비어디피복실' AND mi.medicine_id = m.id AND mi.ingredient_id = i.id AND mi.dosage IS NULL;
UPDATE medicine_master SET cancel_name = '취하', cancel_date = '20170724', atc_code = 'J05AF08' WHERE item_seq = '201003224' AND cancel_name IS NULL;

COMMIT;
