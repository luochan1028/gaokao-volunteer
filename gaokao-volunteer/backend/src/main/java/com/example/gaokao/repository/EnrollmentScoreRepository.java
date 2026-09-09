
package com.example.gaokao.repository;

import com.example.gaokao.entity.EnrollmentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentScoreRepository extends JpaRepository<EnrollmentScore, Long> {

    List<EnrollmentScore> findByCollegeId(Long collegeId);

    List<EnrollmentScore> findByYearAndProvince(String year, String province);

    @Query("SELECT es FROM EnrollmentScore es WHERE es.year = :year AND es.province = :province AND es.scienceOrArts = :scienceOrArts")
    List<EnrollmentScore> findByYearAndProvinceAndScienceOrArts(@Param("year") String year, @Param("province") String province, @Param("scienceOrArts") EnrollmentScore.ScienceOrArts scienceOrArts);

    @Query("SELECT es FROM EnrollmentScore es WHERE es.college.id = :collegeId AND es.year = :year")
    List<EnrollmentScore> findByCollegeIdAndYear(@Param("collegeId") Long collegeId, @Param("year") String year);

    @Query("SELECT es FROM EnrollmentScore es WHERE es.province = :province AND es.year = :year AND es.batch = :batch")
    List<EnrollmentScore> findByProvinceYearAndBatch(@Param("province") String province, @Param("year") String year, @Param("batch") EnrollmentScore.Batch batch);

    @Query("SELECT es FROM EnrollmentScore es WHERE es.province = :province AND es.year = :year AND es.scienceOrArts = :scienceOrArts AND es.minScore <= :score AND es.maxScore >= :score")
    List<EnrollmentScore> findScoresNearScore(@Param("province") String province, @Param("year") String year, @Param("scienceOrArts") EnrollmentScore.ScienceOrArts scienceOrArts, @Param("score") Integer score);

    @Query("SELECT es FROM EnrollmentScore es WHERE es.province = :province AND es.year = :year AND es.minRank <= :rank AND es.maxRank >= :rank")
    List<EnrollmentScore> findScoresNearRank(@Param("province") String province, @Param("year") String year, @Param("rank") Integer rank);

    @Query("SELECT DISTINCT es.year FROM EnrollmentScore es ORDER BY es.year DESC")
    List<String> findAllYears();

    @Query("SELECT DISTINCT es.province FROM EnrollmentScore es ORDER BY es.province")
    List<String> findAllProvinces();
}
