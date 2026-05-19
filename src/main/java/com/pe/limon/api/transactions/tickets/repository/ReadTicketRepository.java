package com.pe.limon.api.transactions.tickets.repository;

import com.pe.limon.api.transactions.authz.repository.entity.CollaboratorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReadTicketRepository {
    private final NamedParameterJdbcTemplate namedJdbc;
    public CollaboratorEntity findById(CollaboratorEntity entity){
        try {
            String sql = """
                SELECT *
                FROM tbl_ticket t
                INNER JOIN  tbl_event_collaborator ec
                ON c.id = ec.collaborator_id
                WHERE id = @id AND owner_user_id = @ownerUserId
            """;
            return namedJdbc.queryForObject(sql,new BeanPropertySqlParameterSource(entity), new BeanPropertyRowMapper<>(CollaboratorEntity.class));
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }
}
