package com.roadmap.app.repository;

import com.roadmap.app.model.SubTask;
import com.roadmap.app.model.Task;
import com.roadmap.app.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public Task save(Task task) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Task saved = em.merge(task);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Task task) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Task managed = em.contains(task) ? task : em.merge(task);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Task> findByIdWithSubTasks(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Task> result = em.createQuery(
                "SELECT t FROM Task t LEFT JOIN FETCH t.subTasks WHERE t.id = :id",
                Task.class)
                .setParameter("id", id)
                .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public List<Task> findByWeekIdWithSubTasks(Long weekId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.subTasks " +
                "WHERE t.week.id = :weekId ORDER BY t.orderIndex",
                Task.class)
                .setParameter("weekId", weekId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public SubTask saveSubTask(SubTask subTask) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SubTask saved = em.merge(subTask);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteSubTask(SubTask subTask) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SubTask managed = em.contains(subTask) ? subTask : em.merge(subTask);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long countCompleted() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(t) FROM Task t WHERE t.completed = true", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countTotal() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(t) FROM Task t", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }
}
