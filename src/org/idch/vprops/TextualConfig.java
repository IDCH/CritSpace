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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.idch.persist.BadDataException;

public final class TextualConfig extends PropertyConfig {
    
    // property keys
    public static final String PK_OPTIONS   = "options";
    public static final String PK_RESTRICT  = "restrict";
    public static final String PK_REGEX_PAT = "regex";
    public static final String PK_REGEX_MOD = "regext";
  
    //========================================================================
    // STATIC METHODS
    //========================================================================
    
    /**
     * 
     * @param value
     * @param enabled
     * @param pattern
     * @param mod
     * @param options
     * @param restrict
     * @return
     */
    public static Map<String, Object> getMomento(
            String value, Boolean enabled, String pattern, String mod, 
            List<String> options, Boolean restrict) {
        
        Map<String, Object> data = 
            PropertyConfig.getMomento(value, enabled, PropertyType.TEXTUAL);

        if (pattern != null)    data.put(PK_REGEX_PAT, pattern);
        if (mod != null)        data.put(PK_REGEX_MOD, mod);
        if (options != null)    data.put(PK_OPTIONS, options);
        else                    data.put(PK_OPTIONS, new ArrayList<String>());
        
        data.put(TextualConfig.PK_RESTRICT, restrict);
        
        return data;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    
    private String m_regexPattern  = null;
    private String m_regexModifier = null;
    private List<String> m_options = null;
    private Boolean m_restrict     = false;

    //========================================================================
    // CONSTRUCTORS 
    //========================================================================
    
    public TextualConfig() {
        
    }
    
    public TextualConfig(String value, boolean enabled) {
        super(value, enabled);
    }
    
    public TextualConfig(String value, Boolean enabled, 
            String pattern, String mod, 
            List<String> options, Boolean restrict) throws BadDataException {
        
        super(value, enabled);
        
        initializePattern(pattern, mod);
        m_options = options;
        m_restrict = restrict;
    }
    
    public TextualConfig(Map<String, Object> data) throws BadDataException {
        initialize(data);
    }

    //========================================================================
    // INSTANCE METHODS 
    //========================================================================
    
    /**
     * 
     * @param pattern
     * @param mod
     * @throws BadDataException
     */
    private void initializePattern(String pattern, String mod)
            throws BadDataException {
        if (pattern == null) 
            return;         // nothing to do
        
        try {
            Pattern.compile(pattern);
            m_regexPattern = pattern;
        } catch (PatternSyntaxException pse) {
            throw new BadDataException(
                    "Could not parse regular expression", pse);
        }
        
        if (mod == null) {
            m_regexModifier = null;
        } else if (mod.equals("i") || mod.equals("g")) {
            m_regexModifier = mod;
        } else {
            throw new BadDataException("Invalid modifier '" + mod + 
                    "'. Must be either 'i' or 'g'."); 
        }
    }
    
    public String getFormat() {
        return PropertyType.TEXTUAL;
    }
    
    public String getRegexPattern() {
        return this.m_regexPattern;
    }
    
    public String getRegexModifier() {
        return this.m_regexModifier;
    }
    
    public boolean areOptionsRequired() {
        return (m_restrict == null) ? false : m_restrict;
    }

    public List<String> getOptions() {
        return m_options;
    }
    
    public void extend(TextualConfig config) {
        super.extend(config);
        
        if (config.m_regexPattern == null) 
            m_regexPattern = config.m_regexPattern;
        
        if (config.m_regexModifier == null) 
            m_regexModifier = config.m_regexModifier;
        
        if (config.m_options == null) 
            m_options = config.m_options;
        
        if (config.m_restrict == null) 
            m_restrict = config.m_restrict;
    }
    
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> data) throws BadDataException {
        super.initialize(data);
        
        try {
            initializePattern((String)data.get(PK_REGEX_PAT),
                              (String)data.get(PK_REGEX_MOD));
            
            m_options = (List<String>)data.get(PK_OPTIONS);
            
            Boolean restrict = (Boolean)data.get(PK_RESTRICT);
            m_restrict = (restrict != null) ? restrict : false; 
        } catch (ClassCastException cce) {
            throw new BadDataException("Could not cast data values to the " +
                    "required data type.", cce);
        }
    }

    public Map<String, Object> toJSON() {
        return getMomento(m_defaultValue, m_enabled, 
                m_regexPattern, m_regexModifier, m_options, m_restrict);
    }
    
    public boolean equals(Object obj) {
        boolean regexEq = false;
        boolean restrictEq = false;
        boolean optionsEq  = false;
        
        if ((obj instanceof TextualConfig) && super.equals(obj)) {
            TextualConfig conf = (TextualConfig)obj;
            String pat = conf.m_regexPattern;
            String mod = conf.m_regexModifier;
            
            regexEq = ((m_regexPattern == null)  ? pat == null  :
                              m_regexPattern.equalsIgnoreCase(pat)) 
                   && ((m_regexModifier == null) ? mod == null : 
                              m_regexModifier.equalsIgnoreCase(mod));
            
            restrictEq = (m_restrict == conf.m_restrict);
            
            // XXX This is a bit overly strict. In theory this should work 
            //     regardless of the order of the options list. 
            List<String> options = conf.getOptions();
            optionsEq = (m_options == null) ? ((options == null) || (options.size() == 0)) :
                m_options.equals(options);
        }
            
        return regexEq && restrictEq && optionsEq;
    }
}
