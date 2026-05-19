package com.pe.limon.api.transactions.ubications.bussiness;

import com.pe.limon.api.transactions.ubications.repository.UbicationsRepository;
import com.pe.limon.api.transactions.ubications.repository.entity.DepartmentEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.DistrictEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.ProvinceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicationsService {

    private final UbicationsRepository ubicationsRepository;

    public List<DepartmentEntity> getDepartaments() {
        return ubicationsRepository.getDepartaments();
    }

    public List<ProvinceEntity> getProvinces(String departmentId) {
        return ubicationsRepository.getProvinces(departmentId);
    }

    public List<DistrictEntity> getDistricts(String departmentId, String provinceId) {
        return ubicationsRepository.getDistricts(departmentId, provinceId);
    }
}