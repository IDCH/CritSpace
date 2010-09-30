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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.idch.persist.ConnectionProvider;
import org.idch.persist.DatabaseException;
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
    
    private ConnectionProvider m_provider;
    
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
    
    @Override
    protected void initialize(ResourceBundle bundle)
        throws RepositoryAccessException {
        
        String url    = bundle.getString(DB_URL_PROP);
        String driver = bundle.getString(DB_DRIVER_PROP);
        String user   = bundle.getString(DB_USER_PROP);
        String pass   = bundle.getString(DB_PASS_PROP);
        
        try {
            m_provider = new ConnectionProvider(url, driver, user, pass);
        } catch (DatabaseException dbe) {
            throw new RepositoryAccessException("Could not connect to database", dbe);
        }
        File dir = new File(bundle.getString(DB_SRCRIPTS_PROP));
        if (dir.canRead() && dir.isDirectory()) {
            m_sqlDirectory = dir;
        } else { 
            m_sqlDirectory = null;
            LogService.logInfo("Could not load database configuration " +
                    "scripts.", LOGGER);
        } 
    }
    
    //========================================================================
    // DATABASE MANIPULATION METHODS
    //========================================================================
    
    // XXX Magic Strings (vprops.sql, etc) . Move to properties file
    
    /**
     * Helper method that executes the provided script file and probes to see
     * if the appropriate database tables are defined for the repository. 
     * 
     * @param sqlFile The SQL script file to be executed.  
     * @param expectSuccess Indicates whether this method should expect that 
     *      probing for the database will be successful. For instance, after 
     *      deleting the database, the probe should fail. 
     *      
     * @return <code>true</code> if the script was executed and the probe 
     *      returned the expected results, <code>false</code> otherwise. 
     * @throws IOException On failure due to IO problems (e.g., could not 
     *      read the script file.
     * @throws SQLException On failure due to database access problems.
     */
    private boolean executeScriptAndProbe(File sqlFile, boolean expectSuccess) 
            throws RepositoryAccessException {
        
        boolean success = false;
        if (!sqlFile.canRead() || !sqlFile.isFile()) {
            throw new RepositoryAccessException("Could not locate script file. " +
                    "The file I tried (" + sqlFile.getAbsolutePath() + ") " +
            "either does not exist or cannot be read.");
        }
        
        try {
            m_provider.executeScript(sqlFile);
            success = (expectSuccess) ? probe() : !probe();
        } catch(Exception ex) {
            throw new RepositoryAccessException(ex);
        }
        
        return success;
    }

    /**
     * Attempts to determine whether or not the proper tables are defined for 
     * use by the <code>PropertyRepository</code>. 
     * 
     * @return <code>true</code> if the required tables are defined, 
     *      <code>false</code> if they are not.
     */
    public boolean probe() {
        // we'll code these in here since these represent the tables and fields
        // that this particular repository implementation expects to be present.
        String sqlProbePropertyTypes = 
            "SELECT type_id, css, name, description, format, config " +
            "  FROM PropertyTypes;";
        String sqlProbePropertyGroups = 
            "SELECT group_id, parent_group, group_type " +
            "  FROM PropertyGroups";
        String sqlProbeVisualProperties = 
            "SELECT vprop_id, group_id, type_id, prop_value, enabled " +
            "  FROM VisualProperties";
        String sqlProbePropertyConfigs = 
            "SELECT vprop_id, prop_value, enabled, units, minimum, " +
            "       maximum, on_value, off_value, options_only, regex, regext " +
            "  FROM PropertyConfigurations";  
        String sqlProbeConfigOptions = 
            "SELECT vprop_id, option_value " +
            "  FROM ConfigurationOptions";
        
        boolean success = false;
        
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = m_provider.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery(sqlProbePropertyTypes);
            stmt.executeQuery(sqlProbePropertyGroups);
            stmt.executeQuery(sqlProbeVisualProperties);
            stmt.executeQuery(sqlProbePropertyConfigs);
            stmt.executeQuery(sqlProbeConfigOptions);
            success = true;
            
        } catch (Exception ex) {
            // not unexpected - we may test under scenarios when the database
            // doesn't exist. 
            
            // XXX Implment these tests by querying for database metadata 
            success = false;
        } finally {
            if (conn != null) {
                try { conn.close(); }
                catch (Exception ex) { 
                    /* complain loudly. leaked resources are bad. */
                    assert false : "Failed to close database connection";
                    throw new RuntimeException("Failed to close database connection");
                }
            }
        }
       
        return success;
    }
    
    /**
     * Create the database tables required for the MySQL PropertyRepository,
     * silently deleting any existing tables or data. Use with caution. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was created succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean create() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, "vprops.sql");
        return executeScriptAndProbe(sqlFile, true);
    }
    
    /** 
     * Attempts to load the default types into the database.
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public boolean initTypes() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, "types.sql");
        if (!sqlFile.exists() || !sqlFile.canRead() || !sqlFile.isFile())
            return false;
        
        return executeScriptAndProbe(sqlFile, true);
    }
    
    /**
     * Deletes all data (but not the database tables) from the database. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was cleaned succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean clean() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, "clean.sql");
        return executeScriptAndProbe(sqlFile, true);
    }
    
    /**
     * Drops all database tables and data associated with this 
     * PropertyRepsoitory. <code>probe</code> should return false after 
     * successfull completion of this method. 
     * 
     * @return <code>true</code> if the database was deleted succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean drop() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, "drop.sql");
        return executeScriptAndProbe(sqlFile, false);
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