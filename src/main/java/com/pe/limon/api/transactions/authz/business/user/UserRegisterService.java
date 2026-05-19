package com.pe.limon.api.transactions.authz.business.user;

import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.gateway.auth.dto.FormUserDTO;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.codes.GenerateCodes;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = {DuplicateKeyException.class, BusinessException.class, Exception.class})
    public UserEntity registerUser(FormUserDTO userDTO) {
        var entity = new UserEntity();
        String uniqueId = GenerateCodes.randomStringId(12);
        entity.setId(uniqueId);
        entity.setEmail(userDTO.email());
        entity.setPassword(passwordEncoder.encode(userDTO.password()));
        entity.setRegistrationCompleted(false);
        entity.setProfileCompleted(false);
        entity.setEmailVerified(false);
        entity.setPhoneVerified(false);
        entity.setProvider("P");
        entity.setStatus(StatusEnum.ACTIVE.getCode());
        entity.setRegisteredDatetime(LocalDateTime.now());
        entity.setRegisteredTimestamp(System.currentTimeMillis());
        userRepository.insertUser(entity);
        return entity;
    }

    public UserEntity upsertGoogleUser(String email) {
        log.info("[upsertGoogleUser] Starting");
        var opt = userRepository.findByEmail(email);
        UserEntity user;
        if (opt.isEmpty()) {
            user = new UserEntity();
            String uniqueId = GenerateCodes.randomStringId(12);
            user.setId(uniqueId);
            user.setEmail(email);
            user.setRegistrationCompleted(false);
            user.setProfileCompleted(false);
            user.setEmailVerified(true);
            user.setPhoneVerified(false);
            user.setProvider("G");
            user.setPassword("{nohoop}");
            user.setStatus(StatusEnum.ACTIVE.getCode());
            user.setRegisteredDatetime(LocalDateTime.now());
            user.setRegisteredTimestamp(System.currentTimeMillis());
            userRepository.insertUser(user);
            return user;
        }

        return opt.get();
    }
}
