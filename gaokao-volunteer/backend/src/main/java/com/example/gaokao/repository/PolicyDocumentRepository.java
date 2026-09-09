package com.example.gaokao.repository;

import com.example.gaokao.entity.PolicyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {

    List<PolicyDocument> findByIsActiveTrueOrderByPublishDateDesc();

    List<PolicyDocument> findByProvinceAndIsActiveTrue(String province);
}
