package com.pe.limon.api.core.aop;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Profile("qa")
@Component
public class RequestAop implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestAop.class);

    @Value("${application.front.url.complete-profile}")
    private String redirectUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        log.debug("Init doFilter {}",requestURI);

        if (requestURI.startsWith("/api-limon/v1/public")){
            log.debug("This endpoint is public {}",requestURI);
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();
        String userId = (String) session.getAttribute("userId");
        Boolean profileCompleted = (Boolean) session.getAttribute("profileCompleted");

        if (profileCompleted == null || userId==null || userId.isEmpty()) {
            String userDefault = "dev";
            httpRequest.setAttribute("userId", userDefault);
            return;
        }

        httpRequest.setAttribute("userId",userId);

        List<String> urisAllowed = new ArrayList<>();
        urisAllowed.add("/api-limon/v1/auth/session-status");
        urisAllowed.add("/api-limon/v1/profile/complete");

        if (!urisAllowed.contains(requestURI) && !profileCompleted){
            log.debug("Redirecting to {}", redirectUrl);
            httpResponse.setStatus(HttpServletResponse.SC_FOUND);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"message\": \"Profile not completed\", \"redirectUrl\": \"" + redirectUrl + "\"}");
            return;
        }

        log.debug("No Redirecting for {}",requestURI);
        chain.doFilter(request, response);
    }
}