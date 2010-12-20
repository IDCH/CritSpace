
IDCH.namespace("nt");
(function() {
    if (IDCH.nt.BASE_TEXT_PANEL)
        return;
    
    var lang  = YAHOO.lang,
        dom   = YAHOO.util.Dom,
        util  = YAHOO.util,
        JSON  = lang.JSON,

        Event = YAHOO.util.Event,
        Selector = YAHOO.util.Selector,

        Workspace     = IDCH.critspace.Workspace,
        PanelRegistry = IDCH.critspace.PanelRegistry,
        ImageScroller = IDCH.images.ImageScroller,
        ImageProxy    = IDCH.images.ImageProxy;
            
    var LOGGER = "IDCH.nt.BaseText",
        PANEL_TYPE = "org.idch.nt.BaseText";
        
    var EXAMPLE_TEXT = 
        '<span id="jn1_1">εν αρχη ην ο λογος και ο λογος ην προς τον θεον και θεος ην ο λογος </span>' + 
        '<span id="jn1_2">ουτος ην εν αρχη προς τον θεον </span>' + 
        '<span id="jn1_3">παντα δι αυτου εγενετο και χωρις αυτου εγενετο ουδε εν ο γεγονεν </span>' + 
        '<span id="jn1_4">εν αυτω ζωη ην και η ζωη ην το φως των ανθρωπων </span>' + 
        '<span id="jn1_5">και το φως εν τη σκοτια φαινει και η σκοτια αυτο ου κατελαβεν </span>' + 
        '<span id="jn1_6">εγενετο ανθρωπος απεσταλμενος παρα θεου ονομα αυτω ιωαννης </span>' + 
        '<span id="jn1_7">ουτος ηλθεν εις μαρτυριαν ινα μαρτυρηση περι του φωτος ινα παντες πιστευσωσιν δι αυτου </span>' + 
        '<span id="jn1_8">ουκ ην εκεινος το φως αλλ ινα μαρτυρηση περι του φωτος </span>' + 
        '<span id="jn1_9">ην το φως το αληθινον ο φωτιζει παντα ανθρωπον ερχομενον εις τον κοσμον </span>' + 
        '<span id="jn1_10">εν τω κοσμω ην και ο κοσμος δι αυτου εγενετο και ο κοσμος αυτον ουκ εγνω </span>' + 
        '<span id="jn1_11">εις τα ιδια ηλθεν και οι ιδιοι αυτον ου παρελαβον </span>' + 
        '<span id="jn1_12">οσοι δε ελαβον αυτον εδωκεν αυτοις εξουσιαν τεκνα θεου γενεσθαι τοις πιστευουσιν εις το ονομα αυτου </span>' + 
        '<span id="jn1_13">οι ουκ εξ αιματων ουδε εκ θεληματος σαρκος ουδε εκ θεληματος ανδρος αλλ εκ θεου εγεννηθησαν </span>' + 
        '<span id="jn1_14">και ο λογος σαρξ εγενετο και εσκηνωσεν εν ημιν και εθεασαμεθα την δοξαν αυτου δοξαν ως μονογενους παρα πατρος πληρης χαριτος και αληθειας </span>' + 
        '<span id="jn1_15">ιωαννης μαρτυρει περι αυτου και κεκραγεν λεγων ουτος ην ον ειπον ο οπισω μου ερχομενος εμπροσθεν μου γεγονεν οτι πρωτος μου ην </span>' + 
        '<span id="jn1_16">οτι εκ του πληρωματος αυτου ημεις παντες ελαβομεν και χαριν αντι χαριτος </span>' + 
        '<span id="jn1_17">οτι ο νομος δια μωυσεως εδοθη η χαρις και η αληθεια δια ιησου χριστου εγενετο </span>' + 
        '<span id="jn1_18">θεον ουδεις εωρακεν πωποτε μονογενης θεος ο ων εις τον κολπον του πατρος εκεινος εξηγησατο </span>' + 
        '<span id="jn1_19">και αυτη εστιν η μαρτυρια του ιωαννου οτε απεστειλαν [προς αυτον] οι ιουδαιοι εξ ιεροσολυμων ιερεις και λευιτας ινα ερωτησωσιν αυτον συ τις ει </span>' + 
        '<span id="jn1_20">και ωμολογησεν και ουκ ηρνησατο και ωμολογησεν οτι εγω ουκ ειμι ο χριστος </span>' + 
        '<span id="jn1_21">και ηρωτησαν αυτον τι ουν συ ηλιας ει και λεγει ουκ ειμι ο προφητης ει συ και απεκριθη ου </span>' + 
        '<span id="jn1_22">ειπαν ουν αυτω τις ει ινα αποκρισιν δωμεν τοις πεμψασιν ημας τι λεγεις περι σεαυτου </span>' + 
        '<span id="jn1_23">εφη εγω φωνη βοωντος εν τη ερημω ευθυνατε την οδον κυριου καθως ειπεν ησαιας ο προφητης </span>' + 
        '<span id="jn1_24">και απεσταλμενοι ησαν εκ των φαρισαιων </span>' + 
        '<span id="jn1_25">και ηρωτησαν αυτον και ειπαν αυτω τι ουν βαπτιζεις ει συ ουκ ει ο χριστος ουδε ηλιας ουδε ο προφητης </span>' + 
        '<span id="jn1_26">απεκριθη αυτοις ο ιωαννης λεγων εγω βαπτιζω εν υδατι μεσος υμων εστηκεν ον υμεις ουκ οιδατε </span>' + 
        '<span id="jn1_27">ο οπισω μου ερχομενος ου ουκ ειμι [εγω] αξιος ινα λυσω αυτου τον ιμαντα του υποδηματος </span>' + 
        '<span id="jn1_28">ταυτα εν βηθανια εγενετο περαν του ιορδανου οπου ην ο ιωαννης βαπτιζων </span>' + 
        '<span id="jn1_29">τη επαυριον βλεπει τον ιησουν ερχομενον προς αυτον και λεγει ιδε ο αμνος του θεου ο αιρων την αμαρτιαν του κοσμου </span>' + 
        '<span id="jn1_30">ουτος εστιν υπερ ου εγω ειπον οπισω μου ερχεται ανηρ ος εμπροσθεν μου γεγονεν οτι πρωτος μου ην </span>' + 
        '<span id="jn1_31">καγω ουκ ηδειν αυτον αλλ ινα φανερωθη τω ισραηλ δια τουτο ηλθον εγω εν υδατι βαπτιζων </span>' + 
        '<span id="jn1_32">και εμαρτυρησεν ιωαννης λεγων οτι τεθεαμαι το πνευμα καταβαινον ως περιστεραν εξ ουρανου και εμεινεν επ αυτον </span>' + 
        '<span id="jn1_33">καγω ουκ ηδειν αυτον αλλ ο πεμψας με βαπτιζειν εν υδατι εκεινος μοι ειπεν εφ ον αν ιδης το πνευμα καταβαινον και μενον επ αυτον ουτος εστιν ο βαπτιζων εν πνευματι αγιω </span>' + 
        '<span id="jn1_34">καγω εωρακα και μεμαρτυρηκα οτι ουτος εστιν ο υιος του θεου </span>' + 
        '<span id="jn1_35">τη επαυριον παλιν ειστηκει ο ιωαννης και εκ των μαθητων αυτου δυο </span>' + 
        '<span id="jn1_36">και εμβλεψας τω ιησου περιπατουντι λεγει ιδε ο αμνος του θεου </span>' + 
        '<span id="jn1_37">και ηκουσαν οι δυο μαθηται αυτου λαλουντος και ηκολουθησαν τω ιησου </span>' + 
        '<span id="jn1_38">στραφεις δε ο ιησους και θεασαμενος αυτους ακολουθουντας λεγει αυτοις τι ζητειτε οι δε ειπαν αυτω ραββι ο λεγεται μεθερμηνευομενον διδασκαλε που μενεις </span>' + 
        '<span id="jn1_39">λεγει αυτοις ερχεσθε και οψεσθε ηλθαν ουν και ειδαν που μενει και παρ αυτω εμειναν την ημεραν εκεινην ωρα ην ως δεκατη </span>' + 
        '<span id="jn1_40">ην ανδρεας ο αδελφος σιμωνος πετρου εις εκ των δυο των ακουσαντων παρα ιωαννου και ακολουθησαντων αυτω </span>' + 
        '<span id="jn1_41">ευρισκει ουτος πρωτον τον αδελφον τον ιδιον σιμωνα και λεγει αυτω ευρηκαμεν τον μεσσιαν ο εστιν μεθερμηνευομενον χριστος </span>' + 
        '<span id="jn1_42">ηγαγεν αυτον προς τον ιησουν εμβλεψας αυτω ο ιησους ειπεν συ ει σιμων ο υιος ιωαννου συ κληθηση κηφας ο ερμηνευεται πετρος </span>' + 
        '<span id="jn1_43">τη επαυριον ηθελησεν εξελθειν εις την γαλιλαιαν και ευρισκει φιλιππον και λεγει αυτω ο ιησους ακολουθει μοι </span>' + 
        '<span id="jn1_44">ην δε ο φιλιππος απο βηθσαιδα εκ της πολεως ανδρεου και πετρου </span>' + 
        '<span id="jn1_45">ευρισκει φιλιππος τον ναθαναηλ και λεγει αυτω ον εγραψεν μωυσης εν τω νομω και οι προφηται ευρηκαμεν ιησουν υιον του ιωσηφ τον απο ναζαρετ </span>' + 
        '<span id="jn1_46">και ειπεν αυτω ναθαναηλ εκ ναζαρετ δυναται τι αγαθον ειναι λεγει αυτω [ο] φιλιππος ερχου και ιδε </span>' + 
        '<span id="jn1_47">ειδεν ο ιησους τον ναθαναηλ ερχομενον προς αυτον και λεγει περι αυτου ιδε αληθως ισραηλιτης εν ω δολος ουκ εστιν </span>' + 
        '<span id="jn1_48">λεγει αυτω ναθαναηλ ποθεν με γινωσκεις απεκριθη ιησους και ειπεν αυτω προ του σε φιλιππον φωνησαι οντα υπο την συκην ειδον σε </span>' + 
        '<span id="jn1_49">απεκριθη αυτω ναθαναηλ ραββι συ ει ο υιος του θεου συ βασιλευς ει του ισραηλ </span>' + 
        '<span id="jn1_50">απεκριθη ιησους και ειπεν αυτω οτι ειπον σοι οτι ειδον σε υποκατω της συκης πιστευεις μειζω τουτων οψη </span>' + 
        '<span id="jn1_51">και λεγει αυτω αμην αμην λεγω υμιν οψεσθε τον ουρανον ανεωγοτα και τους αγγελους του θεου αναβαινοντας και καταβαινοντας επι τον υιον του ανθρωπου</span>';
    
    

  
  //========================================================================= 
  // PANEL IMPLEMENTATION
  //=========================================================================
  

/** 
 * 
 * 
 * @class BaseText
 * @namespace IDCH.nt
 * @private
 * @constructor
 * 
 * @param m_panel { Panel } The base panel objecjt that this method extends.
 * @param m_cfg { Object } The user supplied configuration object.
 */
function BaseText(m_panel, m_cfg) {
    //========================================================================= 
    // PRIVATE VARIABLES
    //=========================================================================
        
    var m_body =  m_panel.getBody();
    
    var m_content = null;
    
    // TODO need a smarter way to structure this 
    var m_text = m_cfg.props.text || EXAMPLE_TEXT;
    
    var m_animDuration = m_cfg.props.animDuration || 0.5;
    
    var m_currentReference = {
        book : "jn",
        chapter : 1,
        verse : 1
    };
    
    //========================================================================= 
    // RENDERING AND EVENT LISTENING METHODS
    //=========================================================================

    function attachListeners() {
        // TODO need to update height dynamically based on panel size & border
        //      width
//        Event.on(m_body, "mousedown", function(e) {
//            Event.stopEvent(e);
//            m_panel.focus();
//        });
//        
//        Event.on(m_body, "click", function(e) {
//            Event.stopEvent(e);
//            m_panel.focus();
//        });
    }
      
    function renderText() {
        var html = '<div id="test_drag" class="blockdrag basetext">' + m_text + '</div>'; 
                   
        m_body.innerHTML = html;
        m_panel.dd.addInvalidHandleClass("blockdrag");
        m_panel.dd.addInvalidHandleType("span");
        
        m_content = Selector.query("div", m_body, true)
    }

    //========================================================================= 
    // DISPLAY MANIPULATION METHODS
    //=========================================================================

    function getSelectedText() {
        return (window.getSelection) ? window.getSelection().toString() 
                : (document.getSelection) ? document.getSelection() 
                : (document.selection) ? document.selection.createRange().text 
                : null;
    }
    
    function getCurrentRefId() {
        var r = m_currentReference;
        return "#" + r.book + r.chapter + "_" + r.verse;
    }
    
    /** 
     * Indicates whether or not the current scripture reference is valid.
     * 
     */
    function isCurrentRefValid() {
        // TODO IMPLEMENT ME
        
    }
    
    function onClick() {
        
    }
    
    function setReference(verse, chapter, book) {
        var r = m_currentReference;
        
        if (lang.isNumber(verse))   r.verse = verse; 
        if (lang.isNumber(chapter)) r.chapter = chapter;
        if (lang.isString(book))    r.book = book;
        
        // FIRE VERSE UPDATED
    }
    
    function scrollTo(verse, chapter, book) {
        var r = m_currentReference,
            book = lang.isString(book) ? book : r.book,
            chpt = lang.isNumber(chapter) ? chapter : r.chapter,
            vs   = lang.isNumber(verse) ? verse : r.verse,
        
            id = "#" + book + chpt + "_" + vs,
            vsSpan = Selector.query(id, m_body, true),
            scrollAttrib = { 
                scroll: { to: [0, vsSpan.offsetTop] },
                duration : 0.25
            },
            
            anim = new YAHOO.util.Scroll(m_content, scrollAttrib); 
        
        anim.duration = m_animDuration;
        anim.animate();
    }
    
    //========================================================================= 
    // DOMAIN MODEL INTERACTION METHODS
    //=========================================================================

    /**
     * @param book { String } The two letter book code.
     */
    function getVerse(book, chapter, verse) {
        var id = book + chapter + "_" + verse,
            el = $(id);
        
        return el;
    }
    
    function setText(text) {
        m_text = text;
//        m_panel.setNamedProperty();
        
        // FIRE setText event
    }
      
    function initialize() {
        m_panel.addClass("bt-panel");
        m_panel.configure(m_cfg);
        
        renderText();
        attachListeners();
          
        lang.later(1000, null, function() {
            scrollTo(16);
        })
    }
      

    initialize();
    return m_panel;
}

  // PUBLICIZE & ATTACH TO WORKSPACE

  /**
   * Symbolic constant for the TextPanel type. 
   * @property BASE_TEXT_PANEL
   * @public
   */
  IDCH.nt.BASE_TEXT_PANEL = PANEL_TYPE;

  // finally, register this panel constructor with the workspace
  IDCH.critspace.PanelRegistry.register(PANEL_TYPE, BaseText);
})();
