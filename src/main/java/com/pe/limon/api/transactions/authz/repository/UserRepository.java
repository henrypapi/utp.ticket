package com.pe.limon.api.transactions.authz.repository;

import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public void insertUser(UserEntity entity){
        String sql = """
            INSERT INTO tbl_user
            (id,email, password, provider, registration_completed, profile_completed,email_verified, phone_verified,
            registered_timestamp, registered_datetime, status)
            VALUES (
            :id,:email, :password,:provider,:registrationCompleted, :profileCompleted, :emailVerified, :phoneVerified,
            :registeredTimestamp,:registeredDatetime, :status
            );""";
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity));
    }

    public Optional<UserEntity> findByUsername(String username){
        try {
            String sql = "SELECT * FROM tbl_user WHERE email = ?";
            var entity = jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(UserEntity.class),username);
            return Optional.of(entity);
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public Optional<UserEntity> findByEmailOrUsername(String username, String email){
        try {
            String sql = """
            SELECT
                *
            FROM tbl_user u
            INNER JOIN tbl_profile p ON u.id = p.user_id
            WHERE u.email = ? OR p.username = ?
            """;

            log.info("User: {}-{}", username,email);

            var entity = jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(UserEntity.class),email,username);
            log.info("User: {}", entity);

            return Optional.of(entity);
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public boolean existsByEmailOrUsername(String username, String email){
        String sql = "SELECT COUNT(*) FROM tbl_user WHERE email = ? OR username = ?";
        var count = jdbcTemplate.queryForObject(sql,Integer.class,username, email);
        return count != null && count > 0;
    }

    public List<String> missingUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // Tomamos solo los distintos para no sobrevalidar
        List<String> distinctUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        String sql = """
        SELECT id
        FROM tbl_user
        WHERE id IN (:userIds)
        """;

        var params = new MapSqlParameterSource()
                .addValue("userIds", distinctUserIds);

        List<String> found = namedJdbcTemplate.query(sql, params,
                (rs, n) -> rs.getString("id"));

        Set<String> foundSet = new HashSet<>(found);

        return distinctUserIds.stream()
                .filter(u -> !foundSet.contains(u))
                .toList();
    }

    public UserEntity findById(String id){
        try {
            String sql = "SELECT * FROM tbl_user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(UserEntity.class),id);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public boolean getUser(){
        return false;
    }

    public void updateUser(UserEntity entity){
        String sql = "UPDATE tbl_user\n" +
                "SET\n" +
                "  username     = CASE WHEN :username    IS NOT NULL THEN :username    ELSE username END,\n" +
                "  profile_img  = CASE WHEN :profileImg  IS NOT NULL THEN :profileImg  ELSE profile_img END,\n" +
                "  name         = CASE WHEN :name        IS NOT NULL THEN :name        ELSE name END,\n" +
                "  phone_number = CASE WHEN :phoneNumber IS NOT NULL THEN :phoneNumber ELSE phone_number END\n" +
                "WHERE id = :id;";
        namedJdbcTemplate.update(sql,new BeanPropertySqlParameterSource(entity));
    }

    public Optional<UserEntity> findByEmail(String email) {
        try {
            var sql = """
            SELECT
            u.*,
            p.username,
            p.profile_image
            FROM tbl_user u
            LEFT JOIN tbl_profile p ON u.id = p.user_id
            WHERE u.email = ?
            """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UserEntity user = new UserEntity();
                user.setId(rs.getString("u.id"));
                user.setEmail(rs.getString("u.email"));
                ProfileEntity profile = new ProfileEntity();
                profile.setUsername(rs.getString("p.username"));
                profile.setProfileImage(rs.getString("p.profile_image"));
                user.setProfile(profile);
                user.setProfileCompleted(rs.getBoolean("u.profile_completed"));
                user.setRegistrationCompleted(rs.getBoolean("u.registration_completed"));
                user.setPassword(rs.getString("u.password"));
                return user;
            }, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateGoogleUserProfile(String email, String username, String profileImg) {
        log.info("[updateGoogleUserProfile] Starting");
        String sql = """
      UPDATE tbl_user
         SET username = COALESCE(?, username),
             profile_img = COALESCE(?, profile_img)
       WHERE email = ?
      """;
        jdbcTemplate.update(sql, username, profileImg, email);
    }
     public int markRegistrationAsCompleted(String userId) {
        String sql = "UPDATE tbl_user SET registration_completed = true WHERE id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int markProfileAsCompleted(String userId) {
        String sql = "UPDATE tbl_user SET profile_completed = true WHERE id = ?";
        return jdbcTemplate.update(sql, userId);
    }
}
