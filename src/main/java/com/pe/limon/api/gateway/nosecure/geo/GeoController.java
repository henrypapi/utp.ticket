package com.pe.limon.api.gateway.nosecure.geo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/public/geocode")
@RequiredArgsConstructor
public class GeoController {
    private final RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<?> searchLocation(@RequestParam String query) {
        String fullUrl = "https://nominatim.openstreetmap.org/search?q=" + query +
                "&format=json&addressdetails=1&limit=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Lim-On-Backend/1.0 (contacto@limon.com)");
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = new RestTemplate().exchange(
                fullUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        return ResponseEntity.ok(response.getBody());
    }
}
