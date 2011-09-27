/* Created on       Sep 30, 2010
 * Last Modified on $Date: $
 * $Revision: $
 *
 * Copyright (c) 2010 Institute for Digital Christian Heritage (IDCH)
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software. Except as contained in 
 * this notice, the name(s) of the above copyright holders shall not be used 
 * in advertising or otherwise to promote the sale, use or other dealings in
 * this Software without prior written authorization.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.idch.critspace.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.RepositoryAccessException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@SuppressWarnings("serial")
public class PanelProperties extends CritspaceServlet {
    
    public final static String PANEL_ID_PARAM = "id";
    public final static String PROP_NAME_PARAM = "n";
    public final static String PROP_VALUE_PARAM = "v";
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        
        String panelId       = StringUtils.trimToNull(req.getParameter("id"));
        String propertyName  = StringUtils.trimToNull(req.getParameter("name"));
        
        // handle data errors
        if (!StringUtils.isNumeric(panelId)) {
            resp.sendError(BAD_REQ, "Invalid panel id (" + panelId + "). Not a number.");
            return;
        }
        
        String response = null;
        long id = Long.parseLong(panelId);
        try {
            if (propertyName == null) {
                // list all properties
                Map<String, String> props = s_repo.listProperties(id);
                response = JSONObject.toJSONString(props);
            } else {
                String value = s_repo.getProperty(id, propertyName);
                response = JSONValue.toJSONString(value);
            }
            
        } catch (RepositoryAccessException rae) {
            resp.sendError(INTERNAL_ERROR, 
                    "Could not update panel property (" + id + ", " + propertyName + "): " + 
                    rae.getMessage());
        }
        
        if (response != null) {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/javascript");
            PrintWriter out = resp.getWriter();
            out.write(response);
            out.flush();
        }
    }
    
    public void doPut(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        
        String panelId       = StringUtils.trimToNull(req.getParameter(PANEL_ID_PARAM));
        String propertyName  = StringUtils.trimToNull(req.getParameter(PROP_NAME_PARAM));
        String propertyValue = StringUtils.trimToNull(req.getParameter(PROP_VALUE_PARAM));
//        String historEventId = StringUtils.trimToNull(req.getParameter("eId"));

        // handle data errors
        if (!StringUtils.isNumeric(panelId)) {
            resp.sendError(BAD_REQ, "Invalid panel id (" + panelId + "). Not a number.");
        } else if (propertyName == null) {
            resp.sendError(BAD_REQ, "No property name specified");
        } else {
            long id = Long.parseLong(panelId);
            try {
                if (propertyValue == null)
                    s_repo.deleteProperty(id, propertyName);
                else 
                    s_repo.setProperty(id, propertyName, propertyValue);
                
                resp.setStatus(CREATED);
            } catch (RepositoryAccessException rae) {
                resp.sendError(INTERNAL_ERROR, 
                        "Could not update panel property (" + id + ", " + propertyName + "): " + 
                        rae.getMessage());
            }
        }
    }
    

    public void doDelete(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
        
        String panelId       = StringUtils.trimToNull(req.getParameter(PANEL_ID_PARAM));
        String propertyName  = StringUtils.trimToNull(req.getParameter(PROP_NAME_PARAM));
//        String historEventId = StringUtils.trimToNull(req.getParameter("eId"));

        // handle data errors
        if (!StringUtils.isNumeric(panelId)) {
            resp.sendError(BAD_REQ, "Invalid panel id (" + panelId + "). Not a number.");
        } else if (propertyName == null) {
            resp.sendError(BAD_REQ, "No property name specified");
        } else {
            long id = Long.parseLong(panelId);
            try {
                s_repo.deleteProperty(id, propertyName);
                resp.setStatus(OK);
            } catch (RepositoryAccessException rae) {
                resp.sendError(INTERNAL_ERROR, 
                        "Could not update panel property (" + id + ", " + propertyName + "): " + 
                        rae.getMessage());
            }
        }
    }
}
