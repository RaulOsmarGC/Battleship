package com.clase.battleship3;

import java.io.*;
import java.net.*;

public class GestorRed {
    private static final int PUERTO = 12345;
    private Socket socket;
    private ServerSocket serverSocket;
    private PrintWriter salida;
    private BufferedReader entrada;

    public void esperarConexion() throws IOException {
        serverSocket = new ServerSocket(PUERTO);
        socket = serverSocket.accept();
        configurarFlujos();
    }
    public void conectarAPartida(String ip) throws IOException {
        socket = new Socket(ip, PUERTO);
        configurarFlujos();
    }

    private void configurarFlujos() throws IOException {
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }
    public String leerMensaje() throws IOException {
        if (entrada != null) {
            return entrada.readLine();
        }
        return null;
    }

    public void cerrarConexion() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
        }
    }
}