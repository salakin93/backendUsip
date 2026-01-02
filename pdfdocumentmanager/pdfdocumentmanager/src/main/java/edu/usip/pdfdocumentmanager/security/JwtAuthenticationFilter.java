package edu.usip.pdfdocumentmanager.security;

import edu.usip.pdfdocumentmanager.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());
        try {
            String phone = jwtUtil.extractUsername(token);

            var user = userService.getAllUsers().stream()
                    .filter(u -> u.getPhone().equals(phone))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!user.isActive()) {
                throw new RuntimeException("Usuario desactivado");
            }

            Set<SimpleGrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
                    .map(r -> new SimpleGrantedAuthority(r.name().replace("ROLE_", "")))
                    .collect(Collectors.toSet());

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(phone, null, authorities)
            );

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            // Configurar JSON de error
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}"
            );
            return; // detener la cadena de filtros
        }

        chain.doFilter(request, response);
    }
}
