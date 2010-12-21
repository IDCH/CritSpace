(function() {
    
    var dom         = YAHOO.util.Dom;
    var isValue     = YAHOO.lang.isValue;
    var Event       = YAHOO.util.Event;
    
    /** Shorthand for accessing the YAHOO.util.Dom.setStyle method. */
    var $style = dom.setStyle.bind(YAHOO.util.Dom);

    var page = null;

    function initViewport() {
        var Viewport    = IDCH.tzivi.Viewport,
            ZoomSlider  = IDCH.tzivi.ZoomSlider,
            APIResolver = IDCH.images.APIResolver,
            ImageProxy  = IDCH.images.ImageProxy,
            TziLayer    = IDCH.tzivi.TziLayer,
            MarkerLayer = IDCH.tzivi.MarkerLayer,
            
            viewport, zoomSlider, resolver, proxy, imageLayer, markerLayer;
        
        // instantiate the viewport
        viewport = new Viewport($("afed-imagedisp-v"), {
            visible   : true,  
            vpWidth   : 790,  
            vpHeight  : 600 
        });
    
        // attach the slider
        zoomSlider = new ZoomSlider($("afed-imagedisp"), viewport);
            
        // load the image
        resolver = new APIResolver($P('idch.afed.facsim.image.servlet'));
        proxy    = new ImageProxy(7411, resolver);
        
        imageLayer = new TziLayer(proxy.getTziSource());
        viewport.addLayer(imageLayer);
        
        markerLayer = new MarkerLayer();
        viewport.addLayer(markerLayer);
//        m_imageLayer.on("ready",  onImageReady);
        viewport.show();
        viewport.setRatio(.24);
    }
    
YAHOO.util.Event.addListener(window, "load", function() {
    $P('yui.base.url',               "/js/yui/yui_2.8.1/");
    $P('idch.scripts.url',           "/js/IDCH/");
    
    $P('idch.tzivi.sliderthumb',         "/CritSpace/assets/thumb-v.png");
    $P('idch.afed.facsim.servlet',       "/AFED/facsimile");
    $P('idch.afed.facsim.image.servlet', "/AFED/image");
    

    // load the logger
    IDCH.configLogger("logger", function() {
        var modules = ["afed", "images-scroller", 
                       "tzivi", "tzivi-images", "tzivi-markers", 
                       "selector", "animation"];
        try {
            IDCH.load(modules, true, initViewport, function() {
                alert("Failed to load IDCH modules."); 
            }); 
        } catch (ex) {
            $warn(ex.toString);
        }
    });
});

})();