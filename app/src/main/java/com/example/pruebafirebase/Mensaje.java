package com.example.pruebafirebase;

public class Mensaje {
    public String nombre;
    public String fecha;
    public String mensaje;
    public String email;
    public String foto;
    public String meme;



    public Mensaje(String nombre, String fecha, String mensaje, String email, String foto) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.email = email;
        this.foto = foto;
    }

    public Mensaje(String nombre, String fecha, String mensaje, String email, String foto, String meme) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.email = email;
        this.foto = foto;
        this.meme = meme;
    }
}
