package com.ocn.Literatura.principal;

import com.ocn.Literatura.model.Datos;
import com.ocn.Literatura.model.DatosAutor;
import com.ocn.Literatura.model.DatosLibros;
import com.ocn.Literatura.service.ConsumoAPI;
import com.ocn.Literatura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);

    public void muestraElMenu() {
        int opcion = 1;
        do {
            System.out.println("Seleccione una opción del menú:");
            System.out.println("1- Buscar libro por título \n" +
                    "2- Listar libros registrados \n" +
                    "3- Listar autores registrados \n" +
                    "4- Listar autores vivos en un determinado año \n" +
                    "5- Listar libros por idioma \n" +
                    "6- Top 10 libros populares \n" +
                    "0- Salir \n");
            try {
                opcion = Integer.parseInt(teclado.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida. Intente de nuevo.");
                continue;
            }

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosEnAno();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    top10LibrosPopulares();
                    break;
                case 0:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        } while (opcion != 0);
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        var tituloLibro = teclado.nextLine();
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
            var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
            Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                    .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                    .findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println("Libro Encontrado: " + libroBuscado.get());
            } else {
                System.out.println("Libro no encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
        }
    }

    private void listarLibrosRegistrados() {
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE);
            var datos = conversor.obtenerDatos(json, Datos.class);
            System.out.println("Libros registrados:");
            datos.resultados().forEach(libro -> System.out.println(libro.titulo()));
        } catch (Exception e) {
            System.out.println("Error al listar libros: " + e.getMessage());
        }
    }

    private void listarAutoresRegistrados() {
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE);
            var datos = conversor.obtenerDatos(json, Datos.class);
            System.out.println("Autores registrados:");

            datos.resultados().stream()
                    .flatMap(libro -> libro.autores().orElse(List.of()).stream())  // Desempaqueta el Optional y obtiene la lista
                    .distinct()
                    .forEach(autor -> System.out.println(autor.nombre()));

        } catch (Exception e) {
            System.out.println("Error al listar autores: " + e.getMessage());
        }
    }


    private void listarAutoresVivosEnAno() {
        System.out.println("Ingrese el año para listar autores vivos en ese periodo:");
        try {
            int ano = Integer.parseInt(teclado.nextLine());
            var json = consumoAPI.obtenerDatos(URL_BASE);
            var datos = conversor.obtenerDatos(json, Datos.class);
            System.out.println("Autores vivos en el año " + ano + ":");

            datos.resultados().stream()
                    .flatMap(libro -> libro.autores().orElse(List.of()).stream())  // Desempaqueta el Optional y obtiene la lista
                    .filter(autor -> {
                        try {
                            int anoNacimiento = Integer.parseInt(autor.fechaDeNacimiento());
                            return anoNacimiento <= ano;
                        } catch (NumberFormatException e) {
                            return false;  // Si fechaDeNacimiento no es un número, se ignora el autor
                        }
                    })
                    .distinct()
                    .forEach(autor -> System.out.println(autor.nombre()));

        } catch (NumberFormatException e) {
            System.out.println("Año no válido. Intente nuevamente.");
        } catch (Exception e) {
            System.out.println("Error al listar autores vivos: " + e.getMessage());
        }
    }



    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el código del idioma (ej. 'en' para inglés):");
        String idioma = teclado.nextLine();
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE + "?languages=" + idioma);
            var datos = conversor.obtenerDatos(json, Datos.class);
            System.out.println("Libros en idioma " + idioma + ":");
            datos.resultados().forEach(libro -> System.out.println(libro.titulo()));
        } catch (Exception e) {
            System.out.println("Error al listar libros por idioma: " + e.getMessage());
        }
    }

    private void top10LibrosPopulares() {
        try {
            var json = consumoAPI.obtenerDatos(URL_BASE);
            var datos = conversor.obtenerDatos(json, Datos.class);
            System.out.println("Top 10 libros más descargados:");
            datos.resultados().stream()
                    .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                    .limit(10)
                    .map(l -> l.titulo().toUpperCase())
                    .forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Error al listar los libros más populares: " + e.getMessage());
        }
    }
}
