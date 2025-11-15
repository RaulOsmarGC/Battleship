//Recordar que @BeforeEach hace que se ejecute antes de cada prueba
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProtocoloBattleshipTest {

    @Test
    @DisplayName("Debe construir un mensaje de disparo correctamente")
    void construirMensajeDisparo() {
        String mensaje = ProtocoloBattleship.construirMensajeDisparo(3, 4);

        //Afirmamos que el mensaje es exactamente el esperado
        assertThat(mensaje).isEqualTo("DISPARAR|3,4");
    }

    @Test
    @DisplayName("Debe construir un mensaje de resultado con tipo de barco (HUNDIDO)")
    void construirMensajeResultadoConBarco() {
        String mensaje = ProtocoloBattleship.construirMensajeResultado(
                ProtocoloBattleship.HUNDIDO, 5, 5, "DESTRUCTOR");

        assertThat(mensaje).isEqualTo("HUNDIDO|5,5|DESTRUCTOR");
    }

    @Test
    @DisplayName("Debe construir un mensaje de resultado sin tipo de barco (FALLO)")
    void construirMensajeResultadoSinBarco() {
        String mensaje = ProtocoloBattleship.construirMensajeResultado(
                ProtocoloBattleship.FALLO, 1, 1, null);

        assertThat(mensaje).isEqualTo("FALLO|1,1");
    }

    @Test
    @DisplayName("Debe parsear un comando simple (LISTO)")
    void parsearMensajeSimple() {
        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje("LISTO");

        assertThat(msg.comando).isEqualTo("LISTO");
        assertThat(msg.x).isEqualTo(-1);
        assertThat(msg.y).isEqualTo(-1);
        assertThat(msg.tipoBarco).isNull();
    }

    @Test
    @DisplayName("Debe parsear un comando de disparo (DISPARAR)")
    void parsearMensajeDisparo() {
        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje("DISPARAR|7,8");

        assertThat(msg.comando).isEqualTo("DISPARAR");
        assertThat(msg.x).isEqualTo(7);
        assertThat(msg.y).isEqualTo(8);
        assertThat(msg.tipoBarco).isNull();
    }

    @Test
    @DisplayName("Debe parsear un resultado con barco (HUNDIDO)")
    void parsearMensajeHundido() {
        String raw = "HUNDIDO|2,2|PORTAAVIONES";
        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje(raw);

        assertThat(msg.comando).isEqualTo("HUNDIDO");
        assertThat(msg.x).isEqualTo(2);
        assertThat(msg.y).isEqualTo(2);
        assertThat(msg.tipoBarco).isEqualTo("PORTAAVIONES");
    }

    @Test
    @DisplayName("Debe parsear un resultado sin barco (IMPACTO)")
    void parsearMensajeImpacto() {
        String raw = "IMPACTO|4,4";
        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje(raw);

        assertThat(msg.comando).isEqualTo("IMPACTO");
        assertThat(msg.x).isEqualTo(4);
        assertThat(msg.y).isEqualTo(4);
        assertThat(msg.tipoBarco).isNull();
    }

    @Test
    @DisplayName("Debe lanzar excepción si el mensaje es nulo o vacío")
    void parsearMensajeInvalidoNulo() {
        //LanzaError
        assertThatThrownBy(() -> {
            ProtocoloBattleship.parsearMensaje(null);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensaje nulo o vacío");

        assertThatThrownBy(() -> {
            ProtocoloBattleship.parsearMensaje("   ");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensaje nulo o vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato del mensaje es inválido")
    void parsearMensajeInvalidoFormato() {
        //Faltan coordenadas
        assertThatThrownBy(() -> {
            ProtocoloBattleship.parsearMensaje("DISPARAR|");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mensaje con formato inválido");

        //Coordenada no numérica
        assertThatThrownBy(() -> {
            ProtocoloBattleship.parsearMensaje("DISPARAR|3,A");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mensaje con formato inválido");
    }
}