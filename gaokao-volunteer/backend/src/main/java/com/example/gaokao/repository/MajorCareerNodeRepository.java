package com.example.gaokao.repository;

import com.example.gaokao.entity.MajorCareerNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorCareerNodeRepository extends JpaRepository<MajorCareerNode, Long> {
    Optional<MajorCareerNode> findByMajorName(String majorName);
    List<MajorCareerNode> findByCategory(String category);
}
