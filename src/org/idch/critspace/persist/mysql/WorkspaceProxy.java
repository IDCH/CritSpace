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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.idch.auth.User;
import org.idch.critspace.Workspace;
import org.idch.util.LogService;

public class WorkspaceProxy {
    private final static String LOGGER = WorkspaceProxy.class.getName();
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    private final Connection m_connection;
    
    //========================================================================
    // CONSTRUCTORS & FINALIZATION
    //========================================================================
    
    /**
     * 
     * @param connection
     */
    WorkspaceProxy(Connection connection) {
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
    final void close() throws SQLException {
    }
    
    //========================================================================
    // CREATION
    //========================================================================
    
    private final static String ID_COL      = "ws_id";
    private final static String OWNER_COL   = "owner_id";
    private final static String NAME_COL    = "name";
    private final static String VISIBLE_COL = "visibility";
    
    
    private final static int CREATE_OWNER      = 1;
    private final static int CREATE_NAME       = 2;
    private final static int CREATE_VISIBILITY = 3;
    private final static String CREATE_SQL =
        "INSERT INTO CRIT_Workspaces (owner_id, name, visibility) " +
        "VALUES (?, ?, ?)";
    
    private final static int FIND_OWNER = 1;
    private final static int FIND_NAME  = 2;
    private final static String FIND_SQL =
        "SELECT ws_id, visibility" +
        "  FROM CRIT_Workspaces" +
        " WHERE owner_id = ? AND name = ?";
    
    private final static int LIST_OWNER = 1;
    private final static String LIST_SQL = 
        "SELECT ws_id, owner_id, name, visibility" +
        "  FROM CRIT_Workspaces" +
        " WHERE owner_id = ? OR visibility = 'public'";
    
    /**
     * 
     * @param user
     * @param name
     * @return
     * @throws SQLException
     */
    Map<String, Object> create(User user, String name) throws SQLException {
        Map<String, Object> data = null;
        PreparedStatement stmt = m_connection.prepareStatement(CREATE_SQL, 
                PreparedStatement.RETURN_GENERATED_KEYS); 
        
        long ownerId = (user != null) ? user.getId() : 0;
        String visibility = (user != null) ? "'private" : "public";
        
        stmt.setLong(CREATE_OWNER, ownerId);
        stmt.setString(CREATE_VISIBILITY, visibility);
        stmt.setString(CREATE_NAME, name);
        
        int numRowsChanged = stmt.executeUpdate();
        ResultSet results = stmt.getGeneratedKeys();
        if (numRowsChanged == 1 && results.next()) {
            long id = results.getLong(1);
            
            data = new HashMap<String, Object>(4);
            data.put(Workspace.PK_WS_ID, id);
            data.put(Workspace.PK_OWNER, ownerId);
            data.put(Workspace.PK_NAME, name);
            data.put(Workspace.PK_VISIBLE, visibility);
        }
        
        return data;
    }
    
    Map<String, Object> find(long ownerId, String name) throws SQLException {
        Map<String, Object> data = null;
        PreparedStatement stmt = m_connection.prepareStatement(FIND_SQL); 
        
        stmt.setLong(FIND_OWNER, ownerId);
        stmt.setString(FIND_NAME, name);
        
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            data = new HashMap<String, Object>(4);
            data.put(Workspace.PK_WS_ID, results.getLong(ID_COL));
            data.put(Workspace.PK_OWNER, ownerId);
            data.put(Workspace.PK_NAME, name);
            data.put(Workspace.PK_VISIBLE, results.getString(VISIBLE_COL));
        }
        
        return data;
    }
    
    List<Map<String, Object>> list(long ownerId) throws SQLException {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        PreparedStatement stmt = m_connection.prepareStatement(LIST_SQL); 
        
        stmt.setLong(LIST_OWNER, ownerId);
        
        Map<String, Object> ws = null;
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            ws = new HashMap<String, Object>();
            ws.put(Workspace.PK_WS_ID,   results.getLong(ID_COL));
            ws.put(Workspace.PK_OWNER,   results.getLong(OWNER_COL));
            ws.put(Workspace.PK_NAME,    results.getString(NAME_COL));
            ws.put(Workspace.PK_VISIBLE, results.getString(VISIBLE_COL));
            
            data.add(ws);
        }
        
        return data;
    }
}
