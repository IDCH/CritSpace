/* Initialization script for the CritSpace framework.
 * 
 */ 

(function() {
    
var Note = null;
var Thumb = null;
var TziviPanel = null;
var Filmstrip = null;
var Facsimile = null;

function initFilmstrip() {
    var vcfg = {  
        position : {
            top  : { defaultvalue : 50 },
            left : { defaultvalue : 25 }
        },

        size : {
            width  : { range : [50, 2000], defaultvalue : 800 },
            height : { range : [166, 166], defaultvalue : 166 }
        } 
    };
    
    var resolver = 
        new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    var id = 11237;
    Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            var msg = "Could not retrieve fasimile: " + id;
            $warn(msg);
            
            return;
        }  
        
        var collations = facs.getCollations();
        var images = collations[0].getImages();
        var filmstrip = new Filmstrip({
            vprops : vcfg,
            images : images,
            imageProperties : { width   : 152, height  : 146 }, 
            resolver : resolver,
            format : "small"
        });
        
    }); 
}

function initTziviPanel() {
    var vcfg = {  
        position : {
            top  : { defaultvalue : 300 },
            left : { defaultvalue : 425 }
        },

        size : {
            width  : { range : [50, 2000], defaultvalue : 800 },
            height : { range : [50, 2000], defaultvalue : 600 }
        } 
    };
    
    var tzivi = new TziviPanel({
        vprops : vcfg,
        uri : "http://localhost:8080/CritSpace/images/tzi/NT1"
    });
}

function initNote() {
    var vcfg = {  
        position : {
            top  : { defaultvalue : 100 },
            left : { defaultvalue : 425 }
        },

        size : {
            width  : { range : [50, 2000], defaultvalue : 75 },
            height : { range : [50, 2000], defaultvalue : 75 }
        } 
    };
    
    var p2 = new Note({ vprops : vcfg });
}

function initThumb() {
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
        var thumb = new Thumb({ 
            vprops : new Object(vcfg),
            proxy : new IDCH.images.ImageProxy(url, resolver),
            format : "thumb"
//            url : "http://localhost:8080/CritSpace/images/tzi/NT1/thumb.jpeg"
//            url : "images/astrolabes.jpg"
        });
}

function main() {
    CSDL.critspace.init();
    Note = CSDL.critspace.NotePanel;
    
    TziviPanel = IDCH.tzivi.TziviPanel;
    Filmstrip  = IDCH.images.FilmstripPanel;
    Thumb      = IDCH.images.ThumbPanel;
    Facsimile  = IDCH.afed.Facsimile;
    
    initThumb();
    initTziviPanel();
    initFilmstrip();
}

function loadIDCH() {
    function onFail() { 
        alert("failed to load modules."); 
    }
    IDCH.load(["tzivi-panel", "images-filmstrip", "images-panel", "afed"], true, main, onFail);
}

function configureCSDLModules() {
    CSDL.register("map",      ["utils/Map.js"],                         [], []);
    CSDL.register("kc",       ["utils/KeyCodes.js"],                    [], []);
    CSDL.register("log",      ["utils/Logger.js"],                      ["utils/em.css"], ["map"]);
    CSDL.register("utils",    [], [], ["map", "kc", "log"]);
    
    CSDL.register("vprops",   ["widgets/visual/VisualProperty.js"],     [], ["log", "map"]);
    CSDL.register("stdprops", ["widgets/visual/StandardProperties.js"], [], ["vprops"]);
    CSDL.register("vis",      [], [], ["vprops", "stdprops"]);
    
    CSDL.register("tdd",      ["widgets/controls/TextDropDown.js"],     [], ["log", "kc"]);
    CSDL.register("cp",       ["widgets/controls/ColorPicker.js"],      [], []);
    CSDL.register("cs",       ["widgets/controls/ColorSwatch.js"],      [], ["cp"]);
    CSDL.register("spin",     ["widgets/controls/SpinControl.js"],      [], ["log", "kc"]);
    CSDL.register("push",     ["widgets/controls/PushButton.js"],       [], ["log", "kc"]);
    CSDL.register("slider",   ["widgets/controls/Slider.js"],           [], ["log", "kc"]);
    CSDL.register("bdlg",     ["widgets/controls/BorderDialog.js"],     [], ["cs", "spin", "tdd"]);
    CSDL.register("controls", [], [], ["tdd", "cp", "cs", "spin", "push", "slider", "bdlg"]);
    
    CSDL.register("cspace",   ["critspace/CritSpace.js"],               [], ["utils", "controls"]);
    CSDL.register("panel",    ["critspace/Panel.js"],                   [], ["utils", "controls"]);
    CSDL.register("note-p",   ["critspace/panels/NotePanel.js"],        [], ["utils", "panel"]);
    CSDL.register("thumb-p",  ["critspace/panels/ThumbPanel.js"],        [], ["utils", "panel"]);
    CSDL.register("critspace",[], [], ["cspace", "note-p", "thumb-p" ]);
    
    CSDL.load(["tdd", "spin", "cs", "slider", "stdprops", "critspace"], loadIDCH);
}

// configure source code dependencies
YAHOO.util.Event.addListener(window, "load", function() {
    $P('yui.base.url',     "/CritSpace/yui/yui_2.8.0/");
    $P('idch.scripts.url', "/CritSpace/scripts/IDCH/");
    
    $P('idch.tzivi.sliderthumb', "assets/thumb-v.png");
    $P('idch.afed.facsim.servlet', "/AFED/facsimile");
    $P('idch.afed.facsim.image.servlet', "/AFED/image");

    // load the logger
    IDCH.configLogger("logger", function() {
        configureCSDLModules();
    });
});


})();
