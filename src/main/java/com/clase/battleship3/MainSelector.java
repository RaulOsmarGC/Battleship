package com.clase.battleship3;

import javafx.application.Application;
import java.util.Scanner;

public class MainSelector {
    public static void main(String[] args) {
        System.out.println("BATTLESHIP");
        System.out.println("Seleccione el modo de interfaz:");
        System.out.println("1. Consola (Texto)");
        System.out.println("2. Gráfica (GUI)");
        System.out.print("Opción: ");

        Scanner scanner = new Scanner(System.in);
        String opcion = scanner.nextLine();

        if (opcion.equals("1")) {
            System.out.println("Modo consola.");
            BattleshipConsola.main(args);
        } else if (opcion.equals("2")) {
            System.out.println("Modo gráfico.");
            Application.launch(BattleshipGUI.class, args);
        } else {
            System.out.println("Opción no válida. Saliendo.");
        }
    }
}