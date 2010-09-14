/* Created on       Aug 28, 2010
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
package org.idch.critspace;

import java.util.HashMap;
import java.util.Map;

import org.idch.persist.BadDataException;
import org.idch.persist.DTO;

public class Panel implements DTO {

    public static final String PK_ID         = "id";
    public static final String PK_WS_ID      = "ws";
    public static final String PK_GROUP      = "grp";
    public static final String PK_TYPE       = "type";
    public static final String PK_PROPERTIES = "props";
    public static final String PK_VPROPS     = "vprops";
    
    public static final String PK_PROP_KEY   = "key";
    public static final String PK_PROP_VALUE = "value";
    
    private long id;
    private long wsId;
    private long groupId;
    private String type;
    
    /**
     * @param data
     * @throws BadDataException
     */
    public void initialize(Map<String, Object> data) throws BadDataException {
        // TODO Auto-generated method stub
        
    }
    public Map<String, Object> toJSON() {
        HashMap<String, Object> data = new HashMap<String, Object>(4);
        data.put(Panel.PK_ID, id);
        data.put(Panel.PK_WS_ID, wsId);
        data.put(Panel.PK_GROUP, groupId);
        data.put(Panel.PK_TYPE, type);
        
        return data;
    }
}
