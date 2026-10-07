package pe.ferresur.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.ferresur.repository.UsuarioRepository;

import java.io.IOException;

@Component
public class RoleAwareSuccessHandler implements AuthenticationSuccessHandler {
    private final UsuarioRepository repo;

    public RoleAwareSuccessHandler(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String selected = request.getParameter("rol");
        var usuario = repo.findByUsernameIgnoreCase(authentication.getName()).orElse(null);

        if (usuario == null || selected == null || !selected.equals(usuario.getRol().name())) {
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();
            response.sendRedirect("/login?roleError=true");
            return;
        }
        response.sendRedirect("/dashboard");
    }
}
