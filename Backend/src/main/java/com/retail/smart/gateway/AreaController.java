package com.retail.smart.gateway;

import com.retail.smart.dto.AreaDTO;
import com.retail.smart.entity.Area;
import com.retail.smart.repository.AreaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaRepository areaRepository;

    public AreaController(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @PostMapping
    public ResponseEntity<String> addArea(@Valid @RequestBody AreaDTO dto) {
        if (areaRepository.existsById(dto.getCode())) {
            return ResponseEntity.badRequest().body("Area already exists.");
        }
        areaRepository.save(new Area(dto.getCode()));
        return ResponseEntity.ok("Area added.");
    }

    @GetMapping
    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }
}
