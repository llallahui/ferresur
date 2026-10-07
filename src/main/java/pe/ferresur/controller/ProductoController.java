package pe.ferresur.controller;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.ferresur.model.Producto;
import pe.ferresur.repository.CategoriaRepository;
import pe.ferresur.repository.ProductoRepository;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoRepository repo;
    private final CategoriaRepository cats;

    public ProductoController(ProductoRepository repo, CategoriaRepository cats) {
        this.repo = repo;
        this.cats = cats;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Long categoria,
                         Model model) {
        var todos = repo.findByActivoTrueOrderByNombreAsc();
        var filtrados = todos.stream()
                .filter(p -> q == null || q.isBlank()
                        || p.getNombre().toLowerCase().contains(q.toLowerCase())
                        || p.getCodigo().toLowerCase().contains(q.toLowerCase()))
                .filter(p -> categoria == null ||
                        (p.getCategoria() != null && p.getCategoria().getId().equals(categoria)))
                .toList();
        model.addAttribute("productos", filtrados);
        model.addAttribute("stockBajoCount", repo.countStockBajo());
        model.addAttribute("categorias", cats.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoriaSeleccionada", categoria);
        return "productos/index";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Producto p = new Producto();
        p.setCategoriaId(null);
        model.addAttribute("producto", p);
        model.addAttribute("stockBajoCount", repo.countStockBajo());
        model.addAttribute("categorias", cats.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("categoriaSeleccionada", null);
        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Producto producto = repo.findById(id).orElseThrow();
        model.addAttribute("producto", producto);
        model.addAttribute("stockBajoCount", repo.countStockBajo());
        model.addAttribute("categorias", cats.findByActivoTrueOrderByNombreAsc());
        producto.setCategoriaId(producto.getCategoria() == null ? null : producto.getCategoria().getId());
        model.addAttribute("categoriaSeleccionada", producto.getCategoriaId());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto p,
                          BindingResult br,
                          @RequestParam(required = false) Long categoriaId,
                          Model model) {
        p.setCodigo(p.getCodigo() == null ? null : p.getCodigo().trim().toUpperCase());
        if (categoriaId == null) {
            br.rejectValue("categoriaId", "required", "Seleccione una categoría.");
        } else {
            p.setCategoria(cats.findById(categoriaId).orElse(null));
            if (p.getCategoria() == null) {
                br.rejectValue("categoriaId", "invalid", "La categoría seleccionada no existe.");
            }
        }
        if (repo.findAll().stream().anyMatch(x -> x.getCodigo().equalsIgnoreCase(p.getCodigo())
                && (p.getId() == null || !x.getId().equals(p.getId())))) {
            br.rejectValue("codigo", "duplicate", "Ya existe un producto con ese código.");
        }
        if (br.hasErrors()) {
            model.addAttribute("categorias", cats.findByActivoTrueOrderByNombreAsc());
            p.setCategoriaId(categoriaId);
            model.addAttribute("categoriaSeleccionada", categoriaId);
            return "productos/form";
        }
        try {
            repo.save(p);
        } catch (DataIntegrityViolationException ex) {
            br.rejectValue("codigo", "duplicate", "El código ya está registrado.");
            model.addAttribute("categorias", cats.findByActivoTrueOrderByNombreAsc());
            p.setCategoriaId(categoriaId);
            model.addAttribute("categoriaSeleccionada", categoriaId);
            return "productos/form";
        }
        return "redirect:/productos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        repo.findById(id).ifPresent(p -> {
            p.setActivo(false);
            repo.save(p);
        });
        return "redirect:/productos";
    }
}
