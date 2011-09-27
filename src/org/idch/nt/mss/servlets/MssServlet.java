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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.idch.nt.mss.persist.MSSRepository;
import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;

@SuppressWarnings("serial")
public class MssServlet extends HttpServlet {
        
    private static final String LOGGER = MssServlet.class.getName();
    
    public static final int BAD_REQ        = HttpServletResponse.SC_BAD_REQUEST;
    public static final int NOT_FOUND      = HttpServletResponse.SC_NOT_FOUND;
    public static final int INTERNAL_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    public static final int NOT_ALLOWED    = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    public static final int CONFLICT       = HttpServletResponse.SC_CONFLICT;
    
    public static final int CREATED        = HttpServletResponse.SC_CREATED;
    public static final int OK             = HttpServletResponse.SC_OK;

    protected static MSSRepository s_repo;
    
    /**
     * 
     */
    public void init() throws ServletException {
        if (s_repo != null)
            return;

        try {
            s_repo = MSSRepository.get();
            if (!s_repo.probe()) {
                // try to create the repository, if we can't find it.
                LogService.logWarn("Manuscript repository not yet initialized. " +
                        "Trying to create it now.", LOGGER);
                
                if (!s_repo.create()) {
                    s_repo = null;
                    String msg = "Could not initialize manuscript repository database.";
                    LogService.logError(msg, LOGGER);
                    throw new ServletException(msg);
                }
            }
        } catch (RepositoryAccessException rae) {
            String errmsg = "Could not load MSSRepository.";
            throw new ServletException(errmsg, rae);
        } 
    }
    
    protected long getId(String strId, HttpServletResponse resp)
            throws IOException {
        try { 
            return Long.parseLong(strId);
        } catch (NumberFormatException nfe) {
            String errmsg = "Invalid id supplied (" + strId + ").";
            resp.sendError(BAD_REQ, warn(errmsg, nfe));
            throw nfe;
        }
    }
    
    protected String error(String msg, Throwable e) {
        if (e == null) {
            LogService.logError(msg, LOGGER);
        } else {
            LogService.logError(msg, LOGGER, e);
            msg += " Message: " + e.getMessage();
        }
        
        return msg;
    }
    
    protected String warn(String msg, Throwable e) {
        if (e == null) {
            LogService.logWarn(msg, LOGGER);
        } else {
            LogService.logWarn(msg, LOGGER, e);
            msg += " Message: " + e.getMessage();
        }
        
        return msg;
    }
    
   
}
