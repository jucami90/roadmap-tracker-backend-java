package com.roadmap.app.repository;

import com.roadmap.app.model.Week;
import com.roadmap.app.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class WeekRepository {

    public List<Week> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Week> q = em.createQuery(
                "SELECT DISTINCT w FROM Week w LEFT JOIN FETCH w.tasks ORDER BY w.weekNumber",
                Week.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Week> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Week w = em.find(Week.class, id);
            return Optional.ofNullable(w);
        } finally {
            em.close();
        }
    }

    public Week save(Week week) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Week saved = (week.getId() == null) ? em.merge(week) : em.merge(week);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(Week week) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(week);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(w) FROM Week w", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }
}
