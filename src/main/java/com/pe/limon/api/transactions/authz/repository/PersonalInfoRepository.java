package com.pe.limon.api.transactions.authz.repository;

import com.pe.limon.api.transactions.authz.repository.entity.PersonalInfoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PersonalInfoRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public void insert(PersonalInfoEntity entity){
        String sql = "INSERT INTO tbl_personal_info (id,email, provider, profile_completed, password, registration_completed,registered_datetime, registered_timestamp) " +
                "VALUES (:id,:email,:provider,false, :password,:registrationCompleted, :registeredDatetime);";
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity));
    }

    public void save(PersonalInfoEntity personalInfoEntity) {
        String sql = """
            INSERT INTO tbl_personal_info (
                user_id, first_name, last_name, email, department, province, district,
                address, document_type, document_number, birth_date, gender, phone_number,
                registered_datetime, registered_timestamp
            ) VALUES (
                :userId, :firstName, :lastName, :email, :department, :province, :district,
                :address, :documentType, :documentNumber, :birthDate, :gender, :phoneNumber,
                :registeredDatetime, :registeredTimestamp
            )
        """;
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(personalInfoEntity);
        namedJdbcTemplate.update(sql, params);
    }

    public PersonalInfoEntity findByUserId(String userId) {
        try {
            String sql = """
            SELECT
                pi.user_id,
                pi.first_name,
                pi.last_name,
                pi.email,
                pi.phone_number,
                pi.birth_date,
                pi.document_number,
                pi.document_type
            FROM tbl_personal_info pi
            WHERE pi.user_id = :userId
            """;
            var params = new MapSqlParameterSource().addValue("userId", userId);

            return namedJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(PersonalInfoEntity.class));
        }catch (DataAccessException e){
            return null;
        }
    }
}
