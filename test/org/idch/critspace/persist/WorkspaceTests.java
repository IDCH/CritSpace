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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.idch.critspace.Workspace;
import org.idch.persist.DBBackedRepository;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.persist.PropertyRepository;
import org.json.simple.JSONObject;

import junit.framework.TestCase;

public class WorkspaceTests extends TestCase {
    
    private static final String PROP_BUNDLE = "test_repo";
    
    private CritspaceRepository m_repository = null;
    
    public void setUp() throws Exception {
        DBBackedRepository.setPropertyBundle(PROP_BUNDLE);
        PropertyRepository propRepo = PropertyRepository.get();
        
        m_repository = CritspaceRepository.get();
        m_repository.drop();
        propRepo.drop();
        
        propRepo.create();
        m_repository.create();
    }
    
    public void tearDown() throws Exception {
        // This might be broken (we are testing it after all) but at least 
        // we tried. Ideally, we'd test this with an in memory database
        m_repository.drop();    
        
        PropertyRepository propRepo = PropertyRepository.get();
        propRepo.drop();
    }
    
    public void testCreateWorkspace() throws Exception {
        Map<String, Object> data = 
            m_repository.createWorkspace(null, "test/me/stuff");
        assertNotNull("No data returned", data);
        
        long id = (Long)data.get(Workspace.PK_WS_ID);
        assertTrue("Invalid ID", id > 0);
        assertNotNull("Could not stringify JSON data", 
                JSONObject.toJSONString(data));
        assertTrue("Workspace is not public", 
                ((String)data.get(Workspace.PK_VISIBLE)) == "public");
    }
    
    public void testDuplicateWorkspace() throws Exception {
        Map<String, Object> data = 
            m_repository.createWorkspace(null, "test/me/stuff");
        assertNotNull("No data returned", data);
        
        try {
            Map<String, Object> data2 = 
                m_repository.createWorkspace(null, "test/me/stuff");
            assertNull("Should not have created workspace.", data2);
        } catch (RepositoryAccessException rae) {
            // that's fine.
        }
    }
    
    public void testRetrieveWorkspace() throws Exception {
        Map<String, Object> data = 
            m_repository.createWorkspace(null, "test/me/stuff");
        assertNotNull("No data returned", data);
        
        long ownerId = (Long)data.get(Workspace.PK_OWNER);
        String name  = (String)data.get(Workspace.PK_NAME);

        Map<String, Object> data2 = m_repository.getWorkspace(ownerId, name);
        assertNotNull("No data returned", data2);
        
        assertFalse("Data maps are the same object", data == data2);
        
        long id = (Long)data.get(Workspace.PK_WS_ID);
        long id2 = (Long)data2.get(Workspace.PK_WS_ID);
        
        String vis = (String)data.get(Workspace.PK_VISIBLE);
        String vis2 =(String)data2.get(Workspace.PK_VISIBLE);
        
        long ownerId2 = (Long)data.get(Workspace.PK_OWNER);
        String name2  = (String)data.get(Workspace.PK_NAME);
        assertEquals("Workspace IDs are different", id, id2);
        assertEquals("Workspace names are different", name, name2);
        assertEquals("Workspace owners are different", ownerId, ownerId2);
        assertEquals("Workspace visibility are different", vis, vis2);
    }
    
    public void testListWorkspace() throws Exception {
        String name1 = "test/me/stuff";
        String name2 = "test/me/stuff-2";
        String name3 = "test/me/stuff-3";
        String name4 = "test/";
        
        m_repository.createWorkspace(null, name1);
        m_repository.createWorkspace(null, name2);
        m_repository.createWorkspace(null, name3);
        m_repository.createWorkspace(null, name4);
      
        Set<String> names = new HashSet<String>();
        List<Map<String, Object>> repos = m_repository.listWorkspaces(null);
        for (Map<String, Object> repo : repos) {
            names.add((String)repo.get(Workspace.PK_NAME));
        }
        
        assertTrue("Not all workspaces retrieved", 
                names.contains(name1) && names.contains(name2) &&
                names.contains(name3) && names.contains(name4));
        assertTrue("Unexpected number of workspaces retrieved", 
                names.size() == 4);
        
    }
}
