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
        js   : ["/js/IDCH/nt/CollationBaseText.js"],
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
      
      { type : "org.idch.images",
        js   : ["/js/IDCH/images/ImagePanel.js"],
        menu : "Image Panel",
        desc : "Displays an image.",
        depends : [],
        modules : ["images"]
      }];

/** Script gloabl referent to the main CritSpace environment. */
var CritSpace,
    m_workspace;

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

function initGroup1() {
    var id = 7736,
        resolver = new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    
    // TODO load appropriate base text
    
    IDCH.afed.Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            $warn("Could not retrieve fasimile: " + id);
            return;
        }  
        
        m_workspace.createPanel(IDCH.afed.FACSIMILE_VIEWER, {
            position : { y : 75, x : 50 },
            size : { width  : 800, height : 166 },
            props : { facsimileId : id },
            imageProperties : { width : 152, height  : 146 }, 
            resolver : resolver,
            format : "small" 
        });
    }); 
}

function initGroup2() {
    var id = 7736,
        resolver = new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    
    // TODO load appropriate base text
    
    IDCH.afed.Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            $warn("Could not retrieve fasimile: " + id);
            return;
        }  
        
        m_workspace.createPanel(IDCH.afed.FACSIMILE_VIEWER, {
            position : { y : 75, x : 50 },
            size : { width  : 800, height : 166 },
            props : { facsimileId : id },
            imageProperties : { width : 152, height  : 146 }, 
            resolver : resolver,
            format : "small" 
        });
    }); 
}

function initCollationBaseText() {
    var id = 7736,
        servletUrl = $P('idch.afed.facsim.image.servlet'),
        resolver = new IDCH.images.APIResolver(servletUrl);

        bt_cfg = {
            position : { y : 275, x : 50 },
            size : { width  : 500, height : 600 },
            props : { book : "Luke", chapter : 20 }
        },
        
        facs_cfg = {
            position : { y : 75, x : 50 },
            size : { width  : 800, height : 166 },
            props : { facsimileId : id },
            imageProperties : { width : 152, height  : 146 }, 
            resolver : resolver,
            format : "small" 
        };
    
    // load the facsimile viewer
    IDCH.afed.Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            $warn("Could not retrieve fasimile: " + id);
            return;
        }  
        
        m_workspace.createPanel(IDCH.afed.FACSIMILE_VIEWER, facs_cfg); 
    });
    
    // load the base text panel
    m_workspace.createPanel(IDCH.nt.BASE_TEXT_PANEL, bt_cfg, 
        function(panel) {
            panel.on("ready", function() {
                panel.setChapter("Luke", 20);
            });
        });
}

function initWorkspace(ws) {
    var size = ws.listPanels().length,
        name = ws.getName();
    
    m_workspace = ws;
    if (size == 0) {            // this is a new workspace
        if (name.startsWith("Group1/"))         initGroup1();
        else if (name.startsWith("Group2/"))    initGroup2();
        else                                    initCollationBaseText();
    }
}
function main() {
    
    try {
        CritSpace.on("workspaceloaded", function(ws) {
            ws.on("ready", function() {
                initWorkspace(ws); 
            });
        });
//        CritSpace.loadWorkspace("/test/ws2");
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
    $P('idch.afed.facsim.servlet',       "/afed/facsimile");
    $P('idch.afed.facsim.image.servlet', "/afed/image");
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.vprops.urls.types",  "/CritSpace/vprops/types");
    $P("idch.vprops.urls.groups", "/CritSpace/vprops/groups");
    $P("idch.vprops.urls.props",  "/CritSpace/vprops/properties");
    $P("idch.vprops.timeout",     5000);
    
    // load the logger
    IDCH.configLogger("logger", function() {
    	var modules = ["critspace", "afed", "images-scroller", "nt-tce", "tzivi", "tzivi-images", "tzivi-markers", "selector", "animation"];
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