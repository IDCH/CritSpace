/* Created on       Aug 25, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. PERMISSION TO USE THIS SOFTWARE MAY BE GRANTED 
 * TO INDIVIDUALS OR ORGANIZATIONS ON A CASE BY CASE BASIS. FOR MORE 
 * INFORMATION PLEASE CONTACT THE DIRECTOR OF THE CSDL. IN THE EVENT 
 * THAT SUCH PERMISSION IS GIVEN IT SHOULD BE UNDERSTOOD THAT THIS 
 * SOFTWARE IS PROVIDED ON AN AS IS BASIS. THIS CODE HAS BEEN DEVELOPED 
 * FOR USE WITHIN A PARTICULAR RESEARCH PROJECT AND NO CLAIM IS MADE AS 
 * TO IS CORRECTNESS, PERFORMANCE, OR SUITABILITY FOR ANY USE.
 */
package org.idch.critspace.persist.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.idch.util.LogService;

public abstract class PersistenceProxy {
    protected final static String LOGGER = PersistenceProxy.class.getName();
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    protected final Connection m_connection;
    
    //========================================================================
    // CONSTRUCTORS & FINALIZATION
    //========================================================================
    
    /**
     * 
     * @param connection
     */
    protected PersistenceProxy(Connection connection) {
        m_connection = connection;
    }
    
    protected final void finalize() throws Throwable {
        try {
            this.close();
        } catch (Exception e) {
            /** log and supress errors */
            LogService.logError("Finalization failed", LOGGER, e);
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Closes any open statements in use by this proxy.
     * 
     * @throws SQLException if there is a database access error
     */
    public abstract void close() throws SQLException;
    
}
