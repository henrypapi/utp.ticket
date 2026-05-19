package com.pe.limon.api.transactions.authz.business.user;

import com.pe.limon.api.gateway.client.users.personal.dto.PersonalInfoDTO;
import com.pe.limon.api.transactions.authz.repository.PersonalInfoRepository;
import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.pe.limon.api.core.utils.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.authz.repository.entity.PersonalInfoEntity;

@Service
@RequiredArgsConstructor
public class PersonalInfoService {

    @Value("${application.images.directory.user}")
    private String imageUploadPath;

    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;
    

    @Transactional
    public void savePersonalInfo(PersonalInfoDTO personalInfoDTO, String userId) {

        UserEntity user = userRepository.findById(userId);
        PersonalInfoEntity personalInfoEntity = new PersonalInfoEntity();
        personalInfoEntity.setUserId(userId);
        personalInfoEntity.setFirstName(personalInfoDTO.firstName());
        personalInfoEntity.setLastName(personalInfoDTO.lastName());
        personalInfoEntity.setGender(personalInfoDTO.gender());
        personalInfoEntity.setDepartment(personalInfoDTO.department());
        personalInfoEntity.setProvince(personalInfoDTO.province());
        personalInfoEntity.setDistrict(personalInfoDTO.district());
        personalInfoEntity.setAddress(personalInfoDTO.address());
        personalInfoEntity.setPhoneNumber(personalInfoDTO.phoneNumber());
        personalInfoEntity.setDocumentType(personalInfoDTO.documentType());
        personalInfoEntity.setDocumentNumber(personalInfoDTO.documentNumber());
        personalInfoEntity.setBirthDate(personalInfoDTO.birthDate());
        personalInfoEntity.setEmail(user.getEmail());
        personalInfoEntity.setRegisteredDatetime(LocalDateTime.now());
        personalInfoEntity.setRegisteredTimestamp(System.currentTimeMillis());

        personalInfoRepository.save(personalInfoEntity);

        int rowsAffected = userRepository.markRegistrationAsCompleted(userId);
        if (rowsAffected == 0) {
            throw new BusinessException("Failed to update user registration status.");
        }
    }

    public PersonalInfoEntity getPersonalInfo(String userId){
        return personalInfoRepository.findByUserId(userId);
    }

}