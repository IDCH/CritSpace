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

public abstract class PropertyConfig implements DTO {
    public static final String PK_VALUE   = "value";
    public static final String PK_ENABLED = "enabled";
    public static final String PK_FORMAT  = "format";
    
    public static Map<String, Object> getMomento(
            String value, Boolean enabled, String format) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(PK_VALUE, value);
        data.put(PK_ENABLED, enabled);
        data.put(PK_FORMAT, format);
        
        return data;
    }
    
    public static PropertyConfig get(Map<String, Object> data) 
        throws BadDataException {
        
        PropertyConfig cfg = null;
        String format = (String)data.get(PK_FORMAT);
        if (format == null) {
            throw new BadDataException("Invalid data map. Expected to find " +
            		"a value for '" + PK_FORMAT + "'");
        }
        
        format = format.toLowerCase();
        if (format.equals(PropertyType.NUMERIC))
            cfg = new NumericConfig(data);
        else if (format.equals(PropertyType.TEXTUAL))
            cfg = new TextualConfig(data);
        else if (format.equals(PropertyType.TOGGLE))
            cfg = new ToggleConfig(data);
        else
            throw new BadDataException("Invalid configuration format: " + 
                    format);
        
        return cfg;
    }
    
    String m_defaultValue = null;
    Boolean m_enabled = null;
    
    protected PropertyConfig() {
        
    }
    
    protected PropertyConfig(String value, Boolean enabled) {
        m_defaultValue = value;
        m_enabled = enabled;
    }
    
    public abstract String getFormat();
    
    public String getDefaultValue() {
        return m_defaultValue;
    }
    
    public boolean isEnabled() {
        return (m_enabled == null) ? true : m_enabled;
    }
    
    public void setDefaultValue(String value) {
        m_defaultValue = value;
    }
    
    public void enable() { 
        m_enabled = true;
    }
    
    public void disable() {
        m_enabled = false;
    }
    
    public void extend(PropertyConfig config) {
        if (config.m_enabled != null) {
            m_enabled = config.m_enabled;
        }
        
        if (config.m_defaultValue != null) {
            m_defaultValue = config.m_defaultValue;
        }
    }
    
    public void initialize(Map<String, Object> data)  
    throws BadDataException {
        try {
            m_defaultValue = (String)data.get("value");
            m_enabled = (Boolean)data.get("enabled");
        } catch (ClassCastException cce) {
            throw new BadDataException("Could not cast data values to the " +
            		"required data type.", cce);
        }
    }

    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put(PK_VALUE, m_defaultValue);
        json.put(PK_ENABLED, m_enabled);
        json.put(PK_FORMAT, this.getFormat());
        
        return json;
    }

    public boolean equals(Object obj) {
        boolean eq = false;
        if (obj instanceof PropertyConfig) {
            PropertyConfig conf = (PropertyConfig)obj;
            
            String objValue = conf.getDefaultValue();
            if (m_defaultValue == null) {
                eq = objValue == null;
            } else {
                eq = m_defaultValue.equals(objValue);
            }
            
            eq = eq && (m_enabled == conf.isEnabled());
        }
        
        return eq;
    }
}
