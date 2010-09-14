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
package org.idch.persist;

import java.io.File;
import java.sql.Connection;
import java.util.ResourceBundle;

import junit.framework.TestCase;

public class ConnectionProviderTests extends TestCase {
    private static final String BUNDLE_NAME = "test";

    private String url;
    private String driver; 
    private String user;
    private String pass;
        
    private File sqlDirectory;
    
    public void setUp() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        url = bundle.getString("test.db.url");
        driver = bundle.getString("test.db.driver");
        user = bundle.getString("test.db.user");
        pass = bundle.getString("test.db.pass");
        
        sqlDirectory = new File(bundle.getString("test.db.sqldir"));
        assertTrue(sqlDirectory.canRead() && sqlDirectory.isDirectory());
    }
    
    public void tearDown() {
        
    }
    
    public void testInstantiateProvider() throws Exception {
        try {
            @SuppressWarnings("unused")
            ConnectionProvider provider = 
                new ConnectionProvider(url, driver, user, pass);
        } catch (Exception ex) {
            assertFalse("Could not intantiate a connection provider.", true);
        }
    }
    
    public void testEstablishConnection() throws Exception {
        ConnectionProvider provider = 
            new ConnectionProvider(url, driver, user, pass);
        
        Connection conn = provider.getConnection();
        assertNotNull("Failed to get connection.", conn);
        
        conn.close();
    }
    
    public void testExecuteScript() throws Exception {
        ConnectionProvider provider = 
            new ConnectionProvider(url, driver, user, pass);
//        Statement stmt = provider.getConnection().createStatement();
//        stmt.execute("DROP TABLE IF EXISTS TestTable");
        
        File file = new File(sqlDirectory, "simpleTest.sql");
        int ct = provider.executeScript(file);
        assertTrue("Unexpected number of number of statements executed. " +
        		"Expected 2, executed " + ct, ct == 2);
    }
    
}
