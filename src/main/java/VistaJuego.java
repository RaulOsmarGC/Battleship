import java.util.Map;
public interface VistaJuego {

    void mostrarMensaje(String mensaje);
    void mostrarError(String error);

    void actualizarTableroPropio(char[][] tablero);
    void actualizarTableroEnemigo(char[][] tablero);
    void actualizarEstadoBarcos(Map<String, String> estadoBarcos);

    boolean elegirModo();

    String pedirIPServidor();
    String pedirNombreJugador();
    int[] pedirDisparo();

    void reportarFinJuego(boolean ganaste);
}