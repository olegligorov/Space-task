package space;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Space shuttle launcher is running");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter csv filename path: ");
        String filepath = scanner.nextLine();
        System.out.println("Enter email: ");
        String email = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();
        System.out.println("Enter recipient email: ");
        String recipientEmail = scanner.nextLine();

        SpaceShuttleLauncher launcher = new SpaceShuttleLauncher(filepath, email, password, recipientEmail);
        launcher.launch();
    }
}
