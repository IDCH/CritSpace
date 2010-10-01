/* Created on       Aug 26, 2010
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
package org.idch.critspace.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.BadDataException;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.Group;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class PanelsServlet extends CritspaceServlet {
    
    public final static String PARAM_ID    = "id";
    public final static String PARAM_WS_ID = "ws";
    public final static String PARAM_TYPE  = "type";
    public final static String PARAM_VRPOP = "vprops";
    public final static String PARAM_PROPS = "props";
    
    public final static String PARAM_PROP_NAME  = "prop";
    public final static String PARAM_PROP_VALUE = "v";
    
    /**
     * 
     * @param parser
     * @param req
     * @param resp
     * @return
     * @throws ParseException
     * @throws BadDataException
     */
    @SuppressWarnings("unchecked")
    private Group getVProps( 
            JSONParser parser, HttpServletRequest req, HttpServletResponse resp)
            throws ParseException, BadDataException {
        String strVprop = StringUtils.trimToNull(req.getParameter(PARAM_VRPOP));
        if (strVprop == null) 
            return null;
        
        Map<String, Object> vpropData = 
            (Map<String, Object>)parser.parse(strVprop);
        return new Group(vpropData);
    }
    
    /**
     * 
     * @param parser
     * @param req
     * @param resp
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getPanelProperties(
            JSONParser parser, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ParseException {
        String strProps = StringUtils.trimToNull(req.getParameter(PARAM_PROPS));
        if (strProps == null) 
            return null;
        
        Map<String, Object> propsData = 
            (Map<String, Object>)parser.parse(strProps);
        Map<String, String> props = new HashMap<String, String>();
        for (String name : propsData.keySet()) {
            Object obj = propsData.get(name);
            
            String value = ""; 
            if (obj instanceof String)
                value = (String)obj;
            else if (obj instanceof Number) {
                value = obj.toString();
            } else if (obj instanceof Boolean) {
                value = obj.toString();
            } else if (obj instanceof List) {
                value = JSONArray.toJSONString((List<Object>)obj);
            } else if (obj instanceof Map) {
                value = JSONObject.toJSONString((Map<String, Object>)obj);
            } else {
                String msg = "Could not process panel properties. " +
                        "Unexpected value format: " + obj.getClass().getName();
                resp.sendError(INTERNAL_ERROR, error(msg, null));
                throw new IOException(msg);
            }
           
            props.put(name, value);
        }
        
        return props;
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        String errmsg = "Could retrieve the panel: ";

        String response = null;
        try {
            String pId = req.getParameter(PARAM_ID);
            String wsId = req.getParameter(PARAM_WS_ID); 
            if (pId != null) {
                // get by panel ID
                long id = getId(pId, resp);
                Map<String, Object> data = s_repo.findPanel(id);
                response = JSONObject.toJSONString(data);
            } else if (wsId != null) {
                // get by workspace ID 
                long id = getId(wsId, resp);
                List<Map<String, Object>> data = s_repo.listPanels(id);
                response = JSONArray.toJSONString(data);
            } else {
                // bad request
            }
            
            if (response != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                PrintWriter out = resp.getWriter();
                out.write(response);
                out.flush();
            }
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }

    public void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        String errmsg = "Could set properties: ";
        
        try {
            long panelId = getId(req.getParameter(PARAM_ID), resp);
            if (resp.isCommitted())
                return;
            if (req.getParameter(PARAM_PROPS) == null) {
                String prop  = req.getParameter(PARAM_PROP_NAME);
                String value = req.getParameter(PARAM_PROP_VALUE);
                
                if ((prop == null) || (value == null)) {
                    errmsg += "property name and value must be supplied.";
                    resp.sendError(BAD_REQ, warn(errmsg, null));
                    return;
                }

                s_repo.setProperty(panelId, prop, value);
                resp.setStatus(CREATED);
            } else {
                JSONParser parser = new JSONParser();
                Map<String, String> props = getPanelProperties(parser, req, resp);
                if (resp.isCommitted())
                    return;

                s_repo.setProperties(panelId, props);
                resp.setStatus(CREATED);
            }
            
        } catch (ParseException pe) {
            errmsg += "could not parse supplied data. ";
            resp.sendError(BAD_REQ, warn(errmsg, pe));
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to save data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
    
    /** 
     * Creates a new Panel from the information in the supplied request.
     * 
     * Expects a request with the following parameters
     * <ul>
     *   <li><em>ws</em>  The id of the workspace in which to create 
     *          the property</li>
     *   <li><em>type</em> The type of property to be created</li>
     *   <li><em>vprops</em> The JSON object literal representing the 
     *          new visual propries group associated with this panel</li>
     *   <li><em>props</em> A JSON object literal</li>
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        String errmsg = "Could not create panel: ";
        JSONParser parser = new JSONParser();
        
        String type = StringUtils.trimToNull(req.getParameter(PARAM_TYPE));
        
        try {
            long wsId = getId(req.getParameter(PARAM_WS_ID), resp);
            Group group = getVProps(parser, req, resp);
            Map<String, String> propsData = getPanelProperties(parser, req, resp);
            
            Map<String, Object> panelData =
                s_repo.createPanel(wsId, type, group, propsData);
            
            if (panelData != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                JSONObject.writeJSONString(panelData, resp.getWriter());
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
