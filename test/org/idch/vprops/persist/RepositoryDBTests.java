/* Created on       Aug 10, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.persist;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.idch.persist.ConnectionProvider;

import junit.framework.TestCase;

/**
 * Tests the database creation/clean/drop methods on the repository. This test
 * is fragile, because if it is broken, it is likely to leave the database in
 * an unstable state, requiring manual intervention to fix it. I'm not too 
 * sure of a way around this other than using a persistence framework. That 
 * will come in time.
 * 
 * @author Neal Audenaert
 */
public class RepositoryDBTests extends TestCase {
    private static final String PROP_BUNDLE = "test";
    
    private PropertyRepository m_repository = null;
    private ConnectionProvider m_provider = null;
    
    public void setUp() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle(PROP_BUNDLE);
        m_repository = PropertyRepository.get(bundle);
        
        // get our own connection provider to probe the database.
        String url = bundle.getString(PropertyRepository.DB_URL_PROP);
        String driver = bundle.getString(PropertyRepository.DB_DRIVER_PROP);
        String user = bundle.getString(PropertyRepository.DB_USER_PROP);
        String pass = bundle.getString(PropertyRepository.DB_PASS_PROP);
        
        m_provider = new ConnectionProvider(url, driver, user, pass);
    }


    public void tearDown() throws Exception {
        // This might be broken (we are testing it after all) but at least 
        // we tried. Ideally, we'd test this with an in memory database
        m_repository.drop();    
    }
    
    /**
     * Implement my own probe function since we don't want to assume that the 
     * supplied repo probes correctly.
     */
    private boolean probe() {
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
    
    public void testCreate() throws Exception {
        assertTrue("Could not create repository.", m_repository.create());
        assertTrue("Probe failed. Invalid database structure.", probe());
    }
    
    public void testClean() throws Exception {
        // FIXME doesn't really test to see if the repo got cleaned out.
        //       Checks that it thinks that it cleaned the repository (a good 
        //       sign) and checks that the repo is still there afterwards.
        assertTrue("Could not create repository.", m_repository.create());
        assertTrue("Could not clean repository.", m_repository.clean());
        assertTrue("Probe failed. Invalid database structure.", probe());
    }
    
    public void testDrop() throws Exception {
        assertTrue("Could not create repository.", m_repository.create());
        assertTrue("Could not drop repository.", m_repository.drop());
        assertFalse("Probe failed. Invalid database structure.", probe());
    }

    
}
