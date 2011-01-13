/*  Title: TCEditor.js
 *  Version:
 *  Created: 16 December 2010
 *  Author: Neal Audenaert (neal@idch.org)
 *  Copyright: Institute for Digital Christian Heritage (IDCH) 
 *             All Rights Reserved.
 *             
 *  NOTE the example at http://new.davglass.com/files/yui/editor41/
 */

// TODO Need to support multiple readings (multiple hands, for example).
//      Support Lacunae
//      Indicate method of alternative reading (e.g., marginal correction).
//      Mouseover of variant should display contextual information
//      Wigitify variant reading
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
        
        WINDOW_RENDER_EVENT  = "windowRender",
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
 * work to make this tool useful to a more general audience. 
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
        'span.variant span.wit { display:none; } ' +
        'span.variant span.base { display:inline; }' +
        
        'span.variant.replacement span.wit { display:inline; color:green; font-weight:bold; } ' +
        'span.variant.replacement span.base { display:none; }' +
        
        'span.variant.insertion span.wit { display:inline; color:green; font-weight:bold; } ' +
        'span.variant.insertion span.base { display:none; }' +
        
        'span.variant.deletion span.wit { display:none; }' +
        'span.variant.deletion span.base { display:inline; color:darkred;text-decoration:line-through; }' +
        
        'div.paragraph { display:block; margin-top:0.4em } ' +
        'span.verse { font-family:Helvetica, sans-serif; font-size:1.0em; line-height:1.4em;}';
    
    /** A reference to the created editor. */
    var m_editor = new Editor(baseEl, cfg); 
    
    /** The widnow object used to edit variants. */
    var m_evWindow = null;
    
    //========================================================================
    // EVENT HANDLERS
    //========================================================================
    
    /**
     * Event handler that inserts line breaks whenever the user alt-clicks.
     * 
     * @method onEditorClick
     * @private
     * @param obj { Object } The object supplied by the event handling system.
     * @returns {Boolean} <code>false</code> if this method handles the event,
     *      otherwise <code>unspecified</code>
     */
    function onEditorClick(obj) {
        var ev = obj.ev;

        if (obj.ev.altKey) {
            this._createCurrentElement('br');
            // TODO attach class information and possibly other attributes
            //      that enable this to be a true line break marker
            // TODO remove leading spaces on the resulting lines
            return false;
        }
    }
    
    /** 
     * Enables support to edit a variant reading by double-clicking on it 
     * in the editor.
     * 
     * @method
     * @private onEditorDoubleClick
     * @returns {Boolean} <code>false</code> if this method handles the event,
     *      otherwise <code>unspecified</code>
     */
    function onEditorDoubleClick(obj) {
        var variantEl = m_editor.getVariantNode();
        
        if (lang.isObject(variantEl)) {
            Event.stopEvent(obj.ev);                // I'll handle this.
            m_editor.execCommand(PROP_COMMAND, variantEl);
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
        var variant, ev = obj.ev, code = ev.keyCode;
        
        if ((code == KeyCodes.V) && (ev.ctrlKey && ev.shiftKey)) {      // ctrl-shift-v
            Event.stopEvent(ev);
            m_editor.execCommand(PROP_COMMAND);
        } else if (ev.ctrlKey && (code == KeyCodes.V)) {
            // don't paste (common type-o for ctrl-shift-v).
            Event.stopEvent(ev);
        } else if ((code == KeyCodes.DEL) || (code == KeyCodes.BACK)) {
            Event.stopEvent(ev);
            variant = m_editor.createVariant();
            variant.setWitnessedReading(null);
        } else if ((code == KeyCodes.UP) || 
                   (code == KeyCodes.DOWN) || 
                   (code == KeyCodes.LEFT) || 
                   (code == KeyCodes.RIGHT)) {
            // do nothing - pass these keys through to the underlying 
            // editor so the user can use arrow keys to adjust the selection
        } else if (!ev.ctrlKey && !ev.altKey) {
            // trap all other keystrokes unless ctrl or alt key is pressed
            Event.stopEvent(ev);
        } 
    }
    
    /**
     * Called whenever the focus node in the editor changes so that we can 
     * determine whether or not to enable the 'Edit Variant' button and whether 
     * that button should be marked as pressed or not for the current node.
     * 
     * @method onAfterNodeChange
     * @private
     */
    function onAfterNodeChange() {
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
    
    //========================================================================
    // EXTEND THE EDITOR
    //========================================================================
   
    lang.augmentObject(m_editor, {
        
        /**
         * Given a base node, returns that node or an ancestor of that node, such 
         * that the returned node represents and observed variant reading in the 
         * text.
         * 
         * @method getVariantNode
         * @private
         * @param el { HTMLElement } 
         */
        getVariantNode : function(el) {
            el = el || m_editor._getSelectedElement();
            if (lang.isValue(el)) {
                el = !dom.hasClass(el, CSS_VARIANT) 
                            ? dom.getAncestorByClassName(el, CSS_VARIANT) : el;
            }
            
            return el;
        },
        
        /**
         * Creates a new <code>VariantReading</code> based on the current 
         * selection in the editor.
         * 
         * @method createVariant
         * @private;
         * @returns { VariantReading }
         */
        createVariant : function() {
            return new VariantReading(this, this.getVariantNode());
        },
        
        /**
         * Returns an array of all variants in this document.
         * 
         * @method listVariants
         * @public
         * @returns { Array } An array of VariantReadings for each variant that
         *      has been identified in this document.
         */
        listVariants : function() {
            var i, results = [],
                body = m_editor._getDoc().body,
                elements = Selector.query("span." + CSS_VARIANT, body);
            
            for (i = 0; elements.length; i++) {
                results.push(new VariantReading(this, elements[i]));
            }
            
            return results;
        },
        
        /**
         * This is an <code>execCommand</code> override function. It is called 
         * whenever the <code>editvariant</code> command is invoked.
         * 
         * @method cmd_editvariant
         * @priave
         * @param value { HTMLElement } The DOM element of the variant to edit.
         */
        cmd_editvariant : function(value) {
            var variantEl = lang.isObject(value) ? value : this.getVariantNode(),
                reading   = new VariantReading(this, variantEl);
            
            m_editor.currentElement[0] = reading.getElement();
            m_evWindow.show(reading);
            return [false];
        }
    });
    
    // attach event listeners to the editor
    m_editor.on('toolbarLoaded', function() {
        m_editor.on('editorClick',       onEditorClick,       m_editor, true);
        m_editor.on('editorDoubleClick', onEditorDoubleClick, m_editor, true);
        m_editor.on('editorKeyDown',     onKeyDown,           m_editor, true);
        m_editor.on('afterNodeChange',   onAfterNodeChange,   m_editor, true);
        
        m_evWindow = new EditVariantWindow(m_editor, cfg.kmap);
    }, m_editor, true);
    
    return m_editor;
}

//============================================================================
// VARIANT READING CLASS
//============================================================================
var CSS_BASE        = "base",           // The base text reading
    CSS_WIT         = "wit",            // The witnessed reading
    CSS_VARIANT     = "variant",        // Identifies variant span
    CSS_INSERTION   = "insertion",      // identifies a variant span as an insertion
    CSS_REPLACEMENT = "replacement",    // identifies a variant span as a replacement
    CSS_DELETION    = "deletion";       // identifies a variant span as a deletion
    
/** 
 * 
 * @param m_editor
 * @param variantEl { HTMLElement } 
 * @returns { VariantReading }
 */
function VariantReading(m_editor, variantEl) {  
    
    /** The DOM element used to render this variant. */
    var m_element = null;
    
    /** The span containing the base text. */
    var m_baseSpan = null;
    
    /** The span containing the witnessed reading. */
    var m_witSpan = null;
    
    /** Indicates that this is a newly created reading (as opposed to one
     *  that has been restored from the document.  */
    var m_isNew   = false;
    
    /**
     * Throws an error if the VariantReading is not properly configured. This
     * may occur if the reading is accessed after it has been cleared from
     * the document.
     * 
     *  @method checkState
     *  @private
     */
    function checkState() {
        if (!m_element || !m_baseSpan || !m_witSpan) {
            throw new Error("No variant element. Has this element been cleared?");
        }
    }
    
    var that = { 
        /** 
         * Indicates whether this is a newly created reading (as opposed to one
         * that has been restored from the document.
         * 
         * @method isNew
         * @protected
         * @return { Boolean } <code>true</code> if this reading was created
         *      from the associated <code>TCEditor</code> rather than being 
         *      restored from the document.
         */
        _isNew : function() {
            return m_isNew;
        },
        
        /**
         * Returns the DOM element that embodies this variant reading.
         * 
         * @method getElement
         * @private
         * @returns { HTMLElement } The DOM element that embodies this variant
         *      reading. 
         */
        getElement : function() {
            return m_element;
        },
        
        /**
         * Attempts to set the witnessed reading. If the supplied reading is 
         * null, this will mark the base text as having been deleted.
         * 
         * @method setWitnessedReading
         * @public
         * @param reading { String } The reading for this base text as 
         *      observed in a manuscript.
         */
        setWitnessedReading : function(reading) {
            checkState();
            
            if (!reading) reading = "";
            m_witSpan.innerHTML = reading;
            
            if (dom.hasClass(CSS_INSERTION))    // don't need to update 
                return;                         // replacement/deletion status
            
            if (reading.trim().length == 0) 
                 dom.replaceClass(m_element, CSS_REPLACEMENT, CSS_DELETION);
            else dom.replaceClass(m_element, CSS_DELETION, CSS_REPLACEMENT);
        },
        
        /**
         * Returns the witnessed reading for this variant.
         * 
         * @method getWitnessedReading
         * @public
         * @returns { String } The text of the witnessed reading for this 
         *      variant.
         */
        getWitnessedReading : function() {
            checkState();
            return m_witSpan.innerText || m_witSpan.textContent;
        },
        
        /**
         * Returns the base text reading for this variant.
         * 
         * @method getBaseReading
         * @public
         * @returns { String } The base text reading for this variant.
         */
        getBaseReading : function() {
            checkState();
            return m_baseSpan.innerText || m_baseSpan.textContent;
        },
        
        /**
         * Indicates if this variant indicates that the MS omits a section of 
         * text found in the base text.
         * 
         * @method isOmission
         * @public
         * @returns { Boolean } <code>true</code> if this variant represnts an 
         *      omission, <code>false</code> otherwise.
         */
        isOmission : function() {
            checkState();
            return dom.hasClass(m_element, CSS_DELETION);
        },
        
        /**
         * Indicates if this variant represents a replacement of the base text.
         * A replacement is used here to indicate any situation in which the 
         * base reading has been relpaced by an alternate reading. Notably, 
         * this includes transpositions.
         * 
         * @method isReplacement
         * @public
         * @returns { Boolean } <code>true</code> if this variant represents a 
         *      replacement, <code>false</code> if it does not.
         */
        isReplacement : function() {
            checkState();
            return dom.hasClass(m_element, CSS_REPLACEMENT);
        },

        /**
         * An insertion (or addition) of new text not present in the base text. 
         * 
         * @method isInsertion
         * @public
         * @returns { Boolean } <code>true</code> if this variant represents a 
         *      insertion, <code>false</code> if it does not.
         */
        isInsertion : function() {
            checkState();
            return dom.hasClass(m_element, CSS_INSERTION);
        },
        
        /** 
         * Clears this variant reading from the document.
         * 
         * @method clear
         * @public
         */
        clear : function() {
            checkState();
            
            var doc = m_element.ownerDocument,
                baseText = m_baseSpan.innerHTML,
                textNode = doc.createTextNode(baseText),
                parent   = m_element.parentNode;
            
            parent.insertBefore(textNode, m_element);
            parent.removeChild(m_element);
            parent.normalize();
            
            m_element  = null;
            m_baseSpan = null;
            m_witSpan  = null;
        }
    };
    
    /**
     * Initialization method to create a new variant reading. This is called 
     * if no variant reading is supplied.
     */
    function createVariantElement() {
        var el, typeCss, baseReading = '';
        
        m_editor._createCurrentElement('span');
        el = m_editor.currentElement[0];
        baseReading = el.innerHTML;
        
        el.innerHTML = 
            '<span class="' + CSS_BASE + '">' + baseReading + '</span>' +
            '<span class="' + CSS_WIT  + '"></span>';
        m_element = m_editor._swapEl(el, "span");
        
        // apply class labels
        typeCss = (baseReading.length == 0) ? CSS_INSERTION : "";
        dom.addClass(m_element, CSS_VARIANT);
        dom.addClass(m_element, typeCss);
        
        m_baseSpan = Selector.query("span." + CSS_BASE, m_element, true);
        m_witSpan  = Selector.query("span." + CSS_WIT,  m_element, true);
        
        m_isNew = true;
    }
    
    /** 
     * Initialization method to restore a previously created variant element.
     */
    function restoreVariantElement() {
        if (!dom.hasClass(variantEl, CSS_VARIANT))
            throw new Error("Could not restore variant reading: invalid variant element supplied.");
        
        m_element = variantEl;
        m_baseSpan = Selector.query("span." + CSS_BASE, m_element, true);
        m_witSpan  = Selector.query("span." + CSS_WIT,  m_element, true);
    }
    
    if (!lang.isValue(variantEl)) createVariantElement();
    else restoreVariantElement();
    
    return that;
}

//============================================================================
// EDIT VARIANT WINDOW
//============================================================================

var WINDOW_LABEL_CSS = 'style="float:left;width:110px;font-weight:bold;"',
    WINDOW_INPUT_CSS = 'style="float:left;margin-top:5px;"';

var WINDOW_HTML = 
    '<form class="editvariant-window">' +
    '  <div>' + 
    '    <label ' + WINDOW_LABEL_CSS + ' for="mss">MS Reading: </label>' +
    '    <input class="mss" ' + WINDOW_INPUT_CSS + ' name="mss" type="text" value="" size="40"/>' +
    '  </div>' +
    '  <div>' + 
    '    <label ' + WINDOW_LABEL_CSS + ' for="base">Base Text: </label>' +
    '    <input class="base" ' + WINDOW_INPUT_CSS + ' name="base" type="text" value="" size="40"/>' +
    '  </div>' +
    '  <div class="buttons">' + 
    '    <input type="button" name="reset"  value="Remove"/>' +
    '    <input type="button" name="cancel" value="Cancel"/>' +
    '    <input type="button" name="ok"     value="OK"/>' +
    '  </div>' + 
    '</form>';

var STR_BUTTON_404 = 'Toolbar Button for (' + PROP_COMMAND + ') ' + 
                     'was not found, skipping exec.'; 
var STR_WINDOW_TITLE = "Edit Variant Reading";

/**
 * A window control that allows the user to "correct" the reading in a base
 * text with the reading observed in a manuscript.  
 *  
 */
function EditVariantWindow(m_editor, kmap) {
    var m_id = m_editor.get('id');
    
    //========================================================================
    //  MEMBER VARIABLES
    //========================================================================
    
    /** The keyboard adapter attached to the input field of the window. */
    var m_inputAdapter = null;
    
    /** The id for the DOM element containing the EditVariantWindow body. */
    var m_bodyId = m_editor.get('id') + '_evw';
  
    var m_reading = null;
    
    //========================================================================
    //  PRIVATE METHODS
    //========================================================================
       
    /**
     * Cleans up after the window when it is closed, detaching event handlers
     * as needed and updating the status of the editor.
     * 
     * @method handleWindowClose
     * @private
     */
    function handleWindowClose(obj, editor) {
        m_reading = null;
        var el     = obj.el,
            window = obj.win,
            body   = $(m_bodyId),
            buttons = Selector.query("div.buttons input", body),
            
            resetButton  = buttons[0],
            cancelButton = buttons[1],
            okButton     = buttons[2];
        
        // detach UI elements
        if (m_inputAdapter) {
            m_inputAdapter.detach();
            m_inputAdapter = null;
        }
        
        Event.purgeElement(resetButton);
        Event.purgeElement(cancelButton);
        Event.purgeElement(okButton);
        Event.purgeElement(body, false /* recurse */, "keydown");
        
        // clear the selected node
        m_editor.currentElement = [];
        m_editor.nodeChange();
    }

    /**
     * Pre-renders the HTML to be displayed by the 'Edit Variant' window. This
     * method is attached to the 'renderWindow' event.
     * 
     * @method prepWindow
     * @private
     */
    function prepWindow() {
        m_editor._windows[PROP_COMMAND] = {
            body : $EL('div', null /* css class */, WINDOW_HTML, m_bodyId)
        };
    }
    
    /**
     * Creates a new Window object and passes it to the editor to be 
     * positioned and opened.
     * 
     * @method renderUI
     * @private
     */
    function renderUI() {
        var win = new Window(PROP_COMMAND, { width: '415px' });
        win.setHeader('Edit Variant');
        m_editor.openWindow(win);
    }
    
    /**
     * Synchronizes the Edit Variant window with a variant element in the
     * document. This is called after the window is displayed.
     * 
     * @method syncUI
     * @private
     * @param varianEl { HTMLElement } The element on the page that is 
     *      to be synced with the displayed window.
     */
    function syncUI() {
        var body      = $(m_bodyId),
            baseInput = Selector.query("input.base", body, true), 
            mssInput  = Selector.query("input.mss", body, true);
        
        baseInput.value = m_reading.getBaseReading();
        baseInput.disabled = true;

        mssInput.value = m_reading.getWitnessedReading();
        lang.later(100, m_editor, function() {
            mssInput.focus();
        });
    }
    
    /**
     * Binds the displayed window to event handlers that listen to user 
     * interactions.
     * 
     * @method bindUI
     * @priave
     */
    function bindUI() {
        var body = $(m_bodyId),
            mssInput  = Selector.query("input.mss", body, true),
            buttons = Selector.query("div.buttons input", body),
            resetButton  = buttons[0],
            cancelButton = buttons[1],
            okButton     = buttons[2];
        
        // attach key map adapter
        m_inputAdapter = kmap.attach(mssInput);
        
        /** Handles clicks on the OK button. */
        function onOK() {
            var text = that.getWitnessedReading();
            if (m_reading.isInsertion() && (text.trim().length == 0)) {
                // nothing to insert, delete the empty reading
                m_reading.clear();
            } else {
                m_reading.setWitnessedReading(text);
            }
            
            that.hide();
        }
        
        /** Handles clicks on the Cancel button. */
        function onCancel() { 
            if (m_reading._isNew())
                m_reading.clear();
            
            that.hide();
        }
        
        /** Handles clicks on the reset/remove button. */
        function onRemove() {
            m_reading.clear();
            that.hide();
         }
        
        /** Handles keyboard interactions. */
        function onKeypress(ev) {
            switch (ev.keyCode) {
            case KeyCodes.RET :
                Event.stopEvent(ev);
                onOK();
                break;
            case KeyCodes.ESC :
                Event.stopEvent(ev);
                onCancel();
                break;
            default: break; /* do nothing */
            }
        }
        
        Event.on(resetButton, "click", onRemove);
        Event.on(cancelButton, "click", onCancel);
        Event.on(okButton, "click", onOK);
        Event.on(body, "keydown", onKeypress, this, true);
    }
    
    //========================================================================
    // MAIN WINDOW OBJECT
    //========================================================================
    
    that = {
        /** 
         * Returns the text of the base reading.
         * 
         * @method getBaseReading
         * @public
         * @return { String } The text of the base reading.
         */
        getBaseReading : function() {
            var baseInput = Selector.query("input." + CSS_BASE, $(m_bodyId), true);
            return (baseInput) ? baseInput.value : ""; 
        },
        
        /** 
         * Returns the text entered for the witnessed reading.
         * 
         * @method getWitnessedReading
         * @public
         * @return { String } The text entered for the witnessed reading.
         */
        getWitnessedReading : function() {
            var witInput = Selector.query("input.mss", $(m_bodyId), true);
            return (witInput) ? witInput.value : ""; 
        },
        
        show : function(reading) {
            var el = reading.getElement();
            m_reading = reading;
            if (!lang.isValue(el)) {
                m_reading = null;
                return;
            }
            
            // display the window
            renderUI();
            syncUI();
            bindUI();
        },
        
        hide : function() {
            if (m_reading != null)
                m_editor.closeWindow();
        },
        
        destroy : function() {
            // TODO implement
        }
    };
    
    m_editor.on(WINDOW_CLOSE_EVENT,  handleWindowClose, m_editor, true);
    m_editor.on(WINDOW_RENDER_EVENT, prepWindow);
    
    return that;
}

IDCH.nt.TCEditor          = TCEditor;
IDCH.nt.VariantReading    = VariantReading;
IDCH.nt.EditVariantWindow = EditVariantWindow;
})();