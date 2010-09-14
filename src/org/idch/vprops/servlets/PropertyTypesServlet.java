/* Created on       Aug 6, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.idch.persist.BadDataException;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.PropertyType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class PropertyTypesServlet extends VPropServlet {
    
    public static final String TYPE_ID_PARAM = "id";
    
    /**
     * 
     * @param resp
     * @throws RepositoryAccessException
     * @throws IOException
     */
    private void listTypes(HttpServletResponse resp)
    throws RepositoryAccessException, IOException {
     
        List<PropertyType> types = s_repo.listPropertyTypes();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (PropertyType type : types) {
            data.add(type.toJSON());
        }
        
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/javascript");
        JSONArray.writeJSONString(data, resp.getWriter());
    }
    
    /**
     * 
     * @param id
     * @param resp
     * @throws RepositoryAccessException
     * @throws IOException
     */
    private void getType(String id, HttpServletResponse resp)
    throws RepositoryAccessException, IOException {
        PropertyType type = s_repo.findPropertyType(id);
        
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/javascript");
        JSONObject.writeJSONString(type.toJSON(), resp.getWriter());
    }
    
    /**
     * 
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
       
        String errmsg = "";
        try {
            String id = req.getParameter(TYPE_ID_PARAM);
            if (id != null) {
                getType(id, resp);
            } else {
                listTypes(resp);
            }
                
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to retrieve data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
    
    /** 
     * Handles requests to set the name and/or description of a particular
     * Property type.
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        
    }
    
    /**
     * Handles requests to create new PropertyTypes.
     * 
     */
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        // NOTE Creating a property type is not idempotent. Since property
        //      type values are not updated, if a type already exists, this 
        //      method will fail.
        
        String errmsg = "Could not creat property type: ";
        String data = req.getParameter("data");
        JSONParser parser = new JSONParser();
        try {
            Map<String, Object> json = (Map<String, Object>)parser.parse(data);
            PropertyType type = new PropertyType(json);
            
            boolean success = s_repo.createPropertyType(type);
            
            if (success) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                JSONObject.writeJSONString(type.toJSON(), resp.getWriter());
            } else {
                errmsg += "the repository was not able to complete " +
                        "this request. The may be the result of trying " +
                        "to create a duplicate property type.";
                resp.sendError(BAD_REQ, error(errmsg, null));
            }
            
        } catch (ParseException pe) {
            errmsg += "could not parse supplied data.";
            resp.sendError(BAD_REQ, warn(errmsg, pe));
        } catch (BadDataException cce) {
            errmsg += "supplied property type improperly formated.";
            resp.sendError(BAD_REQ, warn(errmsg, cce));
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
}
