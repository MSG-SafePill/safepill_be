-- Read-only audit. IDs are intentionally not hardcoded.
-- Run against the team's development DB before selecting demo products.
SELECT 'MEDICINE' AS item_type, m.id AS item_id, m.item_seq,
       m.medicine_name AS item_name, i.ingredient_name,
       count(*) AS link_count
FROM medicine_master m
JOIN medicine_ingredient mi ON mi.medicine_id = m.id
JOIN ingredient_master i ON i.id = mi.ingredient_id
WHERE i.ingredient_name IN ('아세트아미노펜', '모르핀', '모르핀황산염수화물')
GROUP BY m.id, m.item_seq, m.medicine_name, i.ingredient_name
UNION ALL
SELECT 'SUPPLEMENT', s.id, s.item_seq, s.supplement_name, i.ingredient_name,
       count(*)
FROM supplement_master s
JOIN supplement_ingredient si ON si.supplement_id = s.id
JOIN ingredient_master i ON i.id = si.ingredient_id
WHERE i.ingredient_name IN ('칼슘', '비타민C', '비타민 C', '마그네슘', '산화마그네슘')
GROUP BY s.id, s.item_seq, s.supplement_name, i.ingredient_name
ORDER BY ingredient_name, item_id;

-- Official MFDS candidate: confirm existence and all ingredient links before import.
SELECT m.id, m.item_seq, m.medicine_name, i.ingredient_name, mi.dosage
FROM medicine_master m
LEFT JOIN medicine_ingredient mi ON mi.medicine_id = m.id
LEFT JOIN ingredient_master i ON i.id = mi.ingredient_id
WHERE m.item_seq = '199704619';
