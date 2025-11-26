package com.clase.battleship3;

import java.util.*;

public class JuegoBattleship {
    private static final int TAMANIO_TABLERO = 10;
    private char[][] tableroPropio;
    private char[][] tableroEnemigo;
    private Map<String, Integer> barcos;
    private Map<String, Integer> impactosPorBarco;
    private Set<String> posicionesDisparadas;

    public JuegoBattleship() {
        tableroPropio = new char[TAMANIO_TABLERO][TAMANIO_TABLERO];
        tableroEnemigo = new char[TAMANIO_TABLERO][TAMANIO_TABLERO];
        inicializarTableros();

        barcos = new HashMap<>();
        //barcos.put("PORTAAVIONES", 5);
        //barcos.put("ACORAZADO", 4);
        //barcos.put("CRUCERO", 3);
        //barcos.put("SUBMARINO", 3);
        barcos.put("DESTRUCTOR", 2);

        impactosPorBarco = new HashMap<>();
        for (String barco : barcos.keySet()) {
            impactosPorBarco.put(barco, 0);
        }

        posicionesDisparadas = new HashSet<>();
    }

    private void inicializarTableros() {
        for (int i = 0; i < TAMANIO_TABLERO; i++) {
            for (int j = 0; j < TAMANIO_TABLERO; j++) {
                tableroPropio[i][j] = '~';
                tableroEnemigo[i][j] = '?';
            }
        }
    }

    public void colocarBarcosAutomaticamente() {
        Random random = new Random();

        for (Map.Entry<String, Integer> entrada : barcos.entrySet()) {
            String nombreBarco = entrada.getKey();
            int tamanio = entrada.getValue();
            boolean colocado = false;

            while (!colocado) {
                boolean horizontal = random.nextBoolean();
                int fila = random.nextInt(TAMANIO_TABLERO);
                int columna = random.nextInt(TAMANIO_TABLERO);

                if (puedeColocarBarco(fila, columna, tamanio, horizontal)) {
                    colocarBarco(fila, columna, tamanio, horizontal, nombreBarco.charAt(0));
                    colocado = true;
                }
            }
        }
    }

    private boolean puedeColocarBarco(int fila, int columna, int tamanio, boolean horizontal) {
        if (horizontal) {
            if (columna + tamanio > TAMANIO_TABLERO) return false;
            for (int i = columna; i < columna + tamanio; i++) {
                if (tableroPropio[fila][i] != '~') return false;
            }
        } else {
            if (fila + tamanio > TAMANIO_TABLERO) return false;
            for (int i = fila; i < fila + tamanio; i++) {
                if (tableroPropio[i][columna] != '~') return false;
            }
        }
        return true;
    }

    private void colocarBarco(int fila, int columna, int tamanio, boolean horizontal, char simbolo) {
        if (horizontal) {
            for (int i = columna; i < columna + tamanio; i++) {
                tableroPropio[fila][i] = simbolo;
            }
        } else {
            for (int i = fila; i < fila + tamanio; i++) {
                tableroPropio[i][columna] = simbolo;
            }
        }
    }

    public boolean recibirDisparo(int fila, int columna) {
        if (tableroPropio[fila][columna] == 'X' || tableroPropio[fila][columna] == 'O') {
            return false;
        }

        if (tableroPropio[fila][columna] != '~') {
            char caracterBarco = tableroPropio[fila][columna];
            String tipoBarco = obtenerTipoBarcoDesdeCaracter(caracterBarco);

            if (impactosPorBarco.containsKey(tipoBarco)) {
                impactosPorBarco.put(tipoBarco, impactosPorBarco.get(tipoBarco) + 1);
            } else {
                System.out.println("Advertencia: Barco no registrado '" + tipoBarco + "' encontrado. Registrando...");
                impactosPorBarco.put(tipoBarco, 1);
            }

            tableroPropio[fila][columna] = 'X';
            return true;
        } else {
            tableroPropio[fila][columna] = 'O';
            return false;
        }
    }

    public void registrarImpacto(int fila, int columna) {
        tableroEnemigo[fila][columna] = 'X';
        posicionesDisparadas.add(fila + "," + columna);
    }

    public void registrarFallo(int fila, int columna) {
        tableroEnemigo[fila][columna] = 'O';
        posicionesDisparadas.add(fila + "," + columna);
    }

    public boolean yaDisparado(int fila, int columna) {
        return posicionesDisparadas.contains(fila + "," + columna);
    }

    public String obtenerTipoBarcoEn(int fila, int columna) {
        char c = tableroPropio[fila][columna];
        if (c == 'X') {
            return "DESCONOCIDO";
        }
        return obtenerTipoBarcoDesdeCaracter(c);
    }

    private String obtenerTipoBarcoDesdeCaracter(char c) {
        switch (c) {
            case 'P': return "PORTAAVIONES";
            case 'A': return "ACORAZADO";
            case 'C': return "CRUCERO";
            case 'S': return "SUBMARINO";
            case 'D': return "DESTRUCTOR";
            default: return "DESCONOCIDO";
        }
    }

    public boolean estaBarcoHundido(String tipoBarco) {
        if (!impactosPorBarco.containsKey(tipoBarco) || !barcos.containsKey(tipoBarco)) {
            return false;
        }

        int impactos = impactosPorBarco.get(tipoBarco);
        int tamanio = barcos.get(tipoBarco);
        return impactos >= tamanio;
    }

    public boolean todosBarcosHundidos() {
        for (String barco : barcos.keySet()) {
            if (!estaBarcoHundido(barco)) {
                return false;
            }
        }
        return true;
    }

    public char[][] getTableroPropio() {
        return this.tableroPropio;
    }

    public char[][] getTableroEnemigo() {
        return this.tableroEnemigo;
    }

    public Map<String, String> getEstadoBarcos() {
        Map<String, String> estado = new HashMap<>();
        for (String barco : barcos.keySet()) {
            int impactos = impactosPorBarco.getOrDefault(barco, 0);
            int tamanio = barcos.get(barco);
            String estadoBarco = (impactos >= tamanio) ? "HUNDIDO" : impactos + "/" + tamanio;
            estado.put(barco, estadoBarco);
        }
        return estado;
    }
}