package de.caluga.morphium.query;

import de.caluga.morphium.Morphium;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * User: Stephan Bösebeck
 * Date: 31.08.12
 * Time: 11:08
 * <p/>
 * crate query for a certain type
 */
public interface QueryFactory {
    public <T> Query<T> createQuery(Morphium m, Class<? extends T> type);

    public Class<? extends Query> getQueryImpl();

    public void setQueryImpl(Class<? extends Query> queryImpl);

    public void setExecutor(ThreadPoolExecutor ex);

    public ThreadPoolExecutor getExecutor(Morphium m);
}
