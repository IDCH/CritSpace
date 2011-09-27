/* Created on       Aug 24, 2010
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

import java.util.List;
import java.util.Map;

import org.idch.auth.User;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.Group;

public interface ICritspaceRepository {

    public Map<String, Object> createWorkspace(User user, String name) 
            throws RepositoryAccessException;
    
    public Map<String, Object> getWorkspace(long userId, String name)
        throws RepositoryAccessException;
    
    public List<Map<String, Object>> listWorkspaces(User user) 
        throws RepositoryAccessException;
    
    public Map<String, Object> createPanel(long wsId, String type, 
            Group grp, Map<String, String> props) 
            throws RepositoryAccessException;
    
    public Map<String, Object> findPanel(long pId) 
        throws RepositoryAccessException;
    
    public List<Map<String, Object>> listPanels(long wsId) 
        throws RepositoryAccessException;
    
    public boolean deletePanel(long panelId) 
        throws RepositoryAccessException;

    public String getProperty(long panelId, String prop) 
        throws RepositoryAccessException;
    
    public Map<String, String> listProperties(long panelId) 
        throws RepositoryAccessException;
    
    public void setProperty(long panelId, String prop, String value) 
        throws RepositoryAccessException;
    
    public void deleteProperty(long panelId, String prop) 
        throws RepositoryAccessException;
    
    public void setProperties(long panelId, Map<String, String> props) 
        throws RepositoryAccessException;
}
