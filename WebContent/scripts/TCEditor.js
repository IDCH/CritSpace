/*  Title: TCEditor.js
 *  Version:
 *  Created: 16 December 2010
 *  Author: Neal Audenaert (neal@idch.org)
 *  Copyright: Institute for Digital Christian Heritage (IDCH) 
 *             All Rights Reserved.
 */
if (IDCH) IDCH.namespace("nt")
else throw new Error("Core IDCH module not yet loaded.");

(function() {
    var lang  = YAHOO.lang,
        util  = YAHOO.util,
        dom   = util.Dom,
    
        Event    = util.Event,
        Selector = util.Selector,
        Editor   = YAHOO.widget.Editor,
        Window   = YAHOO.widget.EditorWindow,
        KeyMap   = IDCH.utils.KeyMap,
        KeyCodes = IDCH.utils.KeyCodes;
    
    var PROP_COMMAND  = "editvariant",
    
        CSS_VARIANT      = "variant",
        
        WINDOW_RENDER_EVENT  = "windowEditVariantRender",
        CLICK_EVENT          = PROP_COMMAND + 'Click',
        WINDOW_CLOSE_EVENT   = 'window' + PROP_COMMAND + 'Close', 
        AFTER_EXEC_EVENT     = "afterExecCommand";

//============================================================================
// TEXTUAL CRITICISM EDITOR
//============================================================================
    
/**
 * This class implements an extension of the Yahoo! YUI Rich Text Editor 
 * suitable for use in New Testament textual criticism applications. 
 * Specifically, this editor allows a textual critic to correct a supplied 
 * base text with the readings found in a manuscript witness. 
 * 
 * <p>This class is currently an early prototype designed for use by the 
 * Center for New Testament Textual Studies (CNTTS) in New Orleans, LA. Based
 * on our experiences and the broader interest from the community, we may 
 * work to make this tool more generally accessible. 
 * 
 * @class TCEditor
 * @public
 * @param baseEl { HTML } The textarea element on the page that contains the
 *      base text to be edited.
 * @param cfg { Object } An object literal containing the configuration 
 *      parameters for this editor. For more detail, please consult the YUI 
 *      ddocumentation.  
 */
function TCEditor(baseEl, cfg) {
    
    cfg.toolbar = {
        titlebar : 'TC Editor',
        buttons : [
            { group: 'textstyle', label: 'Font Style',
              buttons: [
                  { type: 'push', label: 'Edit Variant', value: 'editvariant' }
            ]}       
        ]
    };
    
    cfg.css = YAHOO.widget.SimpleEditor.prototype._defaultCSS + 
        'span.variant { display : inline; } ' + 
        'span.variant span.base { color:#999; } ' +
        'div.paragraph { display:block; margin-top:0.4em } ' +
        'span.verse { font-family:Helvetica, sans-serif; font-size:1.0em; line-height:1.4em;}';
    
    //========================================================================
    // EVENT HANDLERS
    //========================================================================
    
    function onEditorClick(obj) {
        // TODO implement support for creating line breaks
        var ev = obj.ev;

        if (obj.ev.altKey) {
            this._createCurrentElement('br');
            return false;
        }
    }

    /**
     * Listens for key down events and takes the appropriate action. This 
     * prevents most attempts to directly edit the underlying base text but 
     * allows non-editing commands to bubble up. Also responds to basic editing
     * keystrokes such as delete to mark a segment of text as deleted or ctrl-v
     * to open the variant editor.
     * 
     * @method onKeyDown
     * @private
     * @param obj { Object } An object containing the event that resulted in 
     *      a call to this method.
     */
    function onKeyDown (obj) {
        var ev = obj.ev, code = ev.keyCode;
        
        // TODO delete or backspace should create a deleted text variant
        //      ctrl-v should open the editor
        if (ev.ctrlKey && ev.shiftKey && (code == KeyCodes.V)) {
            Event.stopEvent(ev);
            m_editor.execCommand(PROP_COMMAND);
        } else if (ev.ctrlKey && (code == KeyCodes.V)) {
            // don't paste (common type-o for ctrl-shift-v).
            Event.stopEvent(ev);
        } else if ((code == KeyCodes.UP) || 
                   (code == KeyCodes.DOWN) || 
                   (code == KeyCodes.LEFT) || 
                   (code == KeyCodes.RIGHT)) {
            // do nothing - pass these keys through to the underlying 
            // editor so the user can use arrow keys to adjust the 
            // selection
        } else if (!ev.ctrlKey && !ev.altKey) {
            // trap all other keystrokes unless ctrl or alt key is pressed
            Event.stopEvent(ev);
        } 
    }
    
    //========================================================================
    // INITIALIZE THE EDITOR
    //========================================================================
    /**
     * A reference to the created editor.
     * 
     * @property m_editor
     * @private
     */
    var m_editor = new Editor(baseEl, cfg); 
    m_editor.on('toolbarLoaded', function() {
        m_editor.on('editorClick', onEditorClick, m_editor, true);
        m_editor.on('editorKeyDown', onKeyDown, m_editor, true);
        
        EditVariantWindow(m_editor, cfg.kmap);
    }, m_editor, true);
        
        
        
    
    return m_editor;
}

//============================================================================
// EDIT VARIANT WINDOW
//============================================================================

/**
 * A map of editors to which a VariantEditorWindow has been attached. This 
 * is used to prevent duplication of the window within a single editor.
 * 
 * @property s_veWindows
 * @private
 * @static
 */
var s_veWindows = {};


function EditVariantWindow(m_editor, kmap) {
    var m_id = m_editor.get('id');
    
    if (lang.isFunction(m_editor['cmd_' + PROP_COMMAND]))
        return;
    
    /**
     * The pre-rendered HTML for the window.
     * 
     * @property
     * @private
     */
    var m_bodyEl = null;
    
    var m_inputAdapter = null;
    
    var m_bodyId = m_editor.get('id') + '_evw';
    
    var STR_BUTTON_404 = 'Toolbar Button for (' + PROP_COMMAND + ') ' + 
                         'was not found, skipping exec.'; 
    var STR_WINDOW_TITLE = "Edit Variant Reading";
    
    
    //========================================================================
    //  HELPER METHODS
    //========================================================================
       
    /**
     * Given a base node, returns that node or an ancestor of that node, such 
     * that the returned node represents and observed variant reading in the 
     * text.
     * 
     * @method getVariantNode
     * @private
     * @param el { HTMLElement } 
     */
    function getVariantNode(el) {
        var result = null;
        if (dom.hasClass(el, CSS_VARIANT)) {
            result = el;
        } else {
            el = dom.getAncestorByClassName(el, CSS_VARIANT);
        }
        
        return el;
    }
    
    /**
     * Pre-renders the HTML to be displayed by the 'Edit Variant' window. This
     * method is attached to the 'renderWindow' event.
     * 
     * @method renderWindowHTML
     * @private
     */
    function renderWindowHTML() {
        var body = $EL('div'),
            WINDOW_LABEL_CSS = 'style="float:left;width:110px;font-weight:bold;"',
            WINDOW_INPUT_CSS = 'style="float:left;margin-top:5px;"';
        body.id = m_bodyId;
        
        body.innerHTML = 
                '<form class="editvariant-window">' +
                '  <div>' + 
                '    <label ' + WINDOW_LABEL_CSS + ' for="mss">MSS Reading: </label>' +
                '    <input class="mss" ' + WINDOW_INPUT_CSS + ' name="mss" type="text" value="" size="40"/>' +
                '  </div>' +
                '  <div>' + 
                '    <label ' + WINDOW_LABEL_CSS + ' for="base">Base Text: </label>' +
                '    <input class="base" ' + WINDOW_INPUT_CSS + ' name="base" type="text" value="" size="40"/>' +
                '  </div>' +
                '</form>';
            
        m_editor._windows[PROP_COMMAND] = {
            body: body
        };
    }
    
    /**
     * Synchronizes the Edit Variant window with a variant element in the
     * document. This should be called after the window is displayed.
     * 
     * @method syncWindow
     * @private
     * @param varianEl { HTMLElement } The element on the page that is 
     *      to be synced with the displayed window.
     */
    function syncWindow(variantEl) {
        var baseEl = Selector.query("span.base", variantEl, true),
            mssEl  = Selector.query("span.mss", variantEl, true),
            
            body      = $(m_bodyId),
            baseInput = Selector.query("input.base", body, true), 
            mssInput  = Selector.query("input.mss", body, true);
        
        if (baseInput) {
            baseInput.value = (baseEl) ? baseEl.innerHTML : "";
            baseInput.disabled = true;
        }

        if (mssInput) {
            mssInput.value = (mssEl)  ? mssEl.innerHTML : "";
            lang.later(100, m_editor, function() {
                mssInput.focus();
            });
            
            // attach key map adapter
            m_inputAdapter = kmap.attach(mssInput);
        }
    }
    
    /**
     * Synchronizes the 
     */
    function syncVariantElement(variantEl) {
        var mssEl    = Selector.query("span.mss", variantEl, true),
            mssInput = Selector.query("input.mss", $(m_bodyId), true),
            value    = (mssInput) ? mssInput.value : ""; 
        
        // sanity check
        if (!lang.isString(value) || !mssEl) {
            return;
        }
        
        value = value.trim();
        if (value.length >= 0) {
            mssEl.innerHTML = value;
        } else {
            // TODO this is deleted text
        }
    }
    
    //========================================================================
    // EVENT HANLDERS
    //========================================================================
   
    function editVariant() {
        // TODO HANDLE DIFFERENT SELECTION CASES (DELETION, ADDITION, REPLACEMENT)
        //      CLEAR VARIANT READING
        
        // the current element should be a variant span
        var win, el = m_editor.currentElement[0];
        if (!dom.hasClass(el, "variant")) {
            // XXX BAD ELEMENT, LOG ERROR
            return;
        }
        
        // display the editor window
        win = new Window(PROP_COMMAND, { width: '415px' });
        win.setHeader('Edit Variant');
        m_editor.openWindow(win);
        
        // update the ed window display with the contents of the variant
        // reading being edited
        syncWindow(el);
        
        // NOTE Not too clear why this is here. This is taken from Dav Glass's 
        //      example. Seems to work fine without, so I'll leave it commented
        //      out for now. 
//        this.on(AFTER_EXEC_EVENT, function() {
//            this.get('panel').syncIframe();
//        }, this, true);
    }
    
    function handleWindowClose(obj, editor) {
        var el     = obj.el,
            window = obj.win,
            body   = $(m_bodyId);
        
        if (m_inputAdapter) {
            // detach the adapter that support Greek text entry in the editor window.
            m_inputAdapter.detach();
            m_inputAdapter = null;
        }
        
        // get values and update dom
        // reset form values
        syncVariantElement(m_editor.currentElement[0]);
        
        // clear the selected node
        m_editor.nodeChange();
        m_editor.currentElement = [];
    }
    
    /** 
     * Handles double-click events on existing variant readings in order to 
     * edit them.
     *  
     * @method handleDoubleClick
     * @private
     * 
     */
    function handleDoubleClick(obj) {
        var variantEl = getVariantNode(m_editor._getSelectedElement());
        
        if (lang.isObject(variantEl)) {
            Event.stopEvent(obj.ev);                // I'll handle this.
            m_editor.execCommand(PROP_COMMAND);
            return false;
        }
    }
    
    /**
     * Called whenever the focus node in the editor changes so that we can 
     * determine whether or not to enable the 'Edit Variant' button and whether 
     * that button should be marked as pressed or not for the current node.
     * 
     * @method handleAfterNodeChange
     * @private
     */
    function handleAfterNodeChange() {
        // FIXME this needs to make sense based on actual editing techniques
        var el = this._getSelectedElement(),
            verse   = dom.hasClass(el, CSS_VERSE) ? el : dom.getAncestorByClassName(el, CSS_VERSE);
            variant = dom.getAncesstorByClassName(el, CSS_VARIANT); 
        if (el && dom.hasClass(el, CSS_VERSE)) {
            this.toolbar.enableButton(PROP_COMMAND);
        } else {
            
        }
        if (this._hasSelection()) {
            this.toolbar.disableButton(PROP_COMMAND);
        } else {
            this.toolbar.enableButton(PROP_COMMAND);
            if (dom.hasClass(el, CSS_VARIANT)) { 
                this.toolbar.selectButton(PROP_COMMAND); 
            } else { 
                this.toolbar.deselectButton(PROP_COMMAND); 
            } 
        }
    }
    

    /**
     * This is an <code>execCommand</code> override function. It is called 
     * whenever the <code>editvariant</code> command is invoked.
     */
    function cmdEditVariant() {
        // NOTE the example at http://new.davglass.com/files/yui/editor41/
        var el = m_editor._getSelectedElement(),
            variantEl = getVariantNode(el);
        
        if (variantEl) {                    // edit an existing variant
            el = variantEl;
            m_editor.currentElement[0] = el;
            editVariant.call(m_editor);
            
        } else {                            // create a new variant reading
            if (m_editor.get('insert')) {
                el = m_editor._createInsertElement();
                // NOTE this is an insertion
                el.innerHTML = 
                    '<span class="base"></span>' +
                    '<span class="mss"></span>';
            } else {
                m_editor._createCurrentElement('span');
                el = m_editor.currentElement[0];
                
                // NOTE this is a replacement
                el.innerHTML = 
                    '<span class="base">' + el.innerHTML + '</span>' +
                    '<span class="mss"></span>';
            }
            
            el = m_editor._swapEl(el, "span");
            dom.addClass(el, "variant");
            
            m_editor.currentElement[0] = el;
            editVariant.call(m_editor);
        }
        
        return [false];
    }
    
    //========================================================================
    // ATTACH METHODS TO EDITOR
    //========================================================================
    // main command handler
    m_editor.cmd_editvariant = cmdEditVariant;
    
    m_editor.on('editorDoubleClick', handleDoubleClick, m_editor, true);
    m_editor.on('afterNodeChange',   handleAfterNodeChange, m_editor, true);
    m_editor.on(WINDOW_CLOSE_EVENT,  handleWindowClose, m_editor, true);
    
    m_editor.on('windowRender', renderWindowHTML);
}

IDCH.nt.TCEditor          = TCEditor;
IDCH.nt.EditVariantWindow = EditVariantWindow;
})();