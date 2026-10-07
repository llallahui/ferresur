package pe.ferresur.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.ferresur.model.Rol;
import pe.ferresur.model.Usuario;
import pe.ferresur.repository.UsuarioRepository;

@Controller @RequestMapping("/usuarios")
public class UsuarioController {
 private final UsuarioRepository repo; private final PasswordEncoder encoder;
 public UsuarioController(UsuarioRepository repo,PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
 @GetMapping public String listar(Model model){model.addAttribute("usuarios",repo.findAll());model.addAttribute("stockBajoCount",0);model.addAttribute("total",repo.count());model.addAttribute("admins",repo.countByRol(Rol.ADMINISTRADOR));model.addAttribute("vendedores",repo.countByRol(Rol.VENDEDOR));model.addAttribute("activos",repo.countByActivoTrue());return "usuarios/index";}
 @GetMapping("/nuevo") public String nuevo(Model model){model.addAttribute("usuario",new Usuario());model.addAttribute("stockBajoCount",0);model.addAttribute("roles",Rol.values());model.addAttribute("edicion",false);return "usuarios/form";}
 @GetMapping("/editar/{id}") public String editar(@PathVariable Long id,Model model){Usuario u=repo.findById(id).orElseThrow();u.setPassword("");u.setPasswordConfirm("");model.addAttribute("usuario",u);model.addAttribute("stockBajoCount",0);model.addAttribute("roles",Rol.values());model.addAttribute("edicion",true);return "usuarios/form";}
 @PostMapping("/guardar") public String guardar(@Valid @ModelAttribute("usuario") Usuario usuario,BindingResult br,Model model){
  usuario.setUsername(normalize(usuario.getUsername())); Usuario existente=usuario.getId()==null?null:repo.findById(usuario.getId()).orElseThrow();
  repo.findByUsernameIgnoreCase(usuario.getUsername()).ifPresent(found->{if(usuario.getId()==null||!found.getId().equals(usuario.getId()))br.rejectValue("username","duplicate","Ese usuario ya existe.");});
  if(usuario.getRol()==null) br.rejectValue("rol","required","Seleccione un rol.");
  if(existente==null){ if(usuario.getPassword()==null||usuario.getPassword().isBlank()) br.rejectValue("password","required","La contraseña es obligatoria."); else validarPassword(usuario,br); }
  // Por seguridad, un administrador nunca cambia aquí la contraseña de otro usuario. El cambio se hace desde /perfil.
  if(br.hasErrors()){model.addAttribute("roles",Rol.values());model.addAttribute("edicion",usuario.getId()!=null);return "usuarios/form";}
  if(existente==null){usuario.setPassword(encoder.encode(usuario.getPassword()));usuario.setActivo(true);} else {usuario.setPassword(existente.getPassword());usuario.setFechaRegistro(existente.getFechaRegistro());usuario.setActivo(existente.isActivo());}
  usuario.setPasswordConfirm(null);repo.save(usuario);return "redirect:/usuarios";
 }
 private void validarPassword(Usuario u,BindingResult br){if(u.getPassword().length()<6)br.rejectValue("password","short","Mínimo 6 caracteres.");if(u.getPasswordConfirm()==null||!u.getPassword().equals(u.getPasswordConfirm()))br.rejectValue("passwordConfirm","mismatch","Las contraseñas no coinciden.");}
 @PostMapping("/{id}/toggle") public String toggle(@PathVariable Long id,Authentication auth){repo.findById(id).ifPresent(u->{if(!u.getUsername().equalsIgnoreCase(auth.getName())){u.setActivo(!u.isActivo());repo.save(u);}});return "redirect:/usuarios";}
 private String normalize(String s){return s==null?null:s.trim().toLowerCase();}
}
