package com.retail.smart.gateway;

import com.retail.smart.dto.SecurityAlertDTO;
import com.retail.smart.dto.SecurityMonitorDTO;
import com.retail.smart.dto.SecuritySummaryDTO;
import com.retail.smart.service.SecurityMonitorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityMonitorController {

    @Autowired
    private SecurityMonitorServiceImpl monitorService;

    @GetMapping("/alerts")
    public List<SecurityAlertDTO> getAllAlerts() {
        return monitorService.getAllAlerts();
    }

    @GetMapping("/alerts/filter")
    public List<SecurityAlertDTO> filterAlerts(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String location) {
        return monitorService.getFilteredAlerts(level, location);
    }

    @GetMapping("/events")
    public List<SecurityMonitorDTO> getAllEvents() {
        return monitorService.getAllEvents(); 
    }

    @GetMapping("/events/filter")
    public List<SecurityMonitorDTO> filterEvents(
            @RequestParam(required = false) String behavior,
            @RequestParam(required = false) String cameraId) {
        return monitorService.getFilteredEvents(behavior, cameraId);
    }

    @GetMapping("/alerts/levels")
    public Map<String, Long> getAlertCountsByLevel() {
        return monitorService.getAlertCountsByLevel();
    }

    @GetMapping("/alerts/top-locations")
    public List<String> getTopLocations() {
        return monitorService.getTopLocations();
    }

    @GetMapping("/alerts/average-per-day")
    public double getAverageAlertsPerDay() {
        return monitorService.getAverageAlertsPerDay();
    }

   @GetMapping("/summary")
public SecuritySummaryDTO getSecuritySummary() {
    return monitorService.getSecuritySummary();
}

}
