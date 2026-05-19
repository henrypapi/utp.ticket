package com.pe.limon.api.transactions.promoters.repository;

import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.authz.business.permissions.EffectivePerm;
import com.pe.limon.api.transactions.authz.repository.entity.PersonalInfoEntity;
import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PromoterRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PageResult<PromoterEntity> findByEventIdWithFilters(Long eventId, String username, String code, int page, int size) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                ep.id,
                ep.event_id,
                ep.code,
                ep.promoter_user_id,
                ep.max_uses,
                ep.total_sales_count,
                ep.total_sales_amount,
                ep.active,
                ep.registered_datetime,
                ep.registered_timestamp,
                ep.registered_by,
                pi.email,
                p.profile_image,
                p.username,
                pi.first_name,
                pi.phone_number,
                pi.last_name
            FROM tbl_event_promoter ep
            LEFT JOIN tbl_profile p ON p.user_id = ep.promoter_user_id
            LEFT JOIN tbl_personal_info pi ON pi.user_id = ep.promoter_user_id
            WHERE ep.event_id = :eventId
        """);

        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*)
            FROM tbl_event_promoter ep
            LEFT JOIN tbl_profile p ON p.user_id = ep.promoter_user_id
            LEFT JOIN tbl_personal_info pi ON pi.user_id = ep.promoter_user_id
            WHERE ep.event_id = :eventId
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventId", eventId);

        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND p.username = :username");
            countSql.append(" AND p.username = :username");
            params.addValue("username", username);
        }

        if (code != null && !code.trim().isEmpty()) {
            sql.append(" AND ep.code = :code");
            countSql.append(" AND ep.code = :code");
            params.addValue("code", code);
        }

        sql.append(" ORDER BY ep.total_sales_amount DESC LIMIT :limit OFFSET :offset");

        int offset = (page - 1) * size;
        params.addValue("limit", size);
        params.addValue("offset", offset);

        log.info("[findByEventIdWithFilters] Main query: {}", sql);
        log.info("[findByEventIdWithFilters] Count query: {}", countSql);
        log.info("[findByEventIdWithFilters] Params: {}", params);

        List<PromoterEntity> list = namedParameterJdbcTemplate.query(sql.toString(), params,
                (rs, rowNum) -> {
                    PromoterEntity entity = new PromoterEntity();
                    entity.setId(rs.getLong("ep.id"));
                    entity.setEventId(rs.getLong("event_id"));
                    entity.setCode(rs.getString("code"));
                    entity.setPromoterUserId(rs.getString("promoter_user_id"));
                    entity.setMaxUses(rs.getInt("max_uses"));
                    entity.setTotalSalesCount(rs.getInt("total_sales_count"));
                    entity.setTotalSalesAmount(rs.getBigDecimal("total_sales_amount"));
                    entity.setActive(rs.getBoolean("active"));
                    entity.setRegisteredDatetime(rs.getTimestamp("registered_datetime").toLocalDateTime());
                    entity.setRegisteredTimestamp(rs.getLong("registered_timestamp"));
                    entity.setRegisteredBy(rs.getString("registered_by"));

                    // Set user details
                    UserEntity user = new UserEntity();

                    ProfileEntity profileEntity = new ProfileEntity();
                    profileEntity.setProfileImage(rs.getString("profile_image"));
                    profileEntity.setUsername(rs.getString("username"));

                    PersonalInfoEntity personalInfoEntity = new PersonalInfoEntity();

                    personalInfoEntity.setFirstName(rs.getString("first_name"));
                    personalInfoEntity.setLastName(rs.getString("last_name"));
                    personalInfoEntity.setEmail(rs.getString("email"));
                    personalInfoEntity.setPhoneNumber(rs.getString("phone_number"));

                    user.setPersonalInfo(personalInfoEntity);
                    user.setProfile(profileEntity);
                    entity.setPromoterUser(user);

                    return entity;
                }
        );

        Long total = namedParameterJdbcTemplate.queryForObject(countSql.toString(), params, Long.class);

        return new PageResult<>(list, page, size, total);
    }


    public List<EffectivePerm> findEventByUserId(String userId) {
        String sql = """
                    SELECT
                        ep.id,
                        ep.event_id,
                        e.name as eventName,
                        e.time_zone,
                        ep.code,
                        ep.promoter_user_id,
                        ep.max_uses,
                        ep.total_sales_count,
                        ep.total_sales_amount,
                        ep.active
                    FROM tbl_event_promoter ep
                    INNER JOIN tbl_event e ON e.id = ep.event_id
                    WHERE ep.promoter_user_id = :userId
                """;


        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
       return namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> new EffectivePerm(
                        rs.getLong("event_id"),
                        rs.getString("eventName"),
                        null,
                        null,
                        rs.getString("time_zone"),
                        false,
                        false,
                        false,
                        Set.of("promoters")
                )
        );
    }

    public PromoterEntity findByEventIdAndCode(Long eventId, String code, String userId) {
        String sql = """
            SELECT
                ep.id,
                ep.event_id,
                ep.code,
                ep.promoter_user_id,
                ep.max_uses,
                ep.total_sales_count,
                ep.total_sales_amount,
                ep.total_sales_details,
                ep.active,
                ep.registered_datetime,
                ep.registered_timestamp,
                ep.registered_by,
                ep.total_uses
            FROM tbl_event_promoter ep
            WHERE ep.event_id = :eventId AND ep.code = :code AND (:userId IS NULL OR ep.promoter_user_id = :userId)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventId", eventId)
                .addValue("code", code)
                .addValue("userId",userId);

        log.info("[findByEventIdAndCode] Query: {}", sql);
        log.info("[findByEventIdAndCode] Params: {}", params);

        List<PromoterEntity> list = namedParameterJdbcTemplate.query(sql.toString(), params,
               new BeanPropertyRowMapper<>(PromoterEntity.class)
        );

        return list.isEmpty() ? null : list.get(0);
    }

    public void save(PromoterEntity entity) {
        String sql = """
            INSERT INTO tbl_event_promoter (
                event_id, code, promoter_user_id, max_uses, total_sales_count,
                total_sales_amount, total_sales_details, active, registered_datetime, registered_timestamp, registered_by
            ) VALUES (
                :eventId, :code, :promoterUserId, :maxUses, 0,
                0, '{}', :active, :registeredDatetime, :registeredTimestamp, :registeredBy
            )
        """;


        log.info("[save] Query: {}", sql);
        log.info("[save] Params: {}", entity.toString());

        namedParameterJdbcTemplate.update(sql,new BeanPropertySqlParameterSource(entity));
    }

    public void update(PromoterEntity entity) {
        String sql = """
            UPDATE tbl_event_promoter
            SET max_uses = :maxUses, active = :active
            WHERE event_id = :eventId AND id = :id
        """;

        log.info("[update] Query: {}", sql);
        log.info("[update] Params: {}", entity.toString());

        namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity));
    }

    public int updateTotalSales(PromoterEntity entity) {
        String sql = """
            UPDATE tbl_event_promoter
            SET total_sales_amount = :totalSalesAmount,
            total_sales_count = :totalSalesCount,
            total_sales_details = :totalSalesDetails,
            total_uses = total_uses + 1
            WHERE event_id = :eventId AND id = :id AND active = 1  AND (max_uses IS NULL OR (total_uses + 1) <= max_uses);
        """;

        return namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity));
    }
}
