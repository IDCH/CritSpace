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

import java.util.Map;

import org.idch.persist.BadDataException;

public final class ToggleConfig extends PropertyConfig {
    
    public static final String PK_ON = "on";
    public static final String PK_OFF = "off";
  
    //========================================================================
    // STATIC METHODS
    //========================================================================
    
    /**
     * 
     * @param value
     * @param enabled
     * @param on
     * @param off
     * @return
     */
    public static Map<String, Object> getMomento(
            String value, Boolean enabled, String on, String off) {
        
        Map<String, Object> data = 
            PropertyConfig.getMomento(value, enabled, PropertyType.TOGGLE);

        if (on != null)    data.put(PK_ON, on);
        if (off != null)   data.put(PK_OFF, off);
        
        return data;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    String m_on;
    String m_off;
    
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    public ToggleConfig() {
        
    }
    
    public ToggleConfig(String value, boolean enabled, String on, String off) 
    throws BadDataException {
        super(value, enabled);
        
        m_on = on;
        m_off = off;
        
        checkValue();
    }
    
    public ToggleConfig(Map<String, Object> data) throws BadDataException {
        initialize(data);
    }
    
    private void checkValue() throws BadDataException {
        String value = this.getDefaultValue();
        if (!value.equals(m_on) && !value.equals(m_off)) {
            throw new BadDataException("Bad value for this config. Must be " +
            		"either '" + m_on + "' or '" + m_off + "'");
        }
    }
    
    //========================================================================
    // INSTANCE METHODS 
    //========================================================================
    
    public String getFormat() {
        return PropertyType.TOGGLE;
    }
    
    public boolean isSet() { 
        return this.m_defaultValue.equals(m_on);
    }
    
    public void set() { 
        this.m_defaultValue = m_on;
    }
    
    public void unset() {
        this.m_defaultValue = m_off;
    }
    
    public void toggle() {
        m_defaultValue = this.isSet() ? m_off : m_on; 
    }

    public String getOnValue() {
        return m_on;
    }
    
    public String getOffValue() {
        return m_off;
    }
    
    public void initialize(Map<String, Object> data) throws BadDataException {
        super.initialize(data);
        
        try {
            m_on = (String)data.get(PK_ON);
            m_off = (String)data.get(PK_OFF);
        } catch (ClassCastException cce) {
            throw new BadDataException("Could not cast data values to the " +
                    "required data type.", cce);
        }
        
        checkValue();
    }

    public Map<String, Object> toJSON() {
        return getMomento(m_defaultValue, m_enabled, m_on, m_off);
    }

    public boolean equals(Object obj) {
        boolean eq = false;
        if ((obj instanceof ToggleConfig) && super.equals(obj)) {
            ToggleConfig conf = (ToggleConfig)obj;
            String on = conf.getOnValue();
            String off = conf.getOffValue();
            
            eq = ((m_on == null)  ? on == null  : m_on.equalsIgnoreCase(on)) &&
                 ((m_off == null) ? off == null : m_off.equalsIgnoreCase(off));
        }
        
        return eq;
    }
}
