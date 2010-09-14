/* Created on       Aug 6, 2010
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
package org.idch.vprops.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.BadDataException;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.Group;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class PropertyGroupsServlet extends VPropServlet {
    
    public static final String GROUP_ID_PARAM = "id";
    
    private Group getGroup(String idParam, HttpServletResponse resp) 
    throws IOException {
        String errmsg = "Could not retrieve Group: ";
        
        Group group = null;
        try {
            idParam = StringUtils.trimToNull(idParam);
            if ((idParam != null) && StringUtils.isNumeric(idParam)) {
                group = s_repo.findGroup(Long.parseLong(idParam));
            } else {
                errmsg += "invalid group id (" + idParam + ").";
                resp.sendError(BAD_REQ, warn(errmsg, null));    
            }
                
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to retrieve data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
        
        return group;
    }
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        String id = req.getParameter(GROUP_ID_PARAM);
        Group group = getGroup(id, resp);
        
        if (resp.isCommitted())
            return;
        
        if (group == null) {
            resp.sendError(NOT_FOUND, 
                    "Could not find the specified group. id = " + id);
            return;
        }
        
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/javascript");
        JSONObject.writeJSONString(group.toJSON(), resp.getWriter());
    }
    
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // Placeholder. Will be used to implement updates to a group since 
        // updates should be idempotent.
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest req, HttpServletResponse resp) 
    throws IOException {
        String errmsg = "Failed to create property group: ";
        String data = req.getParameter("data");
        JSONParser parser = new JSONParser();
        try {
            Map<String, Object> dataMap = 
                (Map<String, Object>)parser.parse(data);
            Group group = new Group(dataMap);
            long id = group.getId();
            if (id >= 0) {
                errmsg += "the supplied group appears to exist (" + id + ").";
                resp.sendError(BAD_REQ, warn(errmsg, null));
                return;
            }
            
            // try to create the supplied group
            group = s_repo.createGroup(group);
            if (group != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                JSONObject.writeJSONString(group.toJSON(), resp.getWriter());
            } else {
                errmsg += "the repository was not able to complete " +
                		"this request.";
                resp.sendError(BAD_REQ, error(errmsg, null));
            }
             
        } catch (ParseException pe) {
            errmsg += "could not parse supplied data. ";
            resp.sendError(BAD_REQ, warn(errmsg, pe));
        } catch (BadDataException cce) {
            errmsg += "supplied group data improperly formated.";
            resp.sendError(BAD_REQ, warn(errmsg, cce));
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
}
