//Recordar que @BeforeEach hace que se ejecute antes de cada prueba
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JuegoBattleshipTest {

    private JuegoBattleship juego;

    @BeforeEach
    void setUp() {
        //Creamos una instancia fresca para cada prueba,
        //asegurando que las pruebas no interfieran entre sí.
        juego = new JuegoBattleship();
    }

    @Test
    @DisplayName("Constructor: No debe haber disparado a ninguna posición al inicio")
    void constructorEstadoInicialYaDisparado() {
        //Verificamos el estado inicial.
        //No se ha disparado, así que (0,0) debe ser 'false'.
        assertThat(juego.yaDisparado(0, 0)).isFalse();
    }

    @Test
    @DisplayName("Constructor: Ningún barco debe estar hundido al inicio")
    void constructorEstadoInicialBarcosHundidos() {
        assertThat(juego.estaBarcoHundido("DESTRUCTOR")).isFalse();
        assertThat(juego.todosBarcosHundidos()).isFalse();
    }

    @Test
    @DisplayName("registrarFallo debe marcar la posición como disparada")
    void registrarFalloActualizaYaDisparado() {
        //Arrange (Preparar): Estado inicial
        assertThat(juego.yaDisparado(5, 5)).isFalse();

        //Act (Actuar): Llamamos al método que queremos probar
        juego.registrarFallo(5, 5);

        //Assert (Afirmar): Verificamos el *cambio de estado*
        assertThat(juego.yaDisparado(5, 5)).isTrue();
    }

    @Test
    @DisplayName("registrarImpacto debe marcar la posición como disparada")
    void registrarImpactoActualizaYaDisparado() {
        //Arrange
        assertThat(juego.yaDisparado(3, 4)).isFalse();

        //Act
        juego.registrarImpacto(3, 4);

        //Assert
        assertThat(juego.yaDisparado(3, 4)).isTrue();
    }

    @Test
    @DisplayName("yaDisparado debe ser falso para posiciones no disparadas")
    void yaDisparadoPosicionDiferente() {
        juego.registrarImpacto(1, 1);

        assertThat(juego.yaDisparado(1, 1)).isTrue();
        assertThat(juego.yaDisparado(1, 2)).isFalse();
        assertThat(juego.yaDisparado(2, 1)).isFalse();
    }

}