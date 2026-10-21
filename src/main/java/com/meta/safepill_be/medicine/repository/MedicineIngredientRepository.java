package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MedicineIngredientRepository extends JpaRepository<MedicineIngredient, Long> {
    List<MedicineIngredient> findByMedicineMaster_Id(Long medicineId);
    boolean existsByMedicineMaster_IdAndIngredientMaster_Id(Long medicineId, Long ingredientId);
    @EntityGraph(attributePaths = {"medicineMaster", "medicineMaster.ingredients", "medicineMaster.ingredients.ingredientMaster"})
    List<MedicineIngredient> findTop10ByIngredientMaster_Id(Long ingredientId);

    /** 주어진 성분 중 하나라도 공유하는 다른 약의 ID. 많이 공유하는 약이 먼저 온다. */
    @Query("select mi.medicineMaster.id from MedicineIngredient mi "
            + "where mi.ingredientMaster.id in :ingredientIds and mi.medicineMaster.id <> :excludeId "
            + "group by mi.medicineMaster.id "
            + "order by count(distinct mi.ingredientMaster.id) desc, mi.medicineMaster.id asc")
    List<Long> findCandidateMedicineIds(@Param("ingredientIds") Collection<Long> ingredientIds,
                                        @Param("excludeId") Long excludeId);
}
