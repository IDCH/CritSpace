/* Created on       Aug 13, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.persist.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.idch.persist.DBUtils;
import org.idch.util.LogService;
import org.idch.vprops.Group;
import org.idch.vprops.VisualProperty;

public class GroupProxy {
    private static final String LOGGER = GroupProxy.class.getName();
    
 
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    private final Connection m_connection;
    
    private PreparedStatement getSubGroups = null;
    
    //========================================================================
    // CONSTRUCTORS & FINALIZATION
    //========================================================================
    
    /**
     * 
     * @param connection
     */
    public GroupProxy(Connection connection) {
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
        DBUtils.close(getSubGroups);
    }
    
    //========================================================================
    // RESTORATION METHODS & CONSTANTS
    //========================================================================
    
    private final static int CREATE_PARENT_COL = 1;
    private final static int CREATE_TYPE_COL   = 2;
    private final static int CREATE_NAME_COL   = 3;
    private final static String CREATE_SQL =
        "INSERT INTO PropertyGroups (parent_group, group_type, group_name) " +
        "VALUES (?, ?, ?)";
    
    /**
     * 
     * @param stmt
     * @param group
     * @param name
     * @throws SQLException
     */
    private final void createGroup(PreparedStatement stmt, Group group, String name) 
    throws SQLException {
        stmt.setString(CREATE_NAME_COL, name);
        stmt.setString(CREATE_TYPE_COL, group.getType());
        
        int numRowsChanged = stmt.executeUpdate();
        if (numRowsChanged != 1)
            throw new SQLException("Could not create group: no rows updated.");
        
        long id = -1;
        ResultSet results = stmt.getGeneratedKeys();
        if (results.next()) {
            id = results.getLong(1);
            group.setId(id);
        } else {
            throw new SQLException("Could not retrieve group id");
        }
    }
    
    /**
     * 
     * @param root
     * @throws SQLException
     */
    public final void createGroup(Group root) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(CREATE_SQL);
        
        Stack<Group> parents = new Stack<Group>();
        
        stmt.setNull(CREATE_PARENT_COL, Types.INTEGER);
        createGroup(stmt, root, "root");
        parents.push(root);
                    
        Group parent = null;
        VisualPropertyProxy propProxy = new VisualPropertyProxy(m_connection);
        do {
            parent = parents.pop();
            stmt.setLong(CREATE_PARENT_COL, parent.getId());
            
            // save the properties
            Map<String, VisualProperty> vprops= parent.getProperties();
            propProxy.create(parent, vprops);
            
            // save the sub-groups, adding them to the stack
            Set<String> subgroups = parent.listSubGroups();
            for (String groupName : subgroups) {
                Group group = parent.getSubGroup(groupName);
                parents.push(group);
                
                createGroup(stmt, group, groupName);
            }
            
        } while (!parents.empty());
    }

    //========================================================================
    // RESTORATION METHODS & CONSTANTS
    //========================================================================
    private final static String R_SUBGROUPS_SQL =
        "SELECT group_id, group_type, group_name " +
        "  FROM PropertyGroups " +
        "  WHERE parent_group = ?";
    
    private final static String R_ROOT_SQL =
        "SELECT group_type FROM PropertyGroups WHERE group_id = ?";
    
    private final static int RESTORE_ID = 1;
    private final static String ID_COL     = "group_id";
    private final static String TYPE_COL   = "group_type";
    private final static String NAME_COL   = "group_name";
    
    /**
     * Initilizes the root node in a Group hierarchy.
     * 
     * @param id The id of the top-level group to retrieve.
     * @return The type of the top level group in the hierarchy
     * 
     * @throws SQLException if there is a database access error 
     */
    private final String getRootGroup(long id) throws SQLException {
        String type = null;
        ResultSet results = null;
        PreparedStatement stmt =
            m_connection.prepareStatement(R_ROOT_SQL);
        try {
            stmt.setLong(RESTORE_ID, id);
            results = stmt.executeQuery();
            if (results.next()) {
                type = results.getString(TYPE_COL);
            } 
        } finally {
            if (stmt != null)
                stmt.close();
        }
        
        return type;
    }
    
    private Map<String, Object> buildBaseGroupMap(long id, String type, 
            VisualPropertyProxy vpProxy) throws SQLException {
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(Group.PK_ID, id);
        data.put(Group.PK_TYPE, type);
        data.put(Group.PK_GROUPS, new HashMap<String, Object>());
        data.put(Group.PK_PROPERTIES, vpProxy.restoreGroupProperties(id));
        
        return data;
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public final Map<String, Object> restoreGroup(long id) throws SQLException {
        Map<String, Object> subgroups = null;
        VisualPropertyProxy vpProxy = new VisualPropertyProxy(m_connection);
        Stack<Map<String, Object>> parents = new Stack<Map<String, Object>>();
        
        if (getSubGroups == null) {
            getSubGroups = m_connection.prepareStatement(R_SUBGROUPS_SQL);
        }
        
        String type = getRootGroup(id);
        if (type == null) 
            return null;
        
        Map<String, Object> data = buildBaseGroupMap(id, type, vpProxy); 
        parents.push(data);
        
        do {
            // get the next group to process
            Map<String, Object> parent = parents.pop();
            long parentId = (Long)parent.get(Group.PK_ID);
            subgroups = (Map<String, Object>)parent.get(Group.PK_GROUPS);
            
            // query all subgroups of this group
            getSubGroups.setLong(RESTORE_ID, parentId);
            ResultSet results = getSubGroups.executeQuery();
            while (results.next()) {
                long grpId     = results.getLong(ID_COL);
                String grpType = results.getString(TYPE_COL);
                String grpName = results.getString(NAME_COL);
                
                Map<String, Object> grp =
                    buildBaseGroupMap(grpId, grpType, vpProxy);
                subgroups.put(grpName, grp);
                
                parents.push(grp);
            }
            
        } while (!parents.empty());
        
        
        return data;
    }
}
