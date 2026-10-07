package pe.ferresur.model;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor");

    private final String nombre;
    Rol(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
}
