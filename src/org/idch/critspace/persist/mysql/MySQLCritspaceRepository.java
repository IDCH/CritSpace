/* Created on       Aug 26, 2010
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.idch.auth.User;
import org.idch.critspace.Panel;
import org.idch.critspace.persist.CritspaceRepository;
import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;
import org.idch.vprops.Group;
import org.idch.vprops.persist.mysql.GroupProxy;

public class MySQLCritspaceRepository extends CritspaceRepository {
    private static final String LOGGER = CritspaceRepository.class.getName(); 
    
    
    //========================================================================
    // DATABASE MANIPULATION METHODS
    //========================================================================
    

    /**
     * Attempts to determine whether or not the proper tables are defined for 
     * use by the <code>PropertyRepository</code>. 
     * 
     * @return <code>true</code> if the required tables are defined, 
     *      <code>false</code> if they are not.
     */
    public boolean probe() {
        List<String> sql = new ArrayList<String>(3);
        sql.add("SELECT ws_id, owner_id, name, visibility" +
                "  FROM CRIT_Workspaces;");
        sql.add("SELECT panel_id, ws_id, panel_type, vprops_grp  " +
                "  FROM CRIT_Panels");
        sql.add("SELECT panel_id, prop_name, prop_value" +
                "  FROM CRIT_PanelProps");
        return probe(sql);
    }
    
    
    //========================================================================
    // WORKSPACE METHODS
    //========================================================================
    
    
    public Map<String, Object> createWorkspace(User user, String name)
            throws RepositoryAccessException {
        
        Map<String, Object> data = null;
        Connection conn = null;
        try {
            conn = openTransaction();
            WorkspaceProxy proxy = new WorkspaceProxy(conn);
            data = proxy.create(user, name);
            
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not create workspace: " + name + ". " + 
                    ex.getMessage();
        
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return data;
    }

    public Map<String, Object> getWorkspace(long ownerId, String name) 
            throws RepositoryAccessException {
        
        Map<String, Object> data = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            WorkspaceProxy proxy = new WorkspaceProxy(conn);
            data = proxy.find(ownerId, name);
        } catch (Exception ex) {
            String msg = "Could not retrieve workspace: " + name + ". " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return data;
    }

    public List<Map<String, Object>> listWorkspaces(User user) 
            throws RepositoryAccessException {
        
        long id = (user != null) ? user.getId() : 0;
        List<Map<String, Object>> data = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            WorkspaceProxy proxy = new WorkspaceProxy(conn);
            
            data = proxy.list(id);
        } catch (Exception ex) {
            String msg = "Could not list workspaces: " + id + ". " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return data;
    }
    
    //========================================================================
    // PANEL METHODS
    //========================================================================
    
    public Map<String, Object> createPanel(long wsId, String type, 
            Group grp, Map<String, String> props) 
            throws RepositoryAccessException {
        
        Map<String, Object> data = null;
        Connection conn = null;
        try {
            conn = openTransaction();
            GroupProxy gproxy = new GroupProxy(conn);
            gproxy.createGroup(grp);
            
            PanelProxy proxy = new PanelProxy(conn);
            data = proxy.createPanel(wsId, grp.getId(), type);
            
            if (props != null) {
                long panelId = (Long)data.get(Panel.PK_ID);
                proxy.setProperties(panelId, props);
            }
            
            conn.commit();
            
            data.put(Panel.PK_PROPERTIES, props);
            data.put(Panel.PK_VPROPS, grp.toJSON());
        } catch (Exception ex) {
            String msg = "Could not create panel: " + type + ". " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return data;
    }
    
    public Map<String, Object> findPanel(long pId) 
        throws RepositoryAccessException {
        Map<String, Object> panel = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            PanelProxy proxy = new PanelProxy(conn);
            GroupProxy gproxy = new GroupProxy(conn);
            
            panel = proxy.findPanel(pId);
            Map<String, String> props = proxy.getProperties(pId);
            panel.put(Panel.PK_PROPERTIES, props);

            long grpId = (Long)panel.get(Panel.PK_GROUP);
            Map<String, Object> vprops = gproxy.restoreGroup(grpId);
            panel.put(Panel.PK_VPROPS, vprops);
                
        } catch (Exception ex) {
            String msg = "Could not retrieve panel: " + pId + ". " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
            
        return panel;
    }
   
    public List<Map<String, Object>> listPanels(long wsId) 
            throws RepositoryAccessException {
        
        List<Map<String, Object>> panels = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            PanelProxy proxy = new PanelProxy(conn);
            GroupProxy gproxy = new GroupProxy(conn);
            
            panels = proxy.findPanels(wsId);
            for (Map<String, Object> panel : panels) {
                long pId = (Long)panel.get(Panel.PK_ID);
                Map<String, String> props = proxy.getProperties(pId);
                panel.put(Panel.PK_PROPERTIES, props);
                
                long grpId = (Long)panel.get(Panel.PK_GROUP);
                Map<String, Object> vprops = gproxy.restoreGroup(grpId);
                panel.put(Panel.PK_VPROPS, vprops);
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve panels for workspace: " + 
                    wsId + ". " + ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
            
        return panels;
    }
    
    public boolean deletePanel(long panelId) 
            throws RepositoryAccessException {
        
        boolean success = false;
        Connection conn = null;
        try {
            conn = openTransaction();
            PanelProxy proxy = new PanelProxy(conn);
            success = proxy.deletePanel(panelId);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not delete panel: " + panelId + ". " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return success;
    }

    public void setProperty(long panelId, String prop, String value) 
            throws RepositoryAccessException {
        Connection conn = null;
        try {
            conn = openTransaction();
            PanelProxy proxy = new PanelProxy(conn);
            proxy.setProperty(panelId, prop, value);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could set property: " + prop + " = " + value + 
                    " for panel (" + panelId + "). " + ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
    }
    
    public void deleteProperty(long panelId, String prop) 
            throws RepositoryAccessException {
        Connection conn = null;
        try {
            conn = openTransaction();
            PanelProxy proxy = new PanelProxy(conn);
            proxy.deleteProperty(panelId, prop);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could delete property: " + prop + 
                    " for panel (" + panelId + "). " + ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
    }
    
    public String getProperty(long panelId, String prop) 
            throws RepositoryAccessException { 
        
        String value = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            PanelProxy proxy = new PanelProxy(conn);
            value = proxy.getProperty(panelId, prop);
        } catch (Exception ex) {
            String msg = "Could get property value for panel " +
            		"[" + panelId + ": " + prop + "]. " + ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
            
        return value;
    }
    
    public Map<String, String> listProperties(long panelId) 
            throws RepositoryAccessException { 
        
        Map<String, String> result = null;
        Connection conn = null;
        try {
            conn = openReadOnly();
            PanelProxy proxy = new PanelProxy(conn);
            result = proxy.getProperties(panelId);
        } catch (Exception ex) {
            String msg = "Could get property value for panel " +
                    "[" + panelId  + "]. " + ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
            
        return result;
    }
    
    public void setProperties(long panelId, Map<String, String> props) 
            throws RepositoryAccessException {
        Connection conn = null;
        try {
            conn = openTransaction();
            PanelProxy proxy = new PanelProxy(conn);
            proxy.setProperties(panelId, props);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could set properties for panel (" + panelId + "). " + 
                    ex.getMessage();
        
            LogService.logError(msg, LOGGER, ex);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
    }
}
