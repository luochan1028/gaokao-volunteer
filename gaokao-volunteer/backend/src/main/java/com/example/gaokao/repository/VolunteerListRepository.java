
package com.example.gaokao.repository;

import com.example.gaokao.entity.VolunteerList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerListRepository extends JpaRepository<VolunteerList, Long> {

    List<VolunteerList> findByUserId(Long userId);

    List<VolunteerList> findByUserIdAndGradientType(Long userId, VolunteerList.GradientType gradientType);

    List<VolunteerList> findByUserIdAndIsSelectedTrue(Long userId);

    @Query("SELECT vl FROM VolunteerList vl WHERE vl.user.id = :userId ORDER BY vl.priorityOrder")
    List<VolunteerList> findByUserIdOrderByPriority(@Param("userId") Long userId);

    @Query("SELECT vl FROM VolunteerList vl WHERE vl.college.id = :collegeId AND vl.user.id = :userId")
    List<VolunteerList> findByCollegeIdAndUserId(@Param("collegeId") Long collegeId, @Param("userId") Long userId);

    @Query("SELECT vl FROM VolunteerList vl WHERE vl.major.id = :majorId AND vl.user.id = :userId")
    List<VolunteerList> findByMajorIdAndUserId(@Param("majorId") Long majorId, @Param("userId") Long userId);
}
