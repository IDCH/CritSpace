/* Created on       Aug 9, 2010
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
package org.idch.vprops.persist.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.idch.persist.RepositoryAccessException;
import org.idch.util.Cache;
import org.idch.util.LogService;
import org.idch.vprops.Group;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.PropertyType;
import org.idch.vprops.VisualProperty;
import org.idch.vprops.persist.IPropertyRepository;
import org.idch.vprops.persist.PropertyRepository;

public final class MySQLPropertyRepository extends PropertyRepository {
    private final static String LOGGER = IPropertyRepository.class.getName();
    
//    private ConnectionProvider m_provider;
    
    private File m_sqlDirectory; 
    
    private Cache<String, PropertyType> m_propTypeCache = 
        new Cache<String, PropertyType>("propTypeCache", 50);
    
    
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    
    /**
     * 
     */
    public MySQLPropertyRepository() { }
    
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
        sql.add("SELECT type_id, css, name, description, format, config " +
            "  FROM PropertyTypes");
        sql.add("SELECT group_id, parent_group, group_type " +
            "  FROM PropertyGroups");
        sql.add("SELECT vprop_id, group_id, type_id, prop_value, enabled " +
            "  FROM VisualProperties");
        sql.add("SELECT vprop_id, prop_value, enabled, units, minimum, " +
            "       maximum, on_value, off_value, options_only, regex, regext " +
            "  FROM PropertyConfigurations");  
        sql.add("SELECT vprop_id, option_value " +
            "  FROM ConfigurationOptions");
        
        return probe(sql);
       
    }
    
    /** 
     * Attempts to load the default types into the database.
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public boolean initTypes() throws RepositoryAccessException {
        // XXX Magic String
        File sqlFile = new File(m_sqlDirectory, "types.sql");
        if (!sqlFile.exists() || !sqlFile.canRead() || !sqlFile.isFile())
            return false;
        
        return executeScriptAndProbe(sqlFile, true);
    }
    
    //========================================================================
    // PROPERTY TYPE METHODS
    //========================================================================
    
    /**
     * 
     * @param id
     * @param css
     * @param name
     * @param desc
     * @param config
     * 
     * @return
     * @throws SQLException
     */
    public PropertyType createPropertyType(String id, String css, 
            String name, String desc, PropertyConfig config) 
            throws RepositoryAccessException {
        
        PropertyType type = new PropertyType(id, css, name, desc, config);
        if (!createPropertyType(type))
            type = null;
        
        return type;
    }
    
    /**
     * 
     */
    public boolean createPropertyType(PropertyType type) throws RepositoryAccessException {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = openTransaction();
            synchronized (m_propTypeCache) {
                PropertyTypeProxy proxy = new PropertyTypeProxy(conn);
                if (proxy.create(type)) { 
                    conn.commit();
                    success = true;
                    
                    m_propTypeCache.cache(type.getId(), type);
                } else {
                    type = null;
                    rollback(conn);
                }
            }            
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not create property type. " +
        		"(" + type.getId() + ", " + type.getCSS() + ", " + 
        		type.getName() + "): " + ex.getMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return success;
    }
    
    public List<PropertyType> listPropertyTypes() 
    throws RepositoryAccessException {
        List<PropertyType> types = null; 
        Connection conn = null;
        
        try {
            conn = openReadOnly();
            synchronized (m_propTypeCache) {
                PropertyTypeProxy proxy = new PropertyTypeProxy(conn);
                types = proxy.listAll();
                
                for (PropertyType type : types) {
                    m_propTypeCache.cache(type.getId(), type);
                }
            }
        } catch (Exception ex) {
            String msg = "Could not list property types: " + 
                    ex.getLocalizedMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return types;
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws RepositoryAccessException
     */
    public PropertyType findPropertyType(String id)
            throws RepositoryAccessException {
        return findPropertyType(id, false);
    }
    
    /**
     * 
     * @param id
     * @param bypassCache
     * @return
     * @throws RepositoryAccessException
     */
    public PropertyType findPropertyType(String id, boolean bypassCache)
            throws RepositoryAccessException {
        PropertyType type = null; 
        Connection conn = null;
            
        try {
            synchronized (m_propTypeCache) {
                
                type = m_propTypeCache.get(id);
                if (type == null || bypassCache) {  
                    conn = openReadOnly();
                    PropertyTypeProxy proxy = new PropertyTypeProxy(conn);
                    type = proxy.restore(id);
                    
                    m_propTypeCache.cache(id, type);
                }
            }
        } catch (Exception ex) {
            String msg = "Could not find property type: " + id + "." +
                    ex.getLocalizedMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return type;
    }

    //========================================================================
    // VISUAL PROPERTY METHODS
    //========================================================================
    
    /**
     * 
     * @param type
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty createVisualProperty(PropertyType type) 
        throws RepositoryAccessException {
        return createVisualProperty(type, type.getConfig(), null, null);
    }
    
    /**
     * 
     * @param type
     * @param defaults
     * @param value
     * @param enabled
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty createVisualProperty(PropertyType type,
            PropertyConfig defaults, String value, Boolean enabled)
            throws RepositoryAccessException {

        Connection conn = null;
        VisualProperty vprop = null;
        try {
            conn = openTransaction();
            
            VisualPropertyProxy proxy = new VisualPropertyProxy(conn);
            vprop = proxy.create(type, defaults, value, enabled);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not create visual property: " + 
                    type.getId() + ". " + ex.getMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return vprop;
    }
    
    /**
     * Updates the database record for the provided property.
     * 
     * @param prop The property to update.
     * @return
     * @throws RepositoryAccessException
     */
    public boolean updataVisualProperty(VisualProperty prop) 
    throws RepositoryAccessException {
        boolean success = false;
        Connection conn = null;
        try {
            conn = openTransaction();
            VisualPropertyProxy proxy = new VisualPropertyProxy(conn);
            success = proxy.update(prop);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not update visual property: " + 
                    prop.getId() + ". " + ex.getMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return success;
    }
    
    
    
    /**
     * 
     * @param id
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty findVisualProperty(long id) 
    throws RepositoryAccessException {
        return findVisualProperty(id, false);
    }
    
    /**
     * 
     * @param id
     * @param bypassCache
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty findVisualProperty(long id, boolean bypassCache) 
    throws RepositoryAccessException {
        // TODO implement caching
        Connection conn = null;
        VisualProperty vprop = null;
        try {
            conn = openTransaction();
            VisualPropertyProxy proxy = new VisualPropertyProxy(conn);
            vprop = proxy.restore(id);
        } catch (Exception ex) {
            String msg = "Could not restore visual property: " + id + ". " + 
                    ex.getMessage();

            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return vprop;
    }
    
    //========================================================================
    // GROUP METHODS
    //========================================================================
    
    public Group createGroup(Group group) throws RepositoryAccessException {
        Connection conn = null;
        try {
            conn = openTransaction();
            GroupProxy proxy = new GroupProxy(conn);
            proxy.createGroup(group);
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not create group." + ex.getMessage();
            
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return group;
    }
    
    public Group findGroup(long id) throws RepositoryAccessException {
        Group group = null;
        Connection conn = null;
        try {
            conn = openTransaction();
            GroupProxy proxy = new GroupProxy(conn);
            Map<String, Object> data = proxy.restoreGroup(id);
            group = new Group(data);
        } catch (Exception ex) {
            String msg = "Could not create group." + ex.getMessage();
            
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return group;
    }
}