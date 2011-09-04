/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.util.ArrayList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author Shirkit
 */
public class DaoEntity {
    
    private Session session;

    public DaoEntity() {
        setSession(hibernate.HibernateUtil.openSession());
    }

    public Session getSession() {
        if (session.isOpen()) {
            return session;
        } else {
            setSession(hibernate.HibernateUtil.getSessionFactory().openSession());
            return session;
        }
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void closeSession() {
        if (session.isOpen()) {
            this.session.flush();
            this.session.close();
        }
    }

    public boolean save(MyEntity entity) {
        try {
            Transaction tx = getSession().beginTransaction();
            getSession().save(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean update(MyEntity entity) {
        try {
            Transaction tx = getSession().beginTransaction();
            getSession().update(entity);
            getSession().flush();
            tx.commit();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean remove(MyEntity entity) throws HibernateException, ConstraintViolationException {

        try {
            Transaction tx = getSession().beginTransaction();
            getSession().delete(entity);
            getSession().flush();
            tx.commit();
            return true;
        } catch (ConstraintViolationException e) {
            return false;
        } catch (HibernateException e) {
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    public MyEntity search(Class clazz, int id) {
        return (MyEntity) getSession().load(clazz, id);
    }

    public ArrayList<MyEntity> list(Class clazz) {
        return (ArrayList<MyEntity>) getSession().createCriteria(clazz).list();
    }
}
