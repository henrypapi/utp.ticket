package com.pe.limon.api.gateway.auth;

import com.pe.limon.api.gateway.auth.dto.AllPermsResponseDTO;
import com.pe.limon.api.gateway.auth.dto.AuthLoginRequest;
import com.pe.limon.api.gateway.auth.dto.AuthTokensResponse;
import com.pe.limon.api.gateway.auth.dto.FormUserDTO;
import com.pe.limon.api.core.security.auth.AuthService;
import com.pe.limon.api.transactions.authz.business.permissions.IPermissionService;
import com.pe.limon.api.transactions.authz.business.user.UserRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRegisterService userRegisterService;
    @Value("${security.jwt.refresh-cookie-name:rt}") private String refreshCookieName;
    private final IPermissionService permissionService;

    @PostMapping(value="/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthTokensResponse> login(
            @RequestBody @Valid AuthLoginRequest request,
            HttpServletRequest httpReq
    ) {
        log.info("[login] Starting");
        return authService.authenticateHttp(request, httpReq);
    }

    @PostMapping(value="/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthTokensResponse> refresh(
            @CookieValue(name = "${security.jwt.refresh-cookie-name:rt}", required = false) String refreshCookie,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Requested-With", required = false) String xrw,
            HttpServletRequest httpReq
    ) {
        log.info("[refresh] Starting");
        if (refreshCookie == null) {
            log.error("[refresh] refreshCookie null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var xrwVal = Optional.ofNullable(xrw).map(String::toLowerCase).orElse("");
        if (!"xmlhttprequest".equals(xrwVal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        var res = authService.refresh(refreshCookie, deviceId, httpReq);

        // Si el service decidió revocar (por mismatch), puedes unificar borrado cookie:
        if (res.getStatusCodeValue() == 401) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie().toString())
                    .build();
        }
        return res;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody FormUserDTO request) {
        log.info("[register] Starting");
        var entity = userRegisterService.registerUser(request);
        return ResponseEntity.ok(authService.issueTokensForUser(entity));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("X-Device-Id") String deviceId,
            HttpServletRequest req
    ) {
        log.info("[logout] Starting");

        var userId = (String) req.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        var cleared = authService.logout(userId, deviceId);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    private ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true).secure(true).sameSite("Lax")
                .path("/").maxAge(Duration.ZERO)
                .build();
    }




    /**
     * Lista todos los eventos a los que el usuario tiene acceso, con sus roles y permisos efectivos.
     */
    @GetMapping("/effective/all")
    public ResponseEntity<AllPermsResponseDTO> getAllEffective(
            @RequestAttribute String userId
    ) {
        var map = permissionService.getAllEffective(userId);
        return ResponseEntity.ok(new AllPermsResponseDTO(userId, System.currentTimeMillis() / 1000, map));
    }

}
