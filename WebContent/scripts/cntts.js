/* Title: image.js
 * Date: 23 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 */

(function() {
    
var panelDefns = [
                  
      { type : "org.idch.images.Filmstrip",
        js   : ["/js/IDCH/images/FilmstripPanel.js"],
        desc : "A filmstrip viewer for displaying a list of images.",
        depends : [],
        modules : ["images", "images-scroller"]
      },
        
      { type : "org.idch.afed.BaseFacsimilePanel",
        js   : ["/js/IDCH/afed/AbstractFacsimilePanel.js"],
        depends : [],
        modules : ["afed"]
      },
      
      { type : "org.idch.tzivi.TziviPanel",
        js   : ["/js/IDCH/tzivi/TziviPanel.js"],
        depends : [],
        modules : ["tzivi"]
      },
          
      { type : "org.idch.nt.BaseText",
        js   : ["scripts/panels/CollationBaseText.js"],
        menu : "Base Text",
        desc : "Displays the the base text for a facsimile and supplies tools " +
        	   "to support the collation of textual variants.",
        depends : ["org.idch.afed.FacsimileViewer", 
                   "org.idch.afed.PageDisplayPanel"],
        modules : ["afed", "selector", "animation"]
      },
      
      { type : "org.idch.afed.FacsimileViewer",
        js   : ["/js/IDCH/afed/FacsimileViewer.js"],
        menu : "Facsimile Viewer",
        desc : "A filmstrip viewer for working with digital facsimiles.",
        depends : ["org.idch.afed.PageDisplayPanel", 
                   "org.idch.images.Filmstrip"],
        modules : ["afed"]
      },
      
      { type : "org.idch.afed.PageDisplayPanel",
        js   : ["/js/IDCH/afed/PageDisplayPanel.js"],
        depends : ["org.idch.afed.BaseFacsimilePanel", 
                   "org.idch.images.ImagePanel"],
        modules : ["afed"]
      },
      
//      { type : "org.idch.afed.PageDisplayPanel",
//        js   : ["scripts/panels/PageDisplayPanel.js"],
//        menu : "Page Display",
//        desc : "Displays a page from a digital facsimile. This is linked to " +
//        		"the underlying facsimile so it can be used to navigate to the " +
//        		"next and previous pages.",
//        depends : [],
//        modules : ["afed", "tzivi"]
//      },
      
      { type : "org.idch.images",
        js   : ["/js/IDCH/images/ImagePanel.js"],
        menu : "Image Panel",
        desc : "Displays an image.",
        depends : [],
        modules : ["images"]
      }];

/** Script gloabl referent to the main CritSpace environment. */
var CritSpace;

/**
 * 
 */
function initFacsimileViewer() {
    var resolver = 
        new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    // var id = 11237; 7406; 8969;
    var id = 7406;
    IDCH.afed.Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            var msg = "Could not retrieve fasimile: " + id;
            $warn(msg);
            
            return;
        }  
        
        var collations = facs.getCollations();
        var images = collations[0].getImages();
        m_workspace.createPanel(IDCH.afed.FACSIMILE_VIEWER, {
            position : { y : 50, x : 25 },
            size : { width  : 800, height : 166 },
            
            facsimile : facs,
            imageProperties : { width : 152, height  : 146 }, 
            resolver : resolver,
            format : "small"
        });
    }); 
}


function main() {
    try {
        CritSpace.loadWorkspace("/test/ws2");
//        initFacsimileViewer();
    } catch (ex) {
        alert(ex);
    }
}

function config() {
    CritSpace = IDCH.critspace.CritSpace;
    var	config = {
	    	panels : panelDefns,
	    	vpropsRepository : new IDCH.vprops.BasicRepository()
	    };
    
    CritSpace.init(config,  {
	    	success : main,
	    	failure : function(msg) {
	    		alert("Failed to initialize CritSpace: " + msg);
	    	}
	    });
}

// configure source code dependencies
YAHOO.util.Event.addListener(window, "load", function() {
    // Tell the IDCH loader where to find the base YUI and IDCH scripts
    $P('yui.base.url',               "/js/yui/yui_2.8.1/");
    $P('idch.scripts.url',           "/js/IDCH/");
    
    $P('idch.tzivi.sliderthumb',         "/CritSpace/assets/thumb-v.png");
    $P('idch.afed.facsim.servlet',       "/AFED/facsimile");
    $P('idch.afed.facsim.image.servlet', "/AFED/image");
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.vprops.urls.types",  "/CritSpace/vprops/types");
    $P("idch.vprops.urls.groups", "/CritSpace/vprops/groups");
    $P("idch.vprops.urls.props",  "/CritSpace/vprops/properties");
    $P("idch.vprops.timeout",     5000);
    
    // load the logger
    IDCH.configLogger("logger", function() {
    	var modules = ["critspace", "afed", "images-scroller", "tzivi", "tzivi-images", "selector", "animation"];
    	try {
    	    IDCH.load(modules, true, config, function() {
    	        alert("Failed to load IDCH modules."); 
    	    }); 
    	} catch (ex) {
    	    $warn(ex.toString);
    	}
    });
});


})();