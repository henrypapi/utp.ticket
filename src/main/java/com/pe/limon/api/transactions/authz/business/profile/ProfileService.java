package com.pe.limon.api.transactions.authz.business.profile;

import com.pe.limon.api.gateway.client.users.profile.dto.CreateProfileDTO;
import com.pe.limon.api.gateway.client.users.profile.dto.PutProfileDTO;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.transactions.authz.business.profile.async.dto.ProfileImageRequestedEvent;
import com.pe.limon.api.transactions.authz.repository.ProfileRepository;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    @Value("${application.images.directory.user}")
    private String imageUploadPath;

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public ProfileEntity getProfile(String userId){
        ProfileEntity entity = profileRepository.findById(userId).get();
        log.info("[getFullDate] entity {}",entity);
        return entity;
    }

    @Transactional
    public void changeProfileInfo(CreateProfileDTO dto, String userId){
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setUserId(userId);
        profileEntity.setUsername(dto.username());

        var isFileSent = dto.profileImage() != null && !dto.profileImage().isEmpty();
        String fileName = "profile_" + userId + ".jpg";
        if (isFileSent)  profileEntity.setProfileImage(fileName);
        try {
            var updated = profileRepository.update(profileEntity);
            if (updated == 0) throw new BusinessException("Fallo al actualizar el  perfil del usuario.");

        }catch (DuplicateKeyException e){
            throw new BusinessException("Username existente, intenta con otro.");
        }

        try {
            if (isFileSent) {
                byte[] bytes = dto.profileImage().getBytes();
                publisher.publishEvent(new ProfileImageRequestedEvent(fileName, bytes));
            }
        }catch (IOException e){
            log.error("[changeProfileInfo] Error al guardar el imagen de usuario", e);
        }

    }
    
    @Transactional
    public void createProfile(CreateProfileDTO dto, String userId) {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setUserId(userId);
        profileEntity.setUsername(dto.username());
        profileEntity.setRegisteredDateTime(LocalDateTime.now());
        profileEntity.setRegisteredTimestamp(System.currentTimeMillis());

        var isFileSent = dto.profileImage() != null && !dto.profileImage().isEmpty();
        String fileName = "profile_" + userId + ".jpg";
        if (isFileSent)  profileEntity.setProfileImage(fileName);

        try {
            profileRepository.save(profileEntity);
        }catch (DuplicateKeyException e){
            throw new BusinessException("El nombre de usuario ya existe, seleccione otro porfavor.");
        }

        int rowsAffected = userRepository.markProfileAsCompleted(userId);
        if (rowsAffected == 0) throw new BusinessException("Fallo al actualizar el estado del perfil del usuario.");

        try {
            if (isFileSent) {
                byte[] bytes = dto.profileImage().getBytes();
                publisher.publishEvent(new ProfileImageRequestedEvent(fileName, bytes));
            }
        }catch (IOException e){
            log.error("[createProfile] Error al guardar el imagen de usuario", e);
        }

    }
}
