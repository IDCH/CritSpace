/* Created on       Aug 26, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *           ALL RIGHTS RESERVED. 
 */
package org.idch.critspace.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.idch.persist.RepositoryAccessException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 
 * @author Neal Audenaert
 */
@SuppressWarnings("serial")
public class WorkspaceServlet extends CritspaceServlet {
    
    public static final String PARAM_USER = "uId";
    public static final String PARAM_NAME = "name";
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        // TODO Tie into authentication system
        String errmsg = "Could not retrieve workspace: ";
        
        try {
            String name = req.getParameter(PARAM_NAME);
            String strId = req.getParameter(PARAM_USER);
            long id = (strId != null) ? getId(strId, resp) : 0;
            
            if (name == null) {
                // list all spaces visible to the current user
                List<Map<String, Object>> spaces = s_repo.listWorkspaces(null);
                
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                JSONArray.writeJSONString(spaces, resp.getWriter());
            } else { 
                // TODO need to validate permissions
                // retrieve the named space
                if (name.startsWith("/"))
                    name = name.substring(1);
                Map<String, Object> ws = s_repo.getWorkspace(id, name);
                
                if (ws == null) {
                    resp.sendError(NOT_FOUND, "Could not load workspace " + name);
                } else {
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("text/javascript");
                    JSONObject.writeJSONString(ws, resp.getWriter());
                }
            }
            
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        String errmsg = "Failed to create workspace: ";
        
        try {
            String name = req.getParameter(PARAM_NAME);
            Map<String, Object> data = s_repo.createWorkspace(null, name);
            
            if (data != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                JSONObject.writeJSONString(data, resp.getWriter());
            } else {
                errmsg += "does a panel with this name already exist?";
                resp.sendError(CONFLICT, warn(errmsg, null));
            }
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }

}
