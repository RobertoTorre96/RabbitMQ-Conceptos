package com.estudio.rabbit.json;

public class Pedido {
    private int id;
    private String producto;
    private double precio;

    public Pedido() { } // constructor vacío: OBLIGATORIO para Jackson

    public Pedido(int id, String producto, double precio) {
        this.id = id;
        this.producto = producto;
        this.precio = precio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", producto='" + producto + "', precio=$" + precio + "}";
    }
}
