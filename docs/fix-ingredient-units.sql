-- 단위가 비어 있던 성분의 단위 보정.
-- 이소트레티노인: 식약처 오피큐탄연질캡슐(품목기준코드 201505259) 원료약품 및 분량이 "10 밀리그램".
UPDATE ingredient_master SET unit = 'mg' WHERE ingredient_name = '이소트레티노인' AND unit IS NULL;
