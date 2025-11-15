import java.util.Map;
import java.util.Scanner;

public class BattleshipConsola implements VistaJuego {

    private final Scanner scanner;

    public BattleshipConsola() {
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        JuegoBattleship modelo = new JuegoBattleship();
        GestorRed red = new GestorRed();
        VistaJuego vista = new BattleshipConsola();

        ControladorJuego controlador = new ControladorJuego(modelo, red, vista);

        controlador.iniciar();
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarError(String error) {
        System.err.println(error);
    }

    @Override
    public void actualizarTableroPropio(char[][] tablero) {
        System.out.println("\n=== TU TABLERO ===");
        mostrarTablero(tablero);
    }

    @Override
    public void actualizarTableroEnemigo(char[][] tablero) {
        System.out.println("\n=== TABLERO ENEMIGO ===");
        mostrarTablero(tablero);
    }

    @Override
    public void actualizarEstadoBarcos(Map<String, String> estadoBarcos) {
        System.out.println("\nEstado de tus barcos:");
        for (Map.Entry<String, String> entry : estadoBarcos.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    @Override
    public boolean elegirModo() {
        while (true) {
            System.out.println("\nSelecciona modo:");
            System.out.println("1. Crear partida (Servidor)");
            System.out.println("2. Unirse a partida (Cliente)");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            if ("1".equals(opcion)) return true;
            if ("2".equals(opcion)) return false;
            System.out.println("Opción inválida.");
        }
    }

    @Override
    public String pedirIPServidor() {
        System.out.print("\nIngresa la IP del servidor: ");
        return scanner.nextLine();
    }

    @Override
    public String pedirNombreJugador() {
        System.out.print("Ingresa tu nombre: ");
        return scanner.nextLine();
    }

    @Override
    public int[] pedirDisparo() {
        while (true) {
            try {
                System.out.print("Ingresa coordenadas para disparar (fila,columna 0-9): ");
                String entrada = scanner.nextLine();
                String[] coordenadas = entrada.split(",");

                if (coordenadas.length != 2) {
                    System.out.println("Formato inválido. Usa: fila,columna");
                    continue;
                }

                int fila = Integer.parseInt(coordenadas[0].trim());
                int columna = Integer.parseInt(coordenadas[1].trim());

                if (fila >= 0 && fila < 10 && columna >= 0 && columna < 10) {
                    return new int[] { fila, columna };
                } else {
                    System.out.println("Coordenadas fuera de rango (0-9).");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingresa números válidos.");
            }
        }
    }

    @Override
    public void reportarFinJuego(boolean ganaste) {
        System.out.println(ganaste ? "¡FIN DEL JUEGO: GANASTE!" : "¡FIN DEL JUEGO: PERDISTE!");
    }

    private void mostrarTablero(char[][] tablero) {
        System.out.print("  ");
        for (int i = 0; i < 10; i++) System.out.print(i + " ");
        System.out.println();

        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 10; j++) {
                System.out.print(tablero[i][j] + " ");
            }
            System.out.println();
        }
    }
}