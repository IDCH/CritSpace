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
package org.idch.vprops;

import java.util.HashMap;
import java.util.Map;

import org.idch.persist.BadDataException;
import org.idch.persist.DTO;

public class PropertyType implements DTO {
    //=========================================================================
    // SYMBOLIC CONSTANTS
    //=========================================================================
    
    public static final String PK_ID   = "id";
    public static final String PK_CSS  = "css";
    public static final String PK_NAME = "name";
    public static final String PK_DESC = "desc";
    public static final String PK_FMT  = "format";
    public static final String PK_CFG  = "cfg";
    
    public static final String TEXTUAL = "txt";
    public static final String NUMERIC = "num";
    public static final String TOGGLE  = "tog";
    
    //=========================================================================
    // MEMBER VARIABLES
    //=========================================================================
    private String m_id             = null;         // immutable
    private String m_css            = null;         // immutable
    private String m_name           = null;         // transient
    private String m_description    = null;         // transient
//    private String m_format         = null;       // immutable
    
    private PropertyConfig m_config = null;         // transient
    

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    public PropertyType() { 
        
    }
    
    public PropertyType(String id, String css, String name, String desc, 
            PropertyConfig config) {
        m_id = id;
        m_css = css;
        m_name = name;
        m_description = desc;
        
        m_config = config;
    }
    
    public PropertyType(Map<String, Object> data) throws BadDataException {
        initialize(data);
    }

    //=========================================================================
    // ACCESSORS & MUTATORS
    //=========================================================================
    public String getId() {
        return m_id;
    }
    
    public String getCSS() {
        return m_css;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getDescription() {
        return m_description;
    }
    
    public String getFormat() {
        return m_config.getFormat();
    }
    
    public PropertyConfig getConfig() {
        return m_config;
    }
    
    public void setName(String name) {
        m_name = name;
    }
    
    public void setDescription(String description) {
        m_description = description;
    }
    
    public void setDefualtConfiguartion(PropertyConfig config) {
        m_config = config;
    }
    
    //=========================================================================
    // MOMENTO METHODS
    //=========================================================================
    
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> data) throws BadDataException {
        m_id          = (String)data.get(PK_ID);
        m_css         = (String)data.get(PK_CSS);
        m_name        = (String)data.get(PK_NAME);
        m_description = (String)data.get(PK_DESC);
        
        Map<String, Object> cfg = (Map<String, Object>)data.get(PK_CFG);
        m_config = PropertyConfig.get(cfg);
        
        String fmt = (String)data.get(PK_FMT);
        assert fmt == m_config.getFormat();
    }
    
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<String, Object>();
        
        json.put("id", m_id);
        json.put("css", m_css);
        json.put("name", m_name);
        json.put("desc", m_description);
        json.put("type", m_config.getFormat());
        json.put("cfg", m_config.toJSON());
        return json;
    }
}
