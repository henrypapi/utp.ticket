package com.pe.limon.api.gateway.client.users.profile;


import com.pe.limon.api.gateway.client.users.profile.dto.CreateProfileDTO;
import com.pe.limon.api.gateway.client.users.profile.dto.PutProfileDTO;
import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.transactions.authz.business.profile.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/profile")
@RequiredArgsConstructor
@Slf4j
public class ClientProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestAttribute String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<?> putProfile(@Valid @ModelAttribute CreateProfileDTO dto, @RequestAttribute String userId) {
        profileService.changeProfileInfo(dto, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }
    @PostMapping
    public ResponseEntity<MessageDTO> createProfile(
        @Valid @ModelAttribute CreateProfileDTO dto,
        @RequestAttribute(name = "userId") String userId) {
        log.info("[createProfile] Creating profile: {}", dto);
        profileService.createProfile(dto, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }
}
