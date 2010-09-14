/* Created on       Aug 27, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *           ALL RIGHTS RESERVED. 
 */
package org.idch.critspace.persist;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.idch.persist.ConnectionProvider;
import org.idch.persist.DBBackedRepository;

import junit.framework.TestCase;

public class RepoTests extends TestCase {
    
    private static final String PROP_BUNDLE = "test_repo";
    
    private CritspaceRepository m_repository = null;
    private ConnectionProvider m_provider = null;
    
    public void setUp() throws Exception {
        DBBackedRepository.setPropertyBundle(PROP_BUNDLE);
        m_repository = CritspaceRepository.get();
        
        // get our own connection provider to probe the database.
        ResourceBundle bundle = ResourceBundle.getBundle(PROP_BUNDLE);
        
        String url = bundle.getString(DBBackedRepository.DB_URL_PROP);
        String driver = bundle.getString(DBBackedRepository.DB_DRIVER_PROP);
        String user = bundle.getString(DBBackedRepository.DB_USER_PROP);
        String pass = bundle.getString(DBBackedRepository.DB_PASS_PROP);
        
        m_provider = new ConnectionProvider(url, driver, user, pass);
    }
    
    public void tearDown() throws Exception {
        // This might be broken (we are testing it after all) but at least 
        // we tried. Ideally, we'd test this with an in memory database
        m_repository.drop();    
    }
    
    public boolean probe() {
        List<String> statements = new ArrayList<String>(3);
        statements.add("SELECT ws_id, owner_id, name, visibility" +
                "  FROM CRIT_Workspaces;");
        statements.add("SELECT panel_id, ws_id, panel_type, vprops_grp  " +
                "  FROM CRIT_Panels");
        statements.add("SELECT panel_id, prop_name, prop_value" +
                "  FROM CRIT_PanelProps");
        
        boolean success = false;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = m_provider.getConnection();
            stmt = conn.createStatement();
            for (String sql : statements) 
                stmt.executeQuery(sql);
            success = true;
            
        } catch (Exception ex) {
            // not unexpected - we may test under scenarios when the database
            // doesn't exist. 
            
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
