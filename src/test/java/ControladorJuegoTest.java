import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// 1. Habilitar Mockito con JUnit 5
@ExtendWith(MockitoExtension.class)
class ControladorJuegoTest {

    // 2. Crear Mocks (simuladores) para CADA dependencia
    @Mock
    private JuegoBattleship mockModelo;

    @Mock
    private GestorRed mockRed;

    @Mock
    private VistaJuego mockVista;

    // 3. Inyectar automáticamente los mocks en el controlador
    @InjectMocks
    private ControladorJuego controlador;

    @Test
    @DisplayName("Turno Local: Jugador dispara y falla")
    void turnoLocal_DisparaYFalla() throws IOException {
        // --- ARRANGE (Preparar) ---
        // 1. Simular la entrada de la VISTA
        when(mockVista.pedirDisparo()).thenReturn(new int[]{3, 3});
        // 2. Simular el estado del MODELO (no ha disparado ahí)
        when(mockModelo.yaDisparado(3, 3)).thenReturn(false);
        // 3. Simular la respuesta de la RED
        when(mockRed.leerMensaje()).thenReturn("FALLO|3,3");

        // --- ACT (Actuar) ---
        boolean juegoContinua = controlador.turnoLocal();

        // --- ASSERT (Afirmar) ---
        // 1. Afirmar que el juego debe continuar
        assertThat(juegoContinua).isTrue();

        // 2. VERIFICAR que el controlador hizo las llamadas correctas
        // - Verificamos que se ENVIÓ el disparo por la red
        verify(mockRed).enviarMensaje("DISPARAR|3,3");
        // - Verificamos que se REGISTRÓ el fallo en el modelo
        verify(mockModelo).registrarFallo(3, 3);
        // - Verificamos que se ACTUALIZÓ el tablero enemigo en la vista
        verify(mockVista).actualizarTableroEnemigo(any());
        // - Verificamos que NUNCA se registró un impacto
        verify(mockModelo, never()).registrarImpacto(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Turno Local: Jugador gana el juego")
    void turnoLocal_GanaJuego() throws IOException {
        // --- ARRANGE ---
        when(mockVista.pedirDisparo()).thenReturn(new int[]{1, 1});
        when(mockModelo.yaDisparado(1, 1)).thenReturn(false);
        // Simular que la respuesta del oponente es que perdimos (¡ganamos!)
        when(mockRed.leerMensaje()).thenReturn("JUEGO_TERMINADO");

        // --- ACT ---
        boolean juegoContinua = controlador.turnoLocal();

        // --- ASSERT ---
        // El juego NO debe continuar
        assertThat(juegoContinua).isFalse();

        // Verificamos que se envió el disparo
        verify(mockRed).enviarMensaje("DISPARAR|1,1");
        // Verificamos que se mostró el mensaje de victoria
        verify(mockVista).mostrarMensaje("¡GANASTE!");
        // Verificamos que se reportó el fin del juego
        verify(mockVista).reportarFinJuego(true);
    }

    @Test
    @DisplayName("Turno Remoto: Oponente dispara y hunde el último barco (pierdes)")
    void turnoRemoto_PierdeJuego() throws IOException {
        // --- ARRANGE ---
        // 1. Simular que la RED nos envía un disparo
        when(mockRed.leerMensaje()).thenReturn("DISPARAR|5,5");
        // 2. Simular que el MODELO reporta un impacto
        when(mockModelo.recibirDisparo(5, 5)).thenReturn(true);
        // 3. Simular que el MODELO identifica el barco
        when(mockModelo.obtenerTipoBarcoEn(5, 5)).thenReturn("DESTRUCTOR");
        // 4. Simular que el MODELO reporta que ESE barco está hundido
        when(mockModelo.estaBarcoHundido("DESTRUCTOR")).thenReturn(true);
        // 5. Simular que el MODELO reporta que TODOS los barcos están hundidos
        when(mockModelo.todosBarcosHundidos()).thenReturn(true);

        // --- ACT ---
        boolean juegoContinua = controlador.turnoRemoto();

        // --- ASSERT ---
        // El juego NO debe continuar
        assertThat(juegoContinua).isFalse();

        // Verificamos que informamos a la RED que el juego terminó
        verify(mockRed).enviarMensaje("JUEGO_TERMINADO");
        // Verificamos que informamos a la VISTA que perdimos
        verify(mockVista).mostrarMensaje("¡HAS PERDIDO!");
        verify(mockVista).reportarFinJuego(false);

        // Verificamos que NO se actualizó el tablero (el juego terminó antes)
        verify(mockVista, never()).actualizarTableroPropio(any());
    }
}