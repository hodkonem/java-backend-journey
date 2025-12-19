package ru.itwizardry.userservice.app;

import ru.itwizardry.userservice.util.HibernateUtil;

public class HibernateTestApp {
    public static void main(String[] args) {
        var sf = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created: " + sf);

        try (var session = sf.openSession()) {
            System.out.println("Session opened: " + session.isOpen());
        }

        HibernateUtil.shutdown();
    }
}
