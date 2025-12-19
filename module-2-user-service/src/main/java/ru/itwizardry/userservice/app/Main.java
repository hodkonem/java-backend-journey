package ru.itwizardry.userservice.app;

import ru.itwizardry.userservice.dao.UserDaoImpl;
import ru.itwizardry.userservice.dao.proxy.UserDaoLoggingProxy;
import ru.itwizardry.userservice.service.UserService;
import ru.itwizardry.userservice.service.UserServiceImpl;
import ru.itwizardry.userservice.util.HibernateUtil;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserService userService = new UserServiceImpl(
                new UserDaoLoggingProxy(new UserDaoImpl())
        );

        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                printMenu();
                String cmd = in.nextLine().trim();

                try {
                    switch (cmd) {
                        case "1" -> create(in, userService);
                        case "2" -> getById(in, userService);
                        case "3" -> getByEmail(in, userService);
                        case "4" -> update(in, userService);
                        case "5" -> delete(in, userService);
                        case "0" -> {
                            System.out.println("Bye!");
                            return;
                        }
                        default -> System.out.println("Unknown command: " + cmd);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void printMenu() {
        System.out.println("""
                === User Service ===
                1) Create User
                2) Get User by id
                3) Get User by email
                4) Update User
                5) Delete User
                0) Exit
                """);
        System.out.print("Select: ");
    }

    private static void create(Scanner in, UserService service) {
        System.out.print("Name: ");
        String name = in.nextLine();

        System.out.print("Email: ");
        String email = in.nextLine();

        System.out.print("Age (empty = null): ");
        String ageStr = in.nextLine().trim();
        Integer age = ageStr.isBlank() ? null : Integer.valueOf(ageStr);

        var user = service.create(name, email, age);
        System.out.println("Created: " + user);
    }

    private static void getById(Scanner in, UserService service) {
        System.out.print("Id: ");
        Long id = Long.valueOf(in.nextLine().trim());

        var user = service.getById(id);
        System.out.println(user == null ? "User not found." : user);
    }

    private static void getByEmail(Scanner in, UserService service) {
        System.out.print("Email: ");
        String email = in.nextLine();

        var user = service.getByEmail(email);
        System.out.println(user == null ? "User not found." : user);
    }

    private static void update(Scanner in, UserService service) {
        System.out.print("Id: ");
        Long id = Long.valueOf(in.nextLine().trim());

        System.out.print("Name: ");
        String name = in.nextLine();

        System.out.print("Email: ");
        String email = in.nextLine();

        System.out.print("Age (empty = null): ");
        String ageStr = in.nextLine().trim();
        Integer age = ageStr.isBlank() ? null : Integer.valueOf(ageStr);

        var user = service.update(id, name, email, age);
        System.out.println(user == null ? "User not found." : ("Updated: " + user));
    }

    private static void delete(Scanner in, UserService service) {
        System.out.print("Id: ");
        Long id = Long.valueOf(in.nextLine().trim());

        service.delete(id);
        System.out.println("Deleted (or not found).");
    }
}
