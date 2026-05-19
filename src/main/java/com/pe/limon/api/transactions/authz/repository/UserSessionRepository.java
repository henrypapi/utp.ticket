package com.pe.limon.api.transactions.authz.repository;

import com.pe.limon.api.transactions.authz.repository.entity.UserSessionEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserSessionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final RowMapper<UserSessionEntity> MAPPER = new RowMapper<>() {
        @Override public UserSessionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            var s = new UserSessionEntity();
            s.setId(rs.getString("id"));
            s.setUserId(rs.getString("user_id"));
            s.setDeviceId(rs.getString("device_id"));
            s.setRefreshHash(rs.getString("refresh_hash"));
            s.setUserAgent(rs.getString("user_agent"));
            s.setIpHash(rs.getString("ip_hash"));
            s.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
            s.setRevoked(rs.getBoolean("revoked"));
            Boolean rem = (Boolean) rs.getObject("remember");
            s.setRemember(rem);
            return s;
        }
    };

    /** Inserta o actualiza la sesión del dispositivo (rotación de refresh). */
    public String saveOrUpdate(String userId, String deviceId, String refreshHash,
                             String userAgent, String ipHash, Instant expiresAt, boolean remember) {

        log.info("[saveOrUpdate] Starting UserSession");
        var existing = findByUserAndDevice(userId, deviceId);
        String id = existing.map(UserSessionEntity::getId).orElse(UUID.randomUUID().toString());

        String sql = """
          INSERT INTO tbl_user_session (id, user_id, device_id, refresh_hash, user_agent, ip_hash, expires_at, revoked,remember, created_at)
          VALUES (:id, :userId, :deviceId, :refreshHash, :userAgent, :ipHash, :expiresAt, 0, :remember, NOW())
          ON DUPLICATE KEY UPDATE
            refresh_hash = :refreshHash,
            user_agent   = :userAgent,
            ip_hash      = :ipHash,
            expires_at   = :expiresAt,
            revoked      = 0,
            remember     = :remember
          """;
        var p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("userId", userId)
                .addValue("deviceId", deviceId)
                .addValue("refreshHash", refreshHash)
                .addValue("userAgent", userAgent)
                .addValue("ipHash", ipHash)
                .addValue("expiresAt", Timestamp.from(expiresAt))
                .addValue("remember", remember);
        jdbc.update(sql, p);
        return id;
    }

    public Optional<UserSessionEntity> findByUserAndDevice(String userId, String deviceId) {
        String sql = """
      SELECT id, user_id, device_id, refresh_hash, user_agent, ip_hash, expires_at, revoked, remember
      FROM tbl_user_session
      WHERE user_id=:userId AND device_id=:deviceId
      LIMIT 1
      """;
        var p = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("deviceId", deviceId);
        return jdbc.query(sql, p, MAPPER).stream().findFirst();
    }

    /** Revoca la sesión en ese dispositivo. */
    public int revoke(String userId, String deviceId) {
        String sql = """
      UPDATE tbl_user_session
      SET revoked=1, expires_at=NOW()
      WHERE user_id=:userId AND device_id=:deviceId
      """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("deviceId", deviceId));
    }

    /** Revoca todas las sesiones del usuario. */
    public int revokeAll(String userId) {
        String sql = "UPDATE tbl_user_session SET revoked=1, expires_at=NOW() WHERE user_id=:userId";
        return jdbc.update(sql, new MapSqlParameterSource().addValue("userId", userId));
    }
}