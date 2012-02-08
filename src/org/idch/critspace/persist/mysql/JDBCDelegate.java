/**
 * 
 */
package org.idch.critspace.persist.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.idch.persist.ConnectionProvider;
import org.idch.persist.DatabaseException;
import org.idch.persist.RepositoryAccessException;

/**
 * @author Neal_2
 */
public abstract class JDBCDelegate {
    private static final Logger LOGGER = Logger.getLogger(JDBCDelegate.class);
    
    protected ConnectionProvider m_provider = null;
    
    protected JDBCDelegate() {
        
    }
    
    public void execute() throws RepositoryAccessException { 
        Connection conn = null;
        try {
            conn = openTransaction();
//            WorkspaceProxy proxy = new WorkspaceProxy(conn);
//            data = proxy.create(user, name);
            invoke();
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Delegated execution failed.";
        
            LOGGER.warn(msg);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
    }
    
    protected abstract void invoke();
    
    //========================================================================
    // HELPER METHODS
    //========================================================================
    protected final Connection openTransaction() 
            throws SQLException, DatabaseException {
        Connection conn = null; 
        synchronized (m_provider) {
            conn = m_provider.getConnection();
            conn.setAutoCommit(false);
        }
        
        return conn;
    }
    
    protected final Connection openReadOnly() 
    throws SQLException, DatabaseException {
        Connection conn = null; 
        synchronized (m_provider) {
            conn = m_provider.getConnection();
            conn.setReadOnly(true);
        }
        
        return conn;
    }
    
    /**
     * Helper method that attempts to rollback a transaction, logging and 
     * supressing any exceptions.
     *  
     * @param conn the connection to rollback
     */
    protected final void rollback(Connection conn) {
        String msg = "Could not rollback transaction.";
        try {
            if (conn != null)
                conn.rollback();
        } catch (SQLException sqe) {
            LOGGER.error(msg, sqe);
            throw new RuntimeException(sqe);
        }
    }
    
    /**
     * Helper method that attempts to close a transaction, logging and 
     * supressing any exceptions.
     *  
     * @param conn the connection to close
     */
    protected final void close(Connection conn) {
        String msg = "Could not close connection.";
        try { 
            if (conn != null)
                conn.close();
        } catch (SQLException sqe) {
            LOGGER.error(msg, sqe);
            throw new RuntimeException(sqe);
        }
    }
}
