package com.roadmap.app.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.File;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT = "roadmapPU";
    private static EntityManagerFactory emf;

    static {
        // Ensure DB directory exists before Hibernate connects
        File dbDir = new File(System.getProperty("user.home"), ".roadmap-tracker/data");
        dbDir.mkdirs();
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            migrateNullBooleans();
        }
        return emf;
    }

    /**
     * Fix NULL boolean columns in existing DB rows created before the
     * addToCalendar field was added. Runs once at startup, safe to re-run.
     */
    private static void migrateNullBooleans() {
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Use native SQL to safely update without Hibernate mapping issues
            em.createNativeQuery(
                "UPDATE reminders SET add_to_calendar = FALSE WHERE add_to_calendar IS NULL")
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            // Column may not exist yet on first run — Hibernate will create it
            try { em.getTransaction().rollback(); } catch (Exception ignored) {}
        } finally {
            em.close();
        }
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
