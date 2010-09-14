package org.idch.vprops.persist;
import java.util.ArrayList;
import java.util.List;

import org.idch.persist.BadDataException;
import org.idch.vprops.NumericConfig;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.TextualConfig;
import org.idch.vprops.ToggleConfig;

/* Created on       Aug 12, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *
 * ALL RIGHTS RESERVED.
 */

public class TypeUtils {

    public final static PropTypeStruct ptTop = 
        new PropTypeStruct("top", "top", "Top", "The top of this object", 
                       new NumericConfig(0, true));
    
    public final static PropTypeStruct ptLeft = 
        new PropTypeStruct("left", "left", "Left", "The left hand of this object", 
                       new NumericConfig(0, true));
    
    public final static PropTypeStruct ptWidth = 
        new PropTypeStruct("width", "width", "Width", "The width of this object", 
                       new NumericConfig(200, true));
    
    public final static PropTypeStruct ptHeight = 
        new PropTypeStruct("height", "height", "Height", "The height of this object", 
                       new NumericConfig(400, true));
    
    public final static PropTypeStruct ptFont = getFontStruct();
    
    public final static PropTypeStruct ptBold = getBoldStruct();
    
    private static final PropTypeStruct getFontStruct() {
        String[] fontArray = { "Arial", "Courier", "Calibri", "Cambria", 
                "Helvetica", "Garamond", "Georgia", "Times", "Verdana", 
                "serif", "sans-serif", "cursive", "fantasy", "monospace"};
        
        PropTypeStruct struct = null;
        try { 
            List<String> options = new ArrayList<String>(fontArray.length);
            for (String option : fontArray)
                options.add(option);
            TextualConfig fontConfig = 
                new TextualConfig(null, true, null, null, options, false); 
        
            struct = new PropTypeStruct("font", "font-family", 
                    "Font", "The font family for text.", fontConfig);
        } catch (BadDataException bde) {
        }
        
        return struct;
    }
    
    private static final PropTypeStruct getBoldStruct() {
        PropTypeStruct struct = null; 
        try {
        ToggleConfig config = 
            new ToggleConfig("normal", true, "bold", "normal");
        
        struct =  new PropTypeStruct("bold", "font-weight", "Bold", 
                "Make text bold", config);
        } catch (BadDataException bde) {
        }
        
        return struct;
    }
    
    
    public static final void loadDefaultTypes() {
        
    }
    
    public static final class PropTypeStruct {
        public String id   = "top";
        public String css  = "top";
        public String name = "Top";
        public String desc = 
            "The y coordinates of the top of this object (in pixels).";
        public PropertyConfig config = new NumericConfig(0, true);
        
        PropTypeStruct(String id, String css, String name, String desc,
                PropertyConfig config) {
            this.id = id;
            this.css = css;
            this.name = name;
            this.desc = desc;
            this.config = config;
        }
    }
}
