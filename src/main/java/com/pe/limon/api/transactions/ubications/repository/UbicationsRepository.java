package com.pe.limon.api.transactions.ubications.repository;

import com.pe.limon.api.transactions.ubications.repository.entity.DepartmentEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.DistrictEntity;
import com.pe.limon.api.transactions.ubications.repository.entity.ProvinceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UbicationsRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public List<DepartmentEntity> getDepartaments() {
        String sql = """
            SELECT
                cod_depa,
                nom_depa
            FROM tbl_ubi_depa
            ORDER BY nom_depa
            """;

        return namedJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(DepartmentEntity.class));
    }

    public List<ProvinceEntity> getProvinces(String departmentId) {
        String sql = """
            SELECT
                cod_prov,
                cod_depa,
                nom_prov
            FROM tbl_ubi_prov
            WHERE cod_depa = :departmentId
            ORDER BY nom_prov
            """;

        var params = new MapSqlParameterSource().addValue("departmentId", departmentId);

        return namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ProvinceEntity.class));
    }

    public List<DistrictEntity> getDistricts(String departmentId, String provinceId) {
        String sql = """
            SELECT
                cod_dist,
                cod_prov,
                cod_depa,
                nom_dist
            FROM tbl_ubi_dist
            WHERE cod_depa = :departmentId AND cod_prov = :provinceId
            ORDER BY nom_dist
            """;

        var params = new MapSqlParameterSource()
                .addValue("departmentId", departmentId)
                .addValue("provinceId", provinceId);

        return namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(DistrictEntity.class));
    }
}