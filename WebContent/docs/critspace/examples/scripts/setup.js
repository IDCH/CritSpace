/* Title: setup.js
 * Date: 23 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 * This script demonstrates a basic configuration of a CritSpace environment.
 */


(function() {
    
var panelDefns = [
    { js   : ["scripts/TextPanel.js"],
      type : "org.idch.examples.TextPanel",
      menu : "Text Panel",
      desc : "A simple panel for displaying some text",
      depends : []
    }
];

var m_repo;
var m_workspace;

function main() {
    var cfg = { 
         text : "Hey look! It works.",
         position : { x : 250, y : 50 },
         size : { width : 160, height : 125 } 
    };
    m_workspace.createPanel(IDCH.examples.Panels.TEXT_PANEL, cfg,
            function(panel) {
                if (panel == null)
                    alert('failed');
            });
}

function config() {
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
    IDCH.load(["critspace", "critspace-repo", "vprops"], true, config, 
            function() { 
                alert("Failed to load IDCH modules."); 
            });
}

function configureCSDLModules() {
    CSDL.register("controls", [], [], ["tdd", "cp", "cs", "spin", "push", "slider", "bdlg"]);
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
    $P('yui.base.url',               "/js/yui/yui_2.8.0/");
    $P('idch.scripts.url',           "/js/IDCH/");
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.vprops.urls.types",  "/CritSpace/vprops/types");
    $P("idch.vprops.urls.groups", "/CritSpace/vprops/groups");
    $P("idch.vprops.urls.props",  "/CritSpace/vprops/properties");
    $P("idch.vprops.timeout",     5000);
    
    // load the logger
    IDCH.configLogger("logger", function() {
        configureCSDLModules();
    });
});


})();