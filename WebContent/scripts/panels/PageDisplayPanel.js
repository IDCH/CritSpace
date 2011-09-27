/* Title: PageDisplayPanel.js
 * Version: 
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 */

/**
 * Provides an implementation of a CritSpace panel that displays a page 
 * from a facsimile document.
 * 
 * @module afed
 */

IDCH.namespace("afed");
(function() {
    if (IDCH.afed.PAGE_DISPLAY_PANEL)
        return;
    
var lang  = YAHOO.lang,
    dom   = YAHOO.util.Dom,
    util  = YAHOO.util,

    Event = YAHOO.util.Event,

    Viewport   = IDCH.tzivi.Viewport,
    ZoomSlider = IDCH.tzivi.ZoomSlider,
    TziLayer   = IDCH.tzivi.TziLayer,
    TziSource  = IDCH.tzivi.TziSource,
        
    PROP_FACS_ID   = "facsimileId",
    PROP_COLLATION = "collationId",
    PROP_SEQ_NO    = "seqNo",
    
    
    LOGGER = "IDCH.afed.PageDisplayPanel",

    PANEL_TYPE = "org.idch.afed.PageDisplay";

function PageDisplayPanel(m_panel, m_cfg) {

    //===================================================
    // Create Panel and Initialize Member Variables
    //===================================================
    
    // TODO document these
    var m_bodyElement      = null;
    var m_visualProperties = null;
    var m_imageLayer       = null;
    var m_aspect           = 1;
    var m_zoomSlider       = null;
    var m_viewport         = null;
    var m_url              = null;
    

    /**
     * The facsimile id. This corresponds to the named property 
     * <code>PROP_FACS_ID</code>.
     * 
     * @param m_fId
     * @private
     * @type Number
     */
    var m_fId
    
    /**
     * The id of the collation to be displayed. This corresponds to the 
     * named property <code>PROP_COLLATION</code>.
     * 
     * @param m_cId
     * @private
     * @type Number
     */
    var m_cId
    
    /**
     * The sequence number that identifies the current image to display. 
     * This corresponds to the named property <code>PROP_SEQ_NO</code>.
     * 
     * @param m_seqNo
     * @private
     * @type Number
     */
    var m_seqNo = m_panel.getNamedProperty(PROP_SEQ_NO) || m_cfg.seqNo || 0;
    
    var m_facsimile      = null;

    //=========================================================================
    // VIEWPORT MANIPULATION METHODS 
    //=========================================================================
    
    /**
     * Constructs the viewport and adds the tzi image layer. 
     * 
     * @method createViewport
     * @private
     */
    function initViewport() {
        var size =  m_visualProperties.size,
            border = m_visualProperties.border,
            leftControl = m_panel.getControls().left,
            m_url = m_cfg.url || m_cfg.uri;
        
        if (!lang.isNumber(border.width.get()))
            border.width.set(1);      // set this to a reasonable value
        
        // configure the image source
        if (!lang.isValue(m_cfg.tziSource)) {
            if (lang.isString(m_cfg.uri)) {
                m_cfg.tziSource = new TziSource(m_cfg.uri);
                m_panel.setNamedProperty("tziImageURL", m_cfg.uri);
            } else {
                var msg = "Could not configure image source: No URI provided.";
                $error(msg, LOGGER);
                throw msg;
            }
        }
        
        // create the viewport
        m_viewport = new Viewport(m_bodyElement, {
            visible   : true,  // FIXME make this actually do something
            vpWidth   : size.width.get(),  
            vpHeight  : size.height.get(), 
            width     : 600,   
            height    : 400   
        });
        
        m_imageLayer = new IDCH.tzivi.TziLayer(m_cfg.tziSource);
        
        // initialize the zoom slider
        m_zoomSlider = new ZoomSlider(leftControl.getEl(), m_viewport);

        // attach the image layer
        attachListeners();
        m_viewport.addLayer(m_imageLayer);
    }
    
    /**
     * Resizes the viewport (and panel) to fit the image.
     * 
     * @method resize
     * @private
     */
    function resize() {
        var frame = m_viewport.getFrame();
        
        var bwidth = m_visualProperties.border.width;
        var bwidth = (bwidth.getPreview() || bwidth.get()) + "px";
        // not sure why, but setting 'top' doesn't seem to work. 
        dom.setStyle(frame, "margin-top", bwidth);        
        dom.setStyle(frame, "margin-left", bwidth);
        
        // the actual size of the editor is a function of its height and width 
        // along with other factors such as its border and padding.
        
        // calculate the difference in the actual size
        var reg = dom.getRegion(frame);
        var frameWidth  = dom.getStyle(frame, "width").replace("px", "");
        var frameHeight = dom.getStyle(frame, "height").replace("px", "");
        
        var wOffset = (reg.right - reg.left) - frameWidth;
        var hOffset = (reg.bottom - reg.top) - frameHeight;
        
        // get the targeted width and height
        var size = m_visualProperties.size;
        var panelWidth  = size.width.getPreview() || size.width.get();
        var panelHeight = size.height.getPreview() || size.height.get();
        var width       = panelWidth - wOffset;
        var height      = panelHeight - hOffset;
        
        m_viewport.setSize(width, height);
        
        // update viewport zoom ratio if layer is smaller than width & height
        if ((width > m_imageLayer.getRelativeWidth()) &&
             height > m_imageLayer.getRelativeHeight()) {
            
            var targetRatio = width / m_imageLayer.getWidth();
            if (targetRatio > m_viewport.getRatio() && targetRatio <= 1) {
                m_viewport.setRatio(targetRatio);
            }
        }
        
        m_viewport.resetConstraints();
    }
    
    /** 
     * Finds the best fit for the for a given width and height.
     * 
     * @method findBestFitHeight
     * @private
     * @param width { Number } The width of the box in which the image 
     *      should fit.
     * @param height { Number } The height of the box in which the image 
     *      should fit.
     *      
     * @return { Number } The height, h, of the resulting box. The 
     *      corresponding width, w, can be derived directly from this 
     *      height: <code>w = h * aspect</code>
     */
    function findBestFitHeight(width, height) {
        var aspect = width / height;   // aspect ratio of the target dimensions 
 
        // squash one dimension
        if (m_aspect > 1) {       
            if (aspect > m_aspect) width  = Math.floor(height / m_aspect);
            else                   height = Math.floor(width / m_aspect);
        } else {                
            if (aspect > m_aspect) width  = Math.floor(height * m_aspect);
            else                   height = Math.floor(width * m_aspect);
        }
        
        return [width, height];
    }
    
    //=========================================================================
    // EVENT HANDLING METHODS 
    //=========================================================================
    
    /**  
     * Attaches event listeners for this panel during the rendering process.
     */
    function attachListeners() {
        var frame = m_viewport.getFrame();
        Event.on(frame, "click", function() {
            m_panel.focus();
        });
        
        // keep track of the aspect ratio
        function onResize() {
            m_aspect = m_imageLayer.getWidth() / m_imageLayer.getHeight();
        }
        
        m_panel.on("focus", m_zoomSlider.setStartState);
        
        m_imageLayer.on("resize", onResize);    // any time the image changes
        m_imageLayer.on("ready", onResize);     // at first opportunity
        m_imageLayer.on("ready", onImageReady);
        
        constrainSize();
    }
    
    /** 
     * Creates override functions for the <code>constrain</code> method of the 
     * width and height properties of this panel. This creates an enclosure 
     * used to track the last requested size as a proxy for where the user's 
     * mouse is.  
     * 
     * <p>These constaints allow the panel (and viewport) to be freely resized 
     * when the viewport is small then the image being displayed. When the 
     * viewport is larger, this attempts to zoom the image to fill the 
     * viewport.  
     */
    function constrainSize() {
        // TODO this still results in pretty jumpy interactions.
        // The last values attempted for the width and height of the panel. 
        // We'll use this as a proxy to track the mouse's movement and 
        // constrain the size properties so the the image is not smaller 
        // than the box. 
        var lastWidth, lastHeight;
        var size = m_visualProperties.size;
        
        /** Constrain the width of the panel. */
        size.width.constrain = function(val) {
            lastWidth = val;
            var relWidth  = m_imageLayer.getRelativeWidth();        // might query this directly from the TziLayer
            var relHeight = m_imageLayer.getRelativeHeight();
            
            // pass through if the width is less than the width of the image, 
            if (val < relWidth)
                return val;
            
            if ((lastWidth > relWidth) && (lastHeight > relHeight)) {
                // make fit in box
                var dim = findBestFitHeight(lastWidth, lastHeight);
                return dim[0];
            } else {
                // constrain to current image width
                return relWidth;
            }
        };
        
        /** Constrain the height of the panel. */
        size.height.constrain = function(val) {
            lastHeight = val;
            var relWidth  = m_imageLayer.getRelativeWidth();        // might query this directly from the TziLayer
            var relHeight = m_imageLayer.getRelativeHeight();
            
            // pass through if the height is less than the height of the image, 
            if (val < relHeight)
                return val;
            
            if (lastWidth > relWidth && lastHeight > relHeight) {
                // make fit in box
                var dim = findBestFitHeight(lastWidth, lastHeight);
                return dim[1];
            } else {
                // constrain to current image height
                return relHeight
            }
        };
    }
    

    function onImageReady() {
        var size = m_visualProperties.size;
        var dim = findBestFitHeight(size.width.get(), size.height.get());
        size.width.set(dim[0]);
        size.height.set(dim[1]);
        
        var ratio = dim[0] / m_imageLayer.getWidth();
        m_viewport.show();
        m_viewport.setRatio(ratio);
        
        resize();
    }
     
   
   
    function initialize() {
        try {
         // specify fixed config attributes
            m_cfg.showTab = true;
            
            m_panel.configure(m_cfg);
            m_bodyElement      = m_panel.getBody();
            m_visualProperties = m_panel.getVisualProperties();
            
            initViewport();
            
            m_visualProperties.on("change", resize);
            m_visualProperties.on("preview", resize);
            m_visualProperties.on("revert", resize);
        } catch (ex) {
            m_panel = null;
            m_bodyElement = null;
            m_visualProperties = null;
            
            var msg = "Could not create TziviPanel. " + ex;
            $error(msg, LOGGER);
        }
        
        return m_panel;
    }
    
    return initialize();
}

/**
 * Symbolic constant for the PageDisplay type. 
 * @property PAGE_DISPLAY_PANEL org.idch.afed.PageDisplay
 * @public
 */
IDCH.afed.PAGE_DISPLAY_PANEL = PANEL_TYPE;

//finally, register this panel constructor with the workspace
IDCH.critspace.PanelRegistry.register(PANEL_TYPE, PageDisplayPanel);

})();   // close and invoke containing function
