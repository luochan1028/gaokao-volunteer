
package com.example.gaokao.repository;

import com.example.gaokao.entity.VolunteerPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerPlanRepository extends JpaRepository<VolunteerPlan, Long> {

    List<VolunteerPlan> findByUserId(Long userId);

    Optional<VolunteerPlan> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<VolunteerPlan> findByUserIdAndId(Long userId, Long planId);
}
