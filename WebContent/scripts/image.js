/* Title: image.js
 * Date: 23 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 */

(function() {
    
var panelDefns = [
        { js   : ["scripts/TextPanel.js"],
          type : "org.idch.examples.TextPanel",
          menu : "Text Panel",
          desc : "A simple panel for displaying some text.",
          depends : []
        },
        
        { js   : ["/js/IDCH/images/images-panel.js"],
          type : "org.idch.images.ImagePanel",
          menu : "Image Panel",
          desc : "Displays an image.",
          depends : []
        },
        
        { js   : ["/js/IDCH/images/images-filmstrip.js"],
          type : "org.idch.images.Filmstrip",
          menu : "Filmstrip",
          desc : "Displays multple images in a filmstrip like interface.",
          depends : ["org.idch.images.ImagePanel"],
          optional : ["org.idch.tzivi.TziviPanel"]
        }
    ];

 
function loadImage() {
    var vcfg = {  
        position : {
            top  : { defaultvalue : 300 },
            left : { defaultvalue : 25 }
        },

        size : {
            width  : { range : [50, 2000], defaultvalue : 275 },
            height : { range : [50, 2000], defaultvalue : 275 }
        } 
    }

    var resolver = new IDCH.images.Resolver();
    var url      = "/CritSpace/images/tzi/NT1/";
    
    m_workspace.createPanel(IDCH.images.IMAGE_PANEL, {
        position : { y : 300, x : 25 },
        size : { width  : 275, height : 275 },
        proxy : new IDCH.images.ImageProxy(url, resolver),
        format : "thumb"
    });
    
    // add a "note" panel too.
    var cfg = { 
        text : "Hello, ECDL 2010!",
        position : { x : 250, y : 300 },
        size : { width : 160, height : 125 } 
   };
   m_workspace.createPanel(IDCH.examples.Panels.TEXT_PANEL, cfg,
       function(panel) {
           if (panel == null)
               return;
           
           vprops = panel.getVisualProperties();
           vprops.font.size.set(2);
           vprops.font.color.set("#336699");
       });
}

function initFilmstrip() {
//    var vcfg = {  
//        position : {
//            top  : { defaultvalue : 50 },
//            left : { defaultvalue : 25 }
//        },
//
//        size : {
//            width  : { range : [50, 2000], defaultvalue : 800 },
//            height : { range : [166, 166], defaultvalue : 166 }
//        } 
//    };
    
    var resolver = 
        new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    var id = 11237;
    IDCH.afed.Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            var msg = "Could not retrieve fasimile: " + id;
            $warn(msg);
            
            return;
        }  
        
        var collations = facs.getCollations();
        var images = collations[0].getImages();
        m_workspace.createPanel(IDCH.images.FILMSTRIP_PANEL, {
            position : { y : 50, x : 25 },
            size : { width  : 800, height : 166 },
            
            images : images,
            imageProperties : { width : 152, height  : 146 }, 
            resolver : resolver,
            format : "small"
        });
    }); 
}

function main() {
    // load our text panel TODO we need to move this to a configuration file
    initFilmstrip();
    loadImage();
}

function config() {
    return;
    var CritspaceRepository = IDCH.critspace.Repository,
        VPropRepository = IDCH.vprops.BasicRepository,
        PanelRegistry = IDCH.critspace.PanelRegistry;
    
    var vpropRepo = new VPropRepository(
            $P("idch.vprops.urls.types"),
            $P("idch.vprops.urls.groups"),
            $P("idch.vprops.urls.props"),
            $P("idch.vprops.timeout"));
    
    
    // first step in processing chain: The repository is ready, load the 
    // initial set of panels.
    function onRepoReady() {
        PanelRegistry.loadPanels(panelDefns, {
            success : onPanelsReady,
            failure : function() {
                alert("failed to load panels");
            }
        });
    }
    
    // second step in processing chain: The core panel types have been loaded,
    // retrive the workspace
    function onPanelsReady() {
        var name = "test/ws2"
        m_repo.loadWorkspace (name, {
            success : function(ws) {
                m_workspace = ws;
                onWorkspaceReady();
            },

            failure : function(msg) {
                alert(msg);
            }
        });
    }
    
    // final step in processing chain: The workspace has been loaded, invoke 
    // the main function
    function onWorkspaceReady() {
        main();
    }
    
    // START THE INITIALIZATION CHAIN
    m_repo = new CritspaceRepository(vpropRepo);
    m_repo.on("ready", onRepoReady);
}

function loadIDCH() {
    IDCH.load(["critspace", "critspace-repo", "critspace-chrome", "images-filmstrip", "tzivi-panel", "afed"], true, config, 
            function() { 
                alert("Failed to load IDCH modules."); 
            });
}

function configureCSDLModules() {
//    CSDL.register("controls", [], [], ["tdd", "cp", "cs", "spin", "push", "slider", "bdlg"]);
    var js = [
//              "utils/Map.js",
//              "utils/KeyCodes.js", 
//              "utils/Logger.js",  
//              "widgets/visual/VisualProperty.js", 
//              "widgets/visual/StandardProperties.js",

//              "widgets/controls/TextDropDown.js", 
//              "widgets/controls/ColorPicker.js", 
//              "widgets/controls/ColorSwatch.js", 
//              "widgets/controls/SpinControl.js", 
//              "widgets/controls/PushButton.js", 
//              "widgets/controls/Slider.js",
//              "widgets/controls/BorderDialog.js"
              ];
    
    for (var i = 0; i < js.length; i++) {
        js[i] = "/CritSpace/scripts/csdl/" + js[i];
    }
    
    YAHOO.util.Get.script(js, {
        onSuccess : loadIDCH
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
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.vprops.urls.types",  "/CritSpace/vprops/types");
    $P("idch.vprops.urls.groups", "/CritSpace/vprops/groups");
    $P("idch.vprops.urls.props",  "/CritSpace/vprops/properties");
    $P("idch.vprops.timeout",     5000);
    
    // load the logger
    IDCH.configLogger("logger", function() {
    	loadIDCH();
    });
});


})();