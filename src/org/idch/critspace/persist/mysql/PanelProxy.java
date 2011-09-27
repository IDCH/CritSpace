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

import org.idch.critspace.Panel;
import org.idch.persist.DBUtils;

public class PanelProxy extends PersistenceProxy {
    //========================================================================
    // SQL STATEMENTS
    //========================================================================
    
    public static final String ID_COL   = "panel_id"; 
    public static final String WS_COL   = "ws_id"; 
    public static final String TYPE_COL = "panel_type";
    public static final String GRP_COL  = "vprops_grp";
    
    public static final String PROP_NAME_COL  = "prop_name";
    public static final String PROP_VALUE_COL = "prop_value";
    
    private final static int CREATE_WS   = 1;
    private final static int CREATE_TYPE = 2;
    private final static int CREATE_GRP  = 3;
    private final static String CREATE_SQL =
        "INSERT INTO CRIT_Panels (ws_id, panel_type, vprops_grp) " +
        "VALUES (?, ?, ?)";
    
    private final static int FIND_WS = 1;
    private final static String FIND_SQL =
        "SELECT panel_id, panel_type, vprops_grp" +
        "  FROM CRIT_Panels" +
        " WHERE ws_id = ?";
    
    private final static int FIND_ID = 1;
    private final static String FIND_PANEL_SQL =
        "SELECT ws_id, panel_type, vprops_grp" +
        "  FROM CRIT_Panels" +
        " WHERE panel_id = ?";
    
    private final static int DELETE_ID = 1;
    private final static String DELETE_SQL =
        "DELETE FROM CRIT_Panels WHERE panel_id = ?";
    
    private final static int FIND_PROPS_ID = 1;
    private final static String FIND_PROPS_SQL = 
        "SELECT prop_name, prop_value" +
        "  FROM CRIT_PanelProps" +
        " WHERE panel_id = ?";
    
    private final static int GET_PROP_ID = 1;
    private final static int GET_PROP_NAME = 2;
    private final static String GET_PROP_SQL = 
        "SELECT prop_value" +
        "  FROM CRIT_PanelProps" +
        " WHERE panel_id = ? AND prop_name = ?";
    
    private final static int SET_ID    = 1;
    private final static int SET_NAME  = 2;
    private final static int SET_VALUE = 3;
    private final static String SET_PROP_SQL = 
        "INSERT INTO CRIT_PanelProps (panel_id, prop_name, prop_value) " +
        "VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE prop_value = VALUES(prop_value)";
    
    private final static int DEL_ID    = 1;
    private final static int DEL_NAME  = 2;
    private final static String DEL_PROP_SQL = 
        "DELETE FROM CRIT_PanelProps " + 
        " WHERE panel_id = ? AND prop_name = ?";
    
    //========================================================================
    // CONSTRUCTORS & FINALIZATION
    //========================================================================
    
    public PreparedStatement getPropsStmt = null; 
    
    /**
     * 
     * @param connection
     */
    PanelProxy(Connection connection) {
        super(connection);
    }
    
    /**
     * Closes any open statements in use by this proxy.
     * 
     * @throws SQLException if there is a database access error
     */
    public final void close() throws SQLException {
        DBUtils.close(getPropsStmt);
    }
    
    //========================================================================
    // CREATION
    //========================================================================
    
    /**
     * 
     * @param wsId
     * @param groupId
     * @param type
     * @return
     * @throws SQLException
     */
    final Map<String, Object> createPanel(long wsId, long groupId, String type) 
    throws SQLException {
        Map<String, Object> data = null;
        
        PreparedStatement stmt = m_connection.prepareStatement(CREATE_SQL, 
                PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setLong(CREATE_WS, wsId);
        stmt.setLong(CREATE_GRP, groupId);
        stmt.setString(CREATE_TYPE, type);
        
        int numRowsChanged = stmt.executeUpdate();
        ResultSet results = stmt.getGeneratedKeys();
        if (numRowsChanged == 1 && results.next()) {
            data = new HashMap<String, Object>(4);
            long id = results.getLong(1);
            
            data.put(Panel.PK_ID, id);
            data.put(Panel.PK_WS_ID, wsId);
            data.put(Panel.PK_GROUP, groupId);
            data.put(Panel.PK_TYPE, type);
        }
        
        return data;
    }

    /**
     * 
     * @param panelId
     * @return
     * @throws SQLException
     */
    final Map<String, Object> findPanel(long panelId) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(FIND_PANEL_SQL);
        stmt.setLong(FIND_ID, panelId);
        
        Map<String, Object> panel = new HashMap<String, Object>(4);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            panel.put(Panel.PK_WS_ID, results.getLong(WS_COL));
            panel.put(Panel.PK_ID, panelId);
            panel.put(Panel.PK_GROUP, results.getLong(GRP_COL));
            panel.put(Panel.PK_TYPE, results.getString(TYPE_COL));
            
        }
        
        return panel;
    }
    
    /**
     * 
     * @param wsId
     * @return
     * @throws SQLException
     */
    final List<Map<String, Object>> findPanels(long wsId) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(FIND_SQL);
        stmt.setLong(FIND_WS, wsId);
        
        List<Map<String, Object>> panels = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = null;
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            data = new HashMap<String, Object>(4);
            data.put(Panel.PK_WS_ID, wsId);
            data.put(Panel.PK_ID, results.getLong(ID_COL));
            data.put(Panel.PK_GROUP, results.getLong(GRP_COL));
            data.put(Panel.PK_TYPE, results.getString(TYPE_COL));
            
            panels.add(data);
        }
        
        return panels;
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     */
    final boolean deletePanel(long id) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(DELETE_SQL);
        stmt.setLong(DELETE_ID, id);
        int numRowsChanged = stmt.executeUpdate();
        
        return numRowsChanged > 0;
    }

    /**
     * Sets the specified properties for a panel. For properties that may have
     * already been set, this will override the current values.
     * 
     * @param panelId The panel for which to set the properties.
     * @param props A <code>Map</code> whose keys are the names of the 
     *      properties to set and whose value are the property values.
     *  
     * @throws SQLException
     */
    final void setProperties(long panelId, Map<String, String> props) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(SET_PROP_SQL);
        
        for (String name : props.keySet()) {
            stmt.setLong(SET_ID, panelId);
            stmt.setString(SET_NAME, name);
            stmt.setString(SET_VALUE, props.get(name));
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Sets a custom property value for the specified panel. Properties 
     * provide a simple key/value storage that third party code can use for 
     * persistent storage of information about a Panel's state.
     * 
     * @param panelId the panel for which to set this property
     * @param name the property to set
     * @param value the value to set for this property
     * @throws SQLException
     */
    final void setProperty(long panelId, String name, String value) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(SET_PROP_SQL);

        stmt.setLong(SET_ID, panelId);
        stmt.setString(SET_NAME, name);
        stmt.setString(SET_VALUE, value);

        stmt.executeUpdate();
    }
    
    /**
     * Deletes a custom property value for the specified panel. Properties 
     * provide a simple key/value storage that third party code can use for 
     * persistent storage of information about a Panel's state.
     * 
     * @param panelId the panel whose property is to be deleted
     * @param name the property to delete
     * @param value the value to set for this property
     * @throws SQLException
     */
    final void deleteProperty(long panelId, String name) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(DEL_PROP_SQL);

        stmt.setLong(DEL_ID, panelId);
        stmt.setString(DEL_NAME, name);

        stmt.executeUpdate();
    }
    
    /**
     * 
     * @param panelId
     * @return
     * @throws SQLException
     */
    final String getProperty(long panelId, String prop)
            throws SQLException {
        String value = null;
        
        if (getPropsStmt == null) 
            getPropsStmt = m_connection.prepareStatement(GET_PROP_SQL);
        
        getPropsStmt.setLong(GET_PROP_ID, panelId);
        getPropsStmt.setString(GET_PROP_NAME, prop);
        ResultSet results = getPropsStmt.executeQuery();
        if (results.next()) {
            value = results.getString(PROP_VALUE_COL);
        }
        
        return value;
    }
    
    /**
     * 
     * @param panelId
     * @return
     * @throws SQLException
     */
    final Map<String, String> getProperties(long panelId) throws SQLException {
        Map<String, String> props = new HashMap<String, String>();
        
        if (getPropsStmt == null) 
            getPropsStmt = m_connection.prepareStatement(FIND_PROPS_SQL);
        
        getPropsStmt.setLong(FIND_PROPS_ID, panelId);
        ResultSet results = getPropsStmt.executeQuery();
        while (results.next()) {
            String key = results.getString(PROP_NAME_COL);
            String value = results.getString(PROP_VALUE_COL);
            
            props.put(key, value);
        }
        
        return props;
    }
}

