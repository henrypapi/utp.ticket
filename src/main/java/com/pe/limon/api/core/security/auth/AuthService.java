package com.pe.limon.api.core.security.auth;

import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.gateway.auth.dto.AuthLoginRequest;
import com.pe.limon.api.gateway.auth.dto.AuthTokensResponse;
import com.pe.limon.api.core.security.jwt.JwtUtil;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.authz.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthStrategyFactory factory;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepo;
    private final UserRepository userRepository;

    @Value("${application.images.url.image-user}") private String urlImgUser;
    @Value("${security.jwt.access-ttl}") private Duration accessTtl;
    @Value("${security.jwt.refresh-ttl}") private Duration refreshTtl;
    @Value("${security.jwt.refresh-ttl-remember}") private Duration refreshTtlRemember;
    @Value("${security.jwt.refresh-cookie-name:rt}") private String refreshCookieName;
    @Value("${security.jwt.refresh-cookie-secure:true}") private boolean refreshCookieSecure;
    @Value("${security.jwt.refresh-cookie-samesite:Lax}") private String refreshCookieSameSite;
    @Value("${security.jwt.refresh-cookie-domain:}") private String refreshCookieDomain;
    @Value("${security.jwt.refresh-cookie-path:}") private String refreshCookiePath;

    public ResponseCookie logout(String userId, String deviceId) {
        sessionRepo.revoke(userId, deviceId);
        return buildRefreshCookie("",Duration.ZERO);
    }

    public AuthTokensResponse issueTokensForUser(UserEntity user) {
        var roles = List.of("USER");
        var pair = jwtUtil.issuePair(user.getEmail(), user.getId(), roles,
                accessTtl.toMillis(), refreshTtl.toMillis());
        String imageUrl = null;
        String username = null;
        if (user.getProfile() != null){
            imageUrl = urlImgUser+"/"+ user.getProfile().getProfileImage();
            username = user.getProfile().getUsername();
        }
        return AuthTokensResponse.builder()
                .tokenType("Bearer")
                .accessToken(pair.accessToken())
                .accessExp(pair.accessExp())
                .refreshToken(pair.refreshToken())
                .refreshExp(pair.refreshExp())
                .user(AuthTokensResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(username)
                    .profileImg(imageUrl)
                    .build())
                .build();
    }

    public ResponseEntity<AuthTokensResponse> authenticateHttp(AuthLoginRequest req, HttpServletRequest httpReq) {
        log.info("Authenticating http request");
        var user = factory.get(req.provider()).authenticate(req);
        log.debug("[authenticateHttp] Get User Id : {}",user);
        boolean remember = Boolean.TRUE.equals(req.rememberDevice());
        var pair = jwtUtil.issuePair(
                user.getEmail(), user.getId(), List.of("USER"),
                accessTtl.toMillis(),
                (remember ? refreshTtlRemember : refreshTtl).toMillis()
        );

        // Registrar/actualizar sesión por dispositivo (hash del refresh)
        String deviceId = Optional.ofNullable(req.deviceId()).orElse(genDeviceId());
        String ua = Optional.ofNullable(httpReq.getHeader("User-Agent")).orElse("n/a");
        String ip = Optional.ofNullable(httpReq.getRemoteAddr()).orElse("0.0.0.0");
        log.info("[authenticateHttp] deviceId: {}, ua: {}, ip: {}]", deviceId, ua, ip);
        log.info("[authenticateHttp] remember: {}]", remember);
        var sessionId = sessionRepo.saveOrUpdate(
                user.getId(),
                deviceId,
                hash(pair.refreshToken()),
                ua, hash(ip), Instant.ofEpochSecond(pair.refreshExp()),remember);

        // Setear cookie HttpOnly con el refresh (para "recordar")
        ResponseCookie cookie = buildRefreshCookie(pair.refreshToken(), remember ? refreshTtlRemember : refreshTtl);
        String imageUrl = null;
        String username = null;
        if (user.getProfile() != null){
            imageUrl = urlImgUser+"/"+ user.getProfile().getProfileImage();
            username = user.getProfile().getUsername();
        }
        // Puedes **omitir** el refresh del body si usas cookie; te dejo ambos por compatibilidad
        var body = AuthTokensResponse.builder()
                .tokenType("Bearer")
                .accessToken(pair.accessToken()).accessExp(pair.accessExp())
                .user(new AuthTokensResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        username,
                        imageUrl,
                        true,
                        true,
                        user.isProfileCompleted(),
                        user.isRegistrationCompleted()))
                .deviceId(deviceId)         // <-- para que Angular lo guarde
                .sessionId(sessionId)
                .remember(remember)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    private String genDeviceId() { return UUID.randomUUID().toString(); }
    private String hash(String s) { return BCrypt.hashpw(s, BCrypt.gensalt()); }
    public ResponseEntity<AuthTokensResponse> refresh(String refreshCookie,
                                                      String deviceId,
                                                      HttpServletRequest httpReq) {
        log.info("[refresh] Starting");
        if (refreshCookie == null || deviceId == null) return ResponseEntity.status(401).build();
        log.info("[refresh] Params Valid ... ");


        log.debug("[refresh] isTokenValid {}",jwtUtil.isTokenValid(refreshCookie));
        log.debug("[refresh] isRefreshToken {}",jwtUtil.isRefreshToken(refreshCookie));
        if (!jwtUtil.isTokenValid(refreshCookie) || !jwtUtil.isRefreshToken(refreshCookie)) return ResponseEntity.status(401).build();
        log.info("[refresh] Token  Valid ... ");

        String email  = jwtUtil.extractUsername(refreshCookie);
        String userId = jwtUtil.extractUserId(refreshCookie);

        var sessionOpt = sessionRepo.findByUserAndDevice(userId, deviceId);
        if (sessionOpt.isEmpty() || sessionOpt.get().isRevoked()) return ResponseEntity.status(401).build();

        var session = sessionOpt.get();
        if (session.getExpiresAt().isBefore(Instant.now())) return ResponseEntity.status(401).build();

        if (!BCrypt.checkpw(refreshCookie, session.getRefreshHash())) {
            // hash mismatch → posible robo → revoca
            sessionRepo.revoke(userId, deviceId);
            return ResponseEntity.status(401).build();
        }

        boolean remember = Boolean.TRUE.equals(session.getRemember());
        var pair = jwtUtil.issuePair(email, userId, java.util.List.of("USER"),
                accessTtl.toMillis(),
                (remember ? refreshTtlRemember : refreshTtl).toMillis());

        String ua = Optional.ofNullable(httpReq.getHeader("User-Agent")).orElse("n/a");
        String ip = Optional.ofNullable(httpReq.getRemoteAddr()).orElse("0.0.0.0");
        sessionRepo.saveOrUpdate(userId, deviceId,
                hash(pair.refreshToken()),  // <-- tu helper
                ua, hash(ip),
                Instant.ofEpochSecond(pair.refreshExp()),remember);

        ResponseCookie cookie = buildRefreshCookie(pair.refreshToken(), remember ? refreshTtlRemember : refreshTtl);
        var userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Not found email: "+email));
        String imageUrl = null;
        String username = null;
        if (userEntity.getProfile() != null){
            imageUrl = urlImgUser+"/"+ userEntity.getProfile().getProfileImage();
            username = userEntity.getProfile().getUsername();
        }

        var body = AuthTokensResponse.builder()
                .tokenType("Bearer")
                .accessToken(pair.accessToken()).accessExp(pair.accessExp())
                .user(
                        new AuthTokensResponse.UserInfo(
                                userId,email, username,
                                imageUrl,
                                true,
                                true,
                                userEntity.isProfileCompleted(),
                                userEntity.isRegistrationCompleted()))
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    private ResponseCookie buildRefreshCookie(String token, Duration ttl) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .maxAge(ttl);

        if ("None".equalsIgnoreCase(refreshCookieSameSite)) {
            b = b.sameSite("None");
        } else if ("Lax".equalsIgnoreCase(refreshCookieSameSite)) {
            b = b.sameSite("Lax");
        } else {
            b = b.sameSite("Strict");
        }

        // Domain (solo setear si no está en blanco)
        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            b = b.domain(refreshCookieDomain);
        }

        return b.build();
    }
}
