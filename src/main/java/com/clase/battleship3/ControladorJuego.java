package com.clase.battleship3;

import java.io.IOException;

public class ControladorJuego {
    private final JuegoBattleship modelo;
    private final GestorRed red;
    private final VistaJuego vista;

    private String nombreJugador;
    private boolean esServidor;

    public ControladorJuego(JuegoBattleship modelo, GestorRed red, VistaJuego vista) {
        this.modelo = modelo;
        this.red = red;
        this.vista = vista;
    }

    public void iniciar() {
        vista.mostrarMensaje("BATTLESHIP");
        this.nombreJugador = vista.pedirNombreJugador();
        this.esServidor = vista.elegirModo();

        try {
            if (esServidor) {
                vista.mostrarMensaje("Esperando conexión");
                red.esperarConexion();
            } else {
                String ip = vista.pedirIPServidor();
                vista.mostrarMensaje("Conectando a " + ip);
                red.conectarAPartida(ip);
            }
            vista.mostrarMensaje("Conectado");

            intercambiarNombres();

            iniciarJuego();

        } catch (IOException e) {
            vista.mostrarError("Error de conexión: " + e.getMessage());
        } finally {
            red.cerrarConexion();
        }
    }

    private void intercambiarNombres() throws IOException {
        String nombreOponente;
        if (esServidor) {
            nombreOponente = red.leerMensaje();
            red.enviarMensaje(nombreJugador);
        } else {
            red.enviarMensaje(nombreJugador);
            nombreOponente = red.leerMensaje();
        }
        vista.mostrarMensaje("Jugando contra: " + nombreOponente);
    }

    private void iniciarJuego() throws IOException {
        vista.mostrarMensaje("\nINICIANDO JUEGO");
        modelo.colocarBarcosAutomaticamente();
        vista.mostrarMensaje("Tus barcos han sido colocados.");

        vista.actualizarTableroPropio(modelo.getTableroPropio());
        vista.actualizarEstadoBarcos(modelo.getEstadoBarcos());
        vista.actualizarTableroEnemigo(modelo.getTableroEnemigo());

        red.enviarMensaje(ProtocoloBattleship.LISTO);
        String respuesta = red.leerMensaje();

        if (respuesta != null && respuesta.equals(ProtocoloBattleship.LISTO)) {
            boolean juegoActivo = true;
            boolean miTurno = esServidor;

            while (juegoActivo) {
                if (miTurno) {
                    juegoActivo = turnoLocal();
                } else {
                    juegoActivo = turnoRemoto();
                }
                miTurno = !miTurno;
            }
        } else {
            vista.mostrarError("Fallo al sincronizar: " + respuesta);
        }
    }

    boolean turnoLocal() throws IOException {
        vista.mostrarMensaje("\nTU TURNO");

        int[] disparo;
        do {
            disparo = vista.pedirDisparo();
            if (modelo.yaDisparado(disparo[0], disparo[1])) {
                vista.mostrarMensaje("Ya disparaste ahí. Intenta de nuevo.");
            }
        } while (modelo.yaDisparado(disparo[0], disparo[1]));

        red.enviarMensaje(ProtocoloBattleship.construirMensajeDisparo(disparo[0], disparo[1]));
        String respuesta = red.leerMensaje();

        if (respuesta == null) {
            vista.mostrarError("El oponente se desconectó.");
            return false;
        }

        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje(respuesta);

        switch (msg.comando) {
            case ProtocoloBattleship.IMPACTO:
                vista.mostrarMensaje("¡Impacto en (" + msg.x + "," + msg.y + ")!");
                modelo.registrarImpacto(msg.x, msg.y);
                break;
            case ProtocoloBattleship.FALLO:
                vista.mostrarMensaje("Fallo en (" + msg.x + "," + msg.y + ")");
                modelo.registrarFallo(msg.x, msg.y);
                break;
            case ProtocoloBattleship.HUNDIDO:
                vista.mostrarMensaje("HUNDIDO " + msg.tipoBarco + " en (" + msg.x + "," + msg.y + ")");
                modelo.registrarImpacto(msg.x, msg.y);
                break;
            case ProtocoloBattleship.JUEGO_TERMINADO:
                vista.mostrarMensaje("GANASTE");
                vista.reportarFinJuego(true);
                return false;
        }
        vista.actualizarTableroEnemigo(modelo.getTableroEnemigo());
        return true;
    }

    boolean turnoRemoto() throws IOException {
        vista.mostrarMensaje("\nTURNO DEL OPONENTE");
        String mensajeEntrante = red.leerMensaje();

        if (mensajeEntrante == null) {
            vista.mostrarError("El oponente se desconectó.");
            return false;
        }

        ProtocoloBattleship.Mensaje msg = ProtocoloBattleship.parsearMensaje(mensajeEntrante);

        if (msg.comando.equals(ProtocoloBattleship.DISPARAR)) {
            String tipoBarco = modelo.obtenerTipoBarcoEn(msg.x, msg.y);

            boolean impacto = modelo.recibirDisparo(msg.x, msg.y);

            if (impacto) {
                if (tipoBarco.equals("DESCONOCIDO")) {
                    red.enviarMensaje(ProtocoloBattleship.construirMensajeResultado(
                            ProtocoloBattleship.IMPACTO, msg.x, msg.y, null));
                    vista.mostrarMensaje("El oponente impactó en (" + msg.x + "," + msg.y + ")");
                }
                else if (modelo.estaBarcoHundido(tipoBarco)) {
                    if (modelo.todosBarcosHundidos()) {
                        red.enviarMensaje(ProtocoloBattleship.JUEGO_TERMINADO);
                        vista.mostrarMensaje("HAS PERDIDO Toda tu flota ha sido hundida.");
                        vista.reportarFinJuego(false);
                        return false;
                    } else {
                        red.enviarMensaje(ProtocoloBattleship.construirMensajeResultado(
                                ProtocoloBattleship.HUNDIDO, msg.x, msg.y, tipoBarco));
                        vista.mostrarMensaje("El oponente hundió tu " + tipoBarco);
                    }
                } else {
                    red.enviarMensaje(ProtocoloBattleship.construirMensajeResultado(
                            ProtocoloBattleship.IMPACTO, msg.x, msg.y, null));
                    vista.mostrarMensaje("El oponente impactó en (" + msg.x + "," + msg.y + ")");
                }
            } else {
                red.enviarMensaje(ProtocoloBattleship.construirMensajeResultado(
                        ProtocoloBattleship.FALLO, msg.x, msg.y, null));
                vista.mostrarMensaje("El oponente falló en (" + msg.x + "," + msg.y + ")");
            }

            vista.actualizarTableroPropio(modelo.getTableroPropio());
            vista.actualizarEstadoBarcos(modelo.getEstadoBarcos());
        }
        return true;
    }
}