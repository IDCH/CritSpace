/* We're going to put our source script in this file to minimize the amount of 
 *  code in our main page.
 */


(function() {   /* don't polute the main namespace.  */


function main(obj, undef) {
    
    // INITIALIZE GLOBAL ALIASES
    var vp = new IDCH.tzivi.Viewport($("tzivi-container"), {
        visible   : true,  // FIXME make this actually do something
        vpWidth   : 800,   // Max width of the viewport 
        vpHeight  : 600,   // Max height of the viewport 
        width     : 600,   // Width of the content pane at 100% zoom 
        height    : 400,   // Height of the content pane at 100% zoom
        x         : 0,     // Initial x position
        y         : 0,     // Initial y position
        maxRatio  : 1.0,   // Maximum zoom ratio
        minRatio  : 0.001, // Minimum zoom ratio
        ratio     : 1      // Current zoom ratio 
    });
    
    vp.show();
    
    var imageURI = "http://localhost:8080/CritSpace/images/tzi/NT1";
    var src = new IDCH.tzivi.TziSource(imageURI);
    
    var layer = new IDCH.tzivi.TziLayer(src);
    layer.on("ready", function(tziSrc) {
        var zSlider = new IDCH.tzivi.ZoomSlider($("zoomslider"), vp);
        vp.show();
        if (YAHOO.lang.isFunction(layer.snapTo)) 
            layer.snapTo();
    });
    vp.addLayer(layer);
    
    
}


YAHOO.util.Event.addListener(window, "load", function() {
    $P('yui.base.url',     "/CritSpace/yui/yui_2.8.0/");
    $P('idch.scripts.url', "/CritSpace/scripts/IDCH/");
    
    $P('idch.tzivi.sliderthumb', "assets/thumb-v.png");
    
    function onFail() { alert("failed to load modules."); }
    
    // load the logger
    IDCH.configLogger("logger", function() {
        IDCH.load(["tzivi", "tzivi-images"], true, main, onFail);
    });
});

})();
