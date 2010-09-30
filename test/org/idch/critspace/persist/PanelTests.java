/* Created on       Aug 27, 2010
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
package org.idch.critspace.persist;

import java.util.HashMap;
import java.util.Map;

import org.idch.critspace.Panel;
import org.idch.critspace.Workspace;
import org.idch.persist.DBBackedRepository;
import org.idch.vprops.Group;
import org.idch.vprops.persist.PropertyRepository;

import junit.framework.TestCase;

public class PanelTests extends TestCase {
    
    private static final String PROP_BUNDLE = "test_repo";
    
    private CritspaceRepository m_repository = null;
    private PropertyRepository m_propRepo = null;
    private WSUtils m_wsUtils = null;
    
    private long m_wsId = -1;
    
    public void setUp() throws Exception {
        DBBackedRepository.setPropertyBundle(PROP_BUNDLE);
        
        m_propRepo = PropertyRepository.get();
        m_repository = CritspaceRepository.get();
        m_repository.drop();
        m_propRepo.drop();
        
        m_propRepo.create();
        m_repository.create();
        
        
        Map<String, Object> data = 
            m_repository.createWorkspace(null, "test/me/stuff");
        assertNotNull("No data returned", data);
        
        m_wsId = (Long)data.get(Workspace.PK_WS_ID);
        
        m_wsUtils = new WSUtils(m_propRepo);
    }
    
    public void tearDown() throws Exception {
        // This might be broken (we are testing it after all) but at least 
        // we tried. Ideally, we'd test this with an in memory database
        m_repository.drop();
        m_propRepo.drop();
    }
    
    @SuppressWarnings("unchecked")
    public void testCreatePanel() throws Exception {
        Group group = m_wsUtils.configureTestGroup();
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("prop1", "a custom property");
        props.put("prop2", "another custom property");
        
        Map<String, Object> data = 
            m_repository.createPanel(m_wsId, "testpanel.type", group, props);
        
        assertNotNull("No data returned", data);
        
        // check groups
        Map<String, Object> vprops = 
            (Map<String, Object>)data.get(Panel.PK_VPROPS);
        Group grp = new Group(vprops);
        assertEquals("Property groups do not match", group.getId(), grp.getId());
        
        // check properties
        Map<String, String> properties = 
            (Map<String, String>)data.get(Panel.PK_PROPERTIES);
        for (String prop : props.keySet()) {
            assertEquals("Properties do not match.", 
                    props.get(prop), properties.get(prop));
        }
        assertEquals("Size of property sets does not match.", 
                props.size(), properties.size());
    }
    
    @SuppressWarnings("unchecked")
    public void testFindPanel() throws Exception {
        Group group = m_wsUtils.configureTestGroup();
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("prop1", "a custom property");
        props.put("prop2", "another custom property");
        
        Map<String, Object> data = 
            m_repository.createPanel(m_wsId, "testpanel.type", group, props);
        assertNotNull("No data returned", data);
        
        long id = (Long)data.get(Panel.PK_ID);
        long wsId = (Long)data.get(Panel.PK_WS_ID);
        String type = (String)data.get(Panel.PK_TYPE);
        
        Map<String, Object> data2 = m_repository.findPanel(id);
        assertNotNull("No data returned", data2);
        
        assertTrue("IDs do not match", (Long)data2.get(Panel.PK_ID) == id);
        assertTrue("Workspace IDs do not match", (Long)data2.get(Panel.PK_WS_ID) == wsId);
        assertEquals("Types do not match", (String)data2.get(Panel.PK_TYPE), type);
        
        // check groups
        Map<String, Object> vprops = 
            (Map<String, Object>)data2.get(Panel.PK_VPROPS);
        Group grp = new Group(vprops);
        assertEquals("Property groups do not match", group.getId(), grp.getId());
        
        // check properties
        Map<String, String> properties = 
            (Map<String, String>)data2.get(Panel.PK_PROPERTIES);
        for (String prop : props.keySet()) {
            assertEquals("Properties do not match.", 
                    props.get(prop), properties.get(prop));
        }
        assertEquals("Size of property sets does not match.", 
                props.size(), properties.size());
    }
}
