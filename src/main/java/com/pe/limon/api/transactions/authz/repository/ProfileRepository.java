package com.pe.limon.api.transactions.authz.repository;

import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProfileRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    @Value("${application.images.url.image-user}")
    private String urlBaseImage;

    public Optional<ProfileEntity> findById(String userId){
        try {
            String sql = "SELECT " +
                    "user_id, " +
                    "CONCAT(?, profile_image) AS profile_image, " +
                    "username " +
                    "FROM tbl_profile WHERE user_id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(ProfileEntity.class),urlBaseImage,userId));
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public void save(ProfileEntity profileEntity) {
        String sql = "INSERT INTO tbl_profile (user_id, username, display_name, bio, profile_image, cover_image, website_url, registered_datetime, registered_timestamp, updated_datetime, updated_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            profileEntity.getUserId(),
            profileEntity.getUsername(),
            profileEntity.getDisplayName(),
            profileEntity.getBio(),
            profileEntity.getProfileImage(),
            profileEntity.getCoverImage(),
            profileEntity.getWebsiteUrl(),
            profileEntity.getRegisteredDateTime(),
            profileEntity.getRegisteredTimestamp(),
            profileEntity.getUpdatedDateTime(),
            profileEntity.getUpdatedTimestamp()
        );
    }

    public int update(ProfileEntity profileEntity) {
        String sql = "UPDATE tbl_profile SET username = ?,  profile_image = COALESCE(?, profile_image),updated_datetime = ?, updated_timestamp = ? where user_id = ?";
        return jdbcTemplate.update(sql,
                profileEntity.getUsername(),
                profileEntity.getProfileImage(),
                profileEntity.getUpdatedDateTime(),
                profileEntity.getUpdatedTimestamp(),
                profileEntity.getUserId()
                );
    }
}
