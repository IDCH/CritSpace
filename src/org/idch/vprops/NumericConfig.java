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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.BadDataException;

public final class NumericConfig extends PropertyConfig {

    public static final String PK_UNITS = "units";
    public static final String PK_RANGE = "range";
    
    //========================================================================
    // STATIC METHODS
    //========================================================================
    
    /**
     * 
     * @param value
     * @param enabled
     * @param units
     * @param min
     * @param max
     * @return
     */
    public static Map<String, Object> getMomento(String value, Boolean enabled, 
            String units, Double min, Double max) {
        
        Map<String, Object> data = 
            PropertyConfig.getMomento(value, enabled, PropertyType.NUMERIC);

        data.put(PK_UNITS, units);
        
        min = Double.isNaN(min) ? null : min;
        max = Double.isNaN(max) ? null : max;
        if ((min != null) || (max != null)) {
            List<Double> range = new ArrayList<Double>();
            range.add(min);
            range.add(max);
            
            data.put(PK_RANGE, range);
        }
        
        return data;
    }
    
    /**
     * 
     * @param value
     * @return
     */
    private static final double makeNumber(Object value) {
        double result = Double.NaN;
        
        if (value instanceof String) {
            try {
                result = Double.parseDouble((String)value);
            } catch (NumberFormatException nfe) {
                // Do nothing. Leave as NaN 
            }
        } else if (value instanceof Long)
            result = ((Long)value).doubleValue();
        else if (value instanceof Integer)
            result = ((Integer)value).doubleValue();
        else if (value instanceof Float)
            result = ((Float)value).doubleValue();
        else if (value instanceof Double)
            result = (Double)value;
        
        return result;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    String  m_units = null;
    List<Double> m_range   = null;
    
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    public NumericConfig(double value, boolean enabled) {
        super(value + "", enabled);
        
    }
    
    public NumericConfig(Map<String, Object> data) throws BadDataException {
        if (data == null) 
            data = new HashMap<String, Object>();
        initialize(data);
    }
    
    public NumericConfig(double value, boolean enabled, String units, 
            double min, double max) {
        super(value + "", enabled);
        
        m_units = StringUtils.trimToNull(units);
        if (!Double.isNaN(min) || !Double.isNaN(max)) {
            m_range = new ArrayList<Double>(2);
            m_range.add(0, min);
            m_range.add(1, max);
        }
    }
    
    //========================================================================
    // INSTANCE METHODS
    //========================================================================
    
    public String getFormat() {
        return PropertyType.NUMERIC;
    }
    
    public String getUnits() { 
        return m_units;
    }
    
    public List<Double> getRange() {
        return m_range;
    }
    
    public double getMinRange() {
        return (m_range == null) ? Double.NaN : m_range.get(0);
    }
    
    public double getMaxRange() {
        return (m_range == null) ? Double.NaN : m_range.get(1);
    }
    
    @SuppressWarnings("rawtypes")
    public void initialize(Map<String, Object> data) throws BadDataException {
        super.initialize(data);
        
        try {
            m_units = (String)data.get("units");
            List range = (List)data.get("range");
            if (range != null && range.size() > 0) {
                m_range = new ArrayList<Double>(2);
                m_range.add(makeNumber(range.get(0)));
                m_range.add(makeNumber(range.get(1)));
            }
        } catch (ClassCastException cce) {
            throw new BadDataException("Could not cast data values to the " +
                    "required data type.", cce);
        }
    }

    public Map<String, Object> toJSON() {
        return getMomento(m_defaultValue, m_enabled, 
                m_units, getMinRange(), getMaxRange());
    }
    
    public boolean equals(Object obj) {
        boolean eqUnits = false; 
        boolean eqRange = false;
        
        if (obj instanceof NumericConfig && super.equals(obj)) {
            NumericConfig conf = (NumericConfig)obj;
            String units = conf.getUnits();
            eqUnits = (m_units == null) ? units == null :
                                     m_units.equalsIgnoreCase(units);
            
            List<Double> range = conf.getRange();
            eqRange = (m_range != null) ? m_range.equals(range) :
                ((range == null) || (range.size() == 0)); 
        }
        
        return eqUnits && eqRange;
    }

}
