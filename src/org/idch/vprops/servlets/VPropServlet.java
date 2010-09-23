/* Created on       Aug 17, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;
import org.idch.vprops.persist.PropertyRepository;

@SuppressWarnings("serial")
public class VPropServlet extends HttpServlet {
    
    private static final String LOGGER = VPropServlet.class.getName();
    
    public static final int BAD_REQ        = HttpServletResponse.SC_BAD_REQUEST;
    public static final int NOT_FOUND      = HttpServletResponse.SC_NOT_FOUND;
    public static final int INTERNAL_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    public static final int NOT_ALLOWED    = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    public static final int CONFLICT       = HttpServletResponse.SC_CONFLICT;
    
    public static final int CREATED        = HttpServletResponse.SC_CREATED;
    public static final int OK             = HttpServletResponse.SC_OK;

    protected static PropertyRepository s_repo;
    
    /**
     * 
     */
    public void init() throws ServletException {
        // TODO specify the property bundle to use
        if (s_repo != null)
            return;

        try {
            s_repo = PropertyRepository.get();
            if (!s_repo.probe()) {
                // try to create the repository, if we can't find it.
                LogService.logWarn("Property repository not yet initialized. " +
                        "Trying to create it now.", LOGGER);
                
                boolean success = s_repo.create();
                
                if (success) {
                    // install default types
                    s_repo.initTypes();
                    LogService.logInfo("Created property repository.", LOGGER);
                } else {
                    s_repo = null;
                    String msg = "Could not initialize VisualProperty database.";
                    LogService.logError(msg, LOGGER);
                    throw new ServletException(msg);
                }
            }
        } catch (RepositoryAccessException rae) {
            String errmsg = "Could not load PropertyRepository.";
            throw new ServletException(errmsg, rae);
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
