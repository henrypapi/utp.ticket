package com.pe.limon.api.gateway.admin.collaborators;

import com.pe.limon.api.transactions.authz.repository.entity.RoleEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("roles")
public class RolesController {

    @GetMapping
    public ResponseEntity<List<RoleEntity>> listRoles() {
        return ResponseEntity.ok().body(new ArrayList<RoleEntity>());
    }
}
