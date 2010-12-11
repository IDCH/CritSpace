/* Created on       Dec 10, 2010
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
package org.idch.nt.mss.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.idch.nt.ScriptureReference;
import org.idch.nt.mss.VsMapping;
import org.idch.persist.BadDataException;
import org.idch.persist.RepositoryAccessException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class VerseMappingServlet extends MssServlet {
    public static final String PROP_ID   = "id";
    public static final String PROP_FID  = "fId";
    public static final String PROP_IMG  = "img";
    public static final String PROP_REF  = "ref";
    public static final String PROP_X    = "x";
    public static final String PROP_Y    = "y";

    private void getByPageImage(String fId, String imgId, 
            HttpServletResponse resp) 
    throws IOException {
        long facsimId = Long.parseLong(fId);
        long imageId  = Long.parseLong(imgId);
        
        try {
            List<VsMapping> mappings = 
                s_repo.getVerseMappings(facsimId, imageId);
            
            String response = JSONArray.toJSONString(mappings);
            if (response != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                PrintWriter out = resp.getWriter();
                out.write(response);
                out.flush();
            }
        } catch (RepositoryAccessException rae) {
//          errmsg += "error trying to save data.";
//          resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
      }
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        
        String fId   = StringUtils.trimToEmpty(req.getParameter(PROP_FID));
        String imgId = StringUtils.trimToEmpty(req.getParameter(PROP_IMG));
        
        if (StringUtils.isNumeric(fId) && StringUtils.isNumeric(imgId)) {
            getByPageImage(fId, imgId, resp);
        } else {
            resp.sendError(BAD_REQ, "No mappings to retrieve");
        }
        
    }
    
    private void updatePosition(HttpServletResponse resp, long id,
            String strX, String strY) throws IOException {
        int x = Integer.parseInt(strX);
        int y = Integer.parseInt(strY);
        
        try {
            if (s_repo.updateVersePosition(id, x, y)) 
                resp.setStatus(OK);
            else 
                resp.sendError(INTERNAL_ERROR, "Could not update the " +
                		"position of this verse mapping.");
        } catch (RepositoryAccessException rae) {
            resp.sendError(INTERNAL_ERROR, "Could not update the position " +
                "of this verse mapping. Failed to update database: " + rae);
      }
    }
    
    private void updateReference(HttpServletResponse resp, 
            long id, String refJSON) throws IOException {
        JSONParser parser = new JSONParser();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = 
                (Map<String, Object>)parser.parse(refJSON);
            
            ScriptureReference ref = new ScriptureReference(data);
            if (s_repo.updateVerse(id, ref)) {
                resp.setStatus(OK);
            } else { 
                resp.sendError(INTERNAL_ERROR, "Could not update the " +
                        "scripture reference of this verse mapping.");
            }
        } catch (ParseException pe) {
            resp.sendError(BAD_REQ, "Could not update the " +
                    "scripture reference of this verse mapping. Could " +
                    "not parse supplied data: " + refJSON);
        } catch (BadDataException bde) {
            resp.sendError(BAD_REQ, "Could not update the " +
                    "scripture reference of this verse mapping. Invalid " +
                    "data supplied: " + refJSON);
        } catch (RepositoryAccessException rae) {
            resp.sendError(INTERNAL_ERROR, "Could not update the " +
                    "scripture reference of this verse mapping. Failed to" +
                    "update database: " + rae);
        }
    }
    
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
    throws IOException {
        long id  = getId(req.getParameter(PROP_ID), resp);
        if (id <= 0) {
            resp.sendError(BAD_REQ, "Cannot update mapping: no ID provided.");
            return;
        }
        
        String x = StringUtils.trimToEmpty(req.getParameter(PROP_X));
        String y = StringUtils.trimToEmpty(req.getParameter(PROP_Y));
        
        String ref = StringUtils.trimToNull(req.getParameter(PROP_REF));
        
        if (StringUtils.isNumeric(x) && StringUtils.isNumeric(y)) {
            updatePosition(resp, id, x, y);
        } else if (ref != null) {
            updateReference(resp, id, ref);
        } else {
            resp.sendError(BAD_REQ, 
                    "Cannot update mapping: no data to update.");
        }
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        
        String strMapping = req.getParameter("mapping");
        JSONParser parser = new JSONParser();
        try {
            Map<String, Object> data = 
                (Map<String, Object>)parser.parse(strMapping);
            long facsimId = (Long)data.get(VsMapping.PK_F_ID);
            long imageId  = (Long)data.get(VsMapping.PK_IMG);
            int  x        = (Integer)data.get(VsMapping.PK_X);
            int  y        = (Integer)data.get(VsMapping.PK_Y);
            
            ScriptureReference ref = new ScriptureReference(
                    (Map<String, Object>)data.get(VsMapping.PK_REF));
            
            VsMapping mapping = 
                s_repo.createVerseMapping(facsimId, imageId, x, y, ref);
            
            String response = JSONObject.toJSONString(mapping.toJSON());
            if (response != null) {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/javascript");
                PrintWriter out = resp.getWriter();
                out.write(response);
                out.flush();
            }
            
        } catch (ParseException pe) {
//            errmsg += "could not parse supplied data. ";
//            resp.sendError(BAD_REQ, warn(errmsg, pe));
        } catch (BadDataException bde) {
        }catch (RepositoryAccessException rae) {
//            errmsg += "error trying to save data.";
//            resp.sendError(INTERNAL_ERROR, error(errmsg, rae));
        }
    }
}
