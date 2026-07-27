
package com.example.gaokao.controller;

import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.entity.User;
import com.example.gaokao.entity.VolunteerList;
import com.example.gaokao.entity.VolunteerPlan;
import com.example.gaokao.service.UserService;
import com.example.gaokao.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteer")
@RequiredArgsConstructor
@Slf4j
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final UserService userService;

    @PostMapping("/list")
    public ApiResponse<VolunteerList> addToVolunteerList(
            @RequestParam Long collegeId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        VolunteerList result = volunteerService.addToVolunteerList(user, collegeId, majorId, name);
        return ApiResponse.success("添加成功", result);
    }

    @DeleteMapping("/list/{id}")
    public ApiResponse<Void> removeFromVolunteerList(@PathVariable Long id) {
        volunteerService.removeFromVolunteerList(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/list")
    public ApiResponse<List<VolunteerList>> getUserVolunteerList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        List<VolunteerList> list = volunteerService.getUserVolunteerList(user.getId());
        return ApiResponse.success(list);
    }

    @PostMapping("/plan")
    public ApiResponse<VolunteerPlan> savePlan(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String selectionMode = (String) request.get("selectionMode");
        @SuppressWarnings("unchecked")
        List<Long> volunteerListIds = (List<Long>) request.get("volunteerListIds");

        VolunteerPlan plan = volunteerService.savePlan(user, name, description, selectionMode, volunteerListIds);
        return ApiResponse.success("保存成功", plan);
    }

    @PutMapping("/plan/{id}")
    public ApiResponse<VolunteerPlan> updatePlan(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        VolunteerPlan plan = volunteerService.updatePlan(id, name, description);
        return ApiResponse.success("更新成功", plan);
    }

    @DeleteMapping("/plan/{id}")
    public ApiResponse<Void> deletePlan(@PathVariable Long id) {
        volunteerService.deletePlan(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/plan")
    public ApiResponse<List<VolunteerPlan>> getUserPlans() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        List<VolunteerPlan> plans = volunteerService.getUserPlans(user.getId());
        return ApiResponse.success(plans);
    }

    @GetMapping("/plan/{id}")
    public ApiResponse<VolunteerPlan> getPlanById(@PathVariable Long id) {
        VolunteerPlan plan = volunteerService.getPlanById(id);
        return ApiResponse.success(plan);
    }

    @GetMapping("/plan/{id}/report")
    public ApiResponse<String> generateReport(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        VolunteerPlan plan = volunteerService.getPlanById(id);
        String report = volunteerService.generateReport(user, plan);
        return ApiResponse.success(report);
    }

    @PostMapping("/plan/{id}/copy")
    public ApiResponse<VolunteerPlan> copyPlan(
            @PathVariable Long id,
            @RequestParam String newName) {
        VolunteerPlan plan = volunteerService.copyPlan(id, newName);
        return ApiResponse.success("复制成功", plan);
    }

    @PostMapping("/plan/{id}/default")
    public ApiResponse<Void> setDefaultPlan(@PathVariable Long id) {
        volunteerService.setDefaultPlan(id);
        return ApiResponse.success("设置成功", null);
    }
}
