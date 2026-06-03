package com.roadmap.app.repository;

import com.roadmap.app.model.Note;
import com.roadmap.app.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class NoteRepository {

    public Note save(Note note) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Note saved = em.merge(note);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Note note) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Note managed = em.contains(note) ? note : em.merge(note);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Note> findByWeekId(Long weekId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT n FROM Note n WHERE n.week.id = :weekId AND n.task IS NULL ORDER BY n.updatedAt DESC",
                Note.class)
                .setParameter("weekId", weekId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Note> findByTaskId(Long taskId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT n FROM Note n WHERE n.task.id = :taskId ORDER BY n.updatedAt DESC",
                Note.class)
                .setParameter("taskId", taskId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public long countByTaskId(Long taskId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT COUNT(n) FROM Note n WHERE n.task.id = :taskId", Long.class)
                .setParameter("taskId", taskId)
                .getSingleResult();
        } finally {
            em.close();
        }
    }
}
