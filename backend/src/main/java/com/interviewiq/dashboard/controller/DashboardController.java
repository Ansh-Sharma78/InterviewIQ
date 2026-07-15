package com.interviewiq.dashboard.controller;

import com.interviewiq.dashboard.dto.DashboardSummaryResponse;
import com.interviewiq.dashboard.service.DashboardService;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return dashboardService.summary(user.id());
    }
}

