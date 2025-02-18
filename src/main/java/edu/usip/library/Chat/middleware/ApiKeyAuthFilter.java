package edu.usip.library.Chat.middleware;

import edu.usip.library.Chat.model.User;
import edu.usip.library.Chat.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import java.io.IOException;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends GenericFilterBean {

    private final UserRepository userRepository;

    @Autowired
    public ApiKeyAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Obtener la clave API desde el header
        String apiKey = httpRequest.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key is missing");
            return;
        }

        // Buscar al usuario asociado con esa API Key
        Optional<User> user = userRepository.findByApiKey(apiKey);

        if (user.isEmpty()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return;
        }

        // Asignar el usuario al contexto de la solicitud
        httpRequest.setAttribute("user", user.get());

        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }
}
