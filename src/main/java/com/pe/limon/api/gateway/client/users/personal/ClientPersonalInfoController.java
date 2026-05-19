package com.pe.limon.api.gateway.client.users.personal;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.gateway.client.users.personal.dto.PersonalInfoDTO;
import com.pe.limon.api.transactions.authz.business.user.PersonalInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/personal-info")
@RequiredArgsConstructor
public class ClientPersonalInfoController {

    private final PersonalInfoService personalInfoService;

    @PostMapping
    public ResponseEntity<MessageDTO> savePersonalInfo(
        @Valid @RequestBody PersonalInfoDTO personalInfoDto
        ,@RequestAttribute(name = "userId") String userId) {
        personalInfoService.savePersonalInfo(personalInfoDto, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }

    @GetMapping
    public ResponseEntity<?> getPersonalInfo(@RequestAttribute String userId) {
        return ResponseEntity.ok(personalInfoService.getPersonalInfo(userId));
    }

}