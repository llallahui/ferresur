package pe.ferresur.controller;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pe.ferresur.model.Rol;
import pe.ferresur.model.Usuario;
import pe.ferresur.repository.UsuarioRepository;

@Controller
public class AuthController {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public AuthController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("roles", Rol.values());
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String crear(@Valid @ModelAttribute("usuario") Usuario usuario,
                        BindingResult br,
                        Model model) {
        usuario.setUsername(normalizeUsername(usuario.getUsername()));

        if (usuario.getRol() == null) {
            br.rejectValue("rol", "required", "Seleccione un rol.");
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            br.rejectValue("password", "short", "La contraseña debe tener al menos 6 caracteres.");
        }
        if (usuario.getPassword() != null && (usuario.getPasswordConfirm() == null ||
                !usuario.getPassword().equals(usuario.getPasswordConfirm()))) {
            br.rejectValue("passwordConfirm", "mismatch", "Las contraseñas no coinciden.");
        }
        if (repo.findByUsernameIgnoreCase(usuario.getUsername()).isPresent()) {
            br.rejectValue("username", "duplicate", "Ese usuario ya existe.");
        }

        if (br.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "auth/registro";
        }

        usuario.setActivo(true);
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        usuario.setPasswordConfirm(null);
        repo.save(usuario);
        return "redirect:/login?registered=true";
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }
}
