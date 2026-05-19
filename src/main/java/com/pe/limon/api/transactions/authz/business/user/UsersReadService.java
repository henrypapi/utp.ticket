package com.pe.limon.api.transactions.authz.business.user;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.client.users.profile.dto.UserInfoDTO;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersReadService {
    private final UserRepository userRepository;

    public UserInfoDTO getUser(String keySearch) {
        log.info("[getUser] Starting");

        if (keySearch != null && !keySearch.isEmpty()){
            var opt = userRepository.findByEmailOrUsername(keySearch, keySearch);
            return opt.map(userEntity -> new UserInfoDTO(true, userEntity.getId(), keySearch)).orElseGet(() -> new UserInfoDTO(false, "", keySearch));
        }

        throw new BusinessException("Parameter for search is null or empty: "+ keySearch);
    }
}
