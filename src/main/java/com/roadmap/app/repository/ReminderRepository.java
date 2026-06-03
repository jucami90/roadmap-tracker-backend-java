package com.roadmap.app.repository;

import com.roadmap.app.model.Reminder;
import com.roadmap.app.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class ReminderRepository {

    public Reminder save(Reminder reminder) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Reminder saved = em.merge(reminder);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Reminder reminder) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Reminder managed = em.contains(reminder) ? reminder : em.merge(reminder);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Reminder> findDueReminders() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Reminder r WHERE r.status = 'PENDING' " +
                "AND r.reminderDateTime <= :now",
                Reminder.class)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reminder> findByTaskId(Long taskId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Reminder r WHERE r.task.id = :taskId ORDER BY r.reminderDateTime",
                Reminder.class)
                .setParameter("taskId", taskId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
