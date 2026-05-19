package com.pe.limon.api.gateway.complements.ubications;

import com.pe.limon.api.transactions.ubications.bussiness.UbicationsService;
import com.pe.limon.api.transactions.ubications.repository.entity.DepartmentEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.DistrictEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.ProvinceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/complements/ubications")
@RequiredArgsConstructor
@Slf4j
public class UbicationsController {
    private final UbicationsService ubicationsService;

    @GetMapping("/departaments")
    public ResponseEntity<List<DepartmentEntity>> getDepartaments() {
        log.info("[getDepartaments]");
        return ResponseEntity.ok(ubicationsService.getDepartaments());
    }

    @GetMapping("/departaments/{departmentId}/provinces")
    public ResponseEntity<List<ProvinceEntity>> getProvinces(@PathVariable String departmentId) {
        log.info("[getProvinces] DepartmentId: {}", departmentId);
        return ResponseEntity.ok(ubicationsService.getProvinces(departmentId));
    }

    @GetMapping("/departaments/{departmentId}/provinces/{provinceId}/")
    public ResponseEntity<List<DistrictEntity>> getDistricts(@PathVariable String departmentId, @PathVariable String provinceId) {
        log.info("[getDistricts] DepartmentId: {}, ProvinceId: {}", departmentId, provinceId);
        return ResponseEntity.ok(ubicationsService.getDistricts(departmentId, provinceId));
    }
}