/* Created on       Sep 23, 2010
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
package org.idch.vprops.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.VisualProperty;

@SuppressWarnings("serial")
public class PropertyServlet extends VPropServlet {
    public static final String PROP_ID_PARAM = "id";
    public static final String VALUE_PARAM = "v";
    public static final String ENABLED_PARAM = "enabled";
    
    private VisualProperty getProperty(String idParam, HttpServletResponse resp) 
    throws IOException {
        String errmsg = "Could not retrieve Group: ";
        
        VisualProperty prop = null;
        try {
            idParam = StringUtils.trimToNull(idParam);
            if ((idParam != null) && StringUtils.isNumeric(idParam)) {
                prop = s_repo.findVisualProperty(Long.parseLong(idParam));
            } else {
                errmsg += "invalid property id (" + idParam + ").";
                resp.sendError(BAD_REQ, warn(errmsg, null));    
            }
                
        } catch (RepositoryAccessException rae) {
            errmsg += "error trying to retrieve data.";
            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
        
        return prop;
    }
    
    /**
     * 
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter(PROP_ID_PARAM);
        VisualProperty prop = getProperty(id, resp);
        
        // set the value
        String value = req.getParameter(VALUE_PARAM);
        if (value != null) {
            value = StringUtils.trimToNull(value);
            prop.setValue(value);
        }
        
        // set the enabled/disabled value
        String enabled = req.getParameter(ENABLED_PARAM);
        if (enabled == null) {
            // no action
        } else if (enabled.equalsIgnoreCase("true")) {
            prop.enable(true);
        } else if (enabled.equalsIgnoreCase("false")) {
            prop.enable(false);
        }
        		
        try {
            s_repo.updataVisualProperty(prop);
            resp.setStatus(CREATED);
        } catch (RepositoryAccessException rae) {
            resp.sendError(INTERNAL_ERROR, "Could not update database: " + rae);
        }
        
    }
}
