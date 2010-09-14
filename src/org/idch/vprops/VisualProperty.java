/* Created on       Aug 7, 2010
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

public class VisualProperty implements DTO {
    public static final String PK_ID       = "id";
    public static final String PK_TYPE     = "type";
    public static final String PK_VALUE    = "value";
    public static final String PK_ENABLED  = "enabled";
    public static final String PK_DEFAULTS = "defaults";
    
    public static Map<String, Object> getMomento(long id, String type,
            String value, Boolean enabled, Map<String, Object> config) {
        Map<String, Object> data = new HashMap<String, Object>();
        
        data.put(PK_ID,       id);
        data.put(PK_TYPE,     type);
        data.put(PK_VALUE,    value);
        data.put(PK_ENABLED,  enabled);
        data.put(PK_DEFAULTS, config);
        
        return data;
    }
    
    private long m_id = -1;
    private String m_type = null;
    private String m_value = null;
    private Boolean m_enabled = null;
    private PropertyConfig m_config = null;
    
    public VisualProperty() {
        
    }
    
    public VisualProperty(long id, String type, String value, Boolean enabled,
            PropertyConfig config) {
        m_id = id;
        m_type = type;
        m_value = value;
        m_enabled = enabled;
        m_config = config;
    }
    
    VisualProperty(String type, 
            Map<String, Object> state, Map<String, Object> config) 
    throws BadDataException {
        
        m_type = type;
        m_config = PropertyConfig.get(config);
        
        m_value = (String)state.get(PK_VALUE);
        m_enabled = (Boolean)state.get(PK_ENABLED);
    }
    
    public VisualProperty(Map<String, Object> data) throws BadDataException {
        initialize(data);
    }
    
    public void setId(long id) {
        this.m_id = id;
    }
    
    public long getId() {
        return m_id;
    }
    
    public String getType() {
        return m_type;
    }
    
    public String getValue() {
        return m_value;
    }
    
    public Boolean getEnabled() {
        return m_enabled;
    }
    
    public PropertyConfig getDefaults() {
        return m_config;
    }

    public void setValue(String value) {
        m_value = value;
    }
    
    public void enable(Boolean flag) {
        m_enabled = flag;
    }
    
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> data) throws BadDataException {
        try {
            Number id = (Number)data.get(PK_ID);
            if (id != null)
                m_id = id.longValue();
            m_type = (String)data.get(PK_TYPE);
            m_value = (String)data.get(PK_VALUE);
            m_enabled = (Boolean)data.get(PK_ENABLED);
            
            Map<String, Object> cfg = 
                (Map<String, Object>)data.get(PK_DEFAULTS);
            m_config = PropertyConfig.get(cfg);
        } catch (ClassCastException cce) {
            throw new BadDataException("Supplied data could not be cast to " +
            		"the appropriate type.", cce);
        }
    }

    public Map<String, Object> toJSON() {
        Map<String, Object> data = new HashMap<String, Object>();
        
        data.put(PK_ID, m_id);
        data.put(PK_TYPE, m_type);
        data.put(PK_VALUE, m_value);
        data.put(PK_ENABLED, m_enabled);
        data.put(PK_DEFAULTS, m_config.toJSON());
        
        return data;
    }

}
