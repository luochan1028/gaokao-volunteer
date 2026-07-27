
package com.example.gaokao.service;

import com.example.gaokao.dto.request.RecommendationRequest;
import com.example.gaokao.entity.User;
import com.example.gaokao.entity.VolunteerList;
import com.example.gaokao.entity.VolunteerPlan;

import java.util.List;

public interface VolunteerService {

    VolunteerList addToVolunteerList(User user, Long collegeId, Long majorId, String name);

    void removeFromVolunteerList(Long id);

    List<VolunteerList> getUserVolunteerList(Long userId);

    VolunteerPlan savePlan(User user, String name, String description, String selectionMode,
                           List<Long> volunteerListIds);

    VolunteerPlan updatePlan(Long planId, String name, String description);

    void deletePlan(Long planId);

    List<VolunteerPlan> getUserPlans(Long userId);

    VolunteerPlan getPlanById(Long planId);

    String generateReport(User user, VolunteerPlan plan);

    VolunteerPlan copyPlan(Long planId, String newName);

    void setDefaultPlan(Long planId);
}
