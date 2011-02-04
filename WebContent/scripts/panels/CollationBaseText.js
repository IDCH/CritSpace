
IDCH.namespace("nt");
(function() {
    if (IDCH.nt.BASE_TEXT_PANEL)
        return;
    
    var lang  = YAHOO.lang,
        dom   = YAHOO.util.Dom,
        util  = YAHOO.util,
        JSON  = lang.JSON,

        Event = YAHOO.util.Event,
        EventProvider = YAHOO.util.EventProvider,
        Selector = YAHOO.util.Selector,

        Workspace     = IDCH.critspace.Workspace,
        PanelRegistry = IDCH.critspace.PanelRegistry,
        PanelLink     = IDCH.critspace.PanelLink,
        ImageScroller = IDCH.images.ImageScroller,
        ImageProxy    = IDCH.images.ImageProxy,
        TCEditor      = IDCH.nt.TCEditor;
            
    var LOGGER = "IDCH.nt.BaseText",
        PANEL_TYPE = "org.idch.nt.BaseText";
        
    var EXAMPLE_TEXT = 
        '<h2>ΚΑΤΑ ΛΟΥΚΑΝ</h2>' +
        '<div class="book" id="Luke">' +

        '  <div class="chapter" id="Luke.1" >' +
        '  <h3 >Chapter 1</h3>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">1</span><span class="verse" id="Luke.1.1">Ἐπειδήπερ πολλοὶ ἐπεχείρησαν ἀνατάξασθαι διήγησιν περὶ τῶν πεπληροφορημένων ἐν ἡμῖν πραγμάτων,</span>' +  
        '    <span class="verseId">2</span><span class="verse" id="Luke.1.2">καθὼς παρέδοσαν ἡμῖν οἱ ἀπ&rsquo; ἀρχῆς αὐτόπται καὶ ὑπηρέται γενόμενοι τοῦ λόγου,</span>' +  
        '    <span class="verseId">3</span><span class="verse" id="Luke.1.3">ἔδοξε κἀμοὶ παρηκολουθηκότι ἄνωθεν πᾶσιν ἀκριβῶς καθεξῆς σοι γράψαι,  κράτιστε Θεόφιλε,</span>' +  
        '    <span class="verseId">4</span><span class="verse" id="Luke.1.4">ἵνα ἐπιγνῷς περὶ ὧν κατηχήθης λόγων τὴν ἀσφάλειαν.</span>' +  
        '    ' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">5</span><span class="verse" id="Luke.1.5">Ἐγένετο ἐν ταῖς ἡμέραις Ἡρῴδου βασιλέως τῆς Ἰουδαίας ἱερεύς τις ὀνόματι Ζαχαρίας ἐξ ἐφημερίας Ἀβιά,  καὶ γυνὴ αὐτῷ ἐκ τῶν θυγατέρων Ἀαρών,  καὶ τὸ ὄνομα αὐτῆς Ἐλισάβετ.</span>' +  
        '    <span class="verseId">6</span><span class="verse" id="Luke.1.6">ἦσαν δὲ δίκαιοι ἀμφότεροι ἐναντίον τοῦ θεοῦ,  πορευόμενοι ἐν πάσαις ταῖς ἐντολαῖς καὶ δικαιώμασιν τοῦ κυρίου ἄμεμπτοι.</span>' +  
        '    <span class="verseId">7</span><span class="verse" id="Luke.1.7">καὶ οὐκ ἦν αὐτοῖς τέκνον,  καθότι ἦν ἡ Ἐλισάβετ⸃  στεῖρα,  καὶ ἀμφότεροι προβεβηκότες ἐν ταῖς ἡμέραις αὐτῶν ἦσαν.</span>' +  
        '    ' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">8</span><span class="verse" id="Luke.1.8">Ἐγένετο δὲ ἐν τῷ ἱερατεύειν αὐτὸν ἐν τῇ τάξει τῆς ἐφημερίας αὐτοῦ ἔναντι τοῦ θεοῦ</span>' + 
        '    <span class="verseId">9</span><span class="verse" id="Luke.1.9">κατὰ τὸ ἔθος τῆς ἱερατείας ἔλαχε τοῦ θυμιᾶσαι εἰσελθὼν εἰς τὸν ναὸν τοῦ κυρίου,</span>' +  
        '    <span class="verseId">10</span><span class="verse" id="Luke.1.10">καὶ πᾶν τὸ πλῆθος ἦν τοῦ λαοῦ προσευχόμενον ἔξω τῇ ὥρᾳ τοῦ θυμιάματος&middot;</span>' +
        '    <span class="verseId">11</span><span class="verse" id="Luke.1.11">ὤφθη δὲ αὐτῷ ἄγγελος κυρίου ἑστὼς ἐκ δεξιῶν τοῦ θυσιαστηρίου τοῦ θυμιάματος.</span>' +  
        '    <span class="verseId">12</span><span class="verse" id="Luke.1.12">καὶ ἐταράχθη Ζαχαρίας ἰδών,  καὶ φόβος ἐπέπεσεν ἐπ&rsquo; αὐτόν.</span>' +
        '    <span class="verseId">13</span><span class="verse" id="Luke.1.13">εἶπεν δὲ πρὸς αὐτὸν ὁ ἄγγελος&middot;  Μὴ φοβοῦ,  Ζαχαρία,  διότι εἰσηκούσθη ἡ δέησίς σου,  καὶ ἡ γυνή σου Ἐλισάβετ γεννήσει υἱόν σοι,  καὶ καλέσεις τὸ ὄνομα αὐτοῦ Ἰωάννην&middot;</span>' +  
        '    <span class="verseId">14</span><span class="verse" id="Luke.1.14">καὶ ἔσται χαρά σοι καὶ ἀγαλλίασις,  καὶ πολλοὶ ἐπὶ τῇ γενέσει αὐτοῦ χαρήσονται&middot;</span>' +
        '    <span class="verseId">15</span><span class="verse" id="Luke.1.15">ἔσται γὰρ μέγας ἐνώπιον τοῦ κυρίου,  καὶ οἶνον καὶ σίκερα οὐ μὴ πίῃ,  καὶ πνεύματος ἁγίου πλησθήσεται ἔτι ἐκ κοιλίας μητρὸς αὐτοῦ,</span>' +  
        '    <span class="verseId">16</span><span class="verse" id="Luke.1.16">καὶ πολλοὺς τῶν υἱῶν Ἰσραὴλ ἐπιστρέψει ἐπὶ κύριον τὸν θεὸν αὐτῶν&middot;</span>' +
        '    <span class="verseId">17</span><span class="verse" id="Luke.1.17">καὶ αὐτὸς προελεύσεται ἐνώπιον αὐτοῦ ἐν πνεύματι καὶ δυνάμει Ἠλίου,  ἐπιστρέψαι καρδίας πατέρων ἐπὶ τέκνα καὶ ἀπειθεῖς ἐν φρονήσει δικαίων,  ἑτοιμάσαι κυρίῳ λαὸν κατεσκευασμένον.</span>' +  
        '    <span class="verseId">18</span><span class="verse" id="Luke.1.18">καὶ εἶπεν Ζαχαρίας πρὸς τὸν ἄγγελον&middot;  Κατὰ τί γνώσομαι τοῦτο;  ἐγὼ γάρ εἰμι πρεσβύτης καὶ ἡ γυνή μου προβεβηκυῖα ἐν ταῖς ἡμέραις αὐτῆς.</span>' +
        '    <span class="verseId">19</span><span class="verse" id="Luke.1.19">καὶ ἀποκριθεὶς ὁ ἄγγελος εἶπεν αὐτῷ&middot;  Ἐγώ εἰμι Γαβριὴλ ὁ παρεστηκὼς ἐνώπιον τοῦ θεοῦ,  καὶ ἀπεστάλην λαλῆσαι πρὸς σὲ καὶ εὐαγγελίσασθαί σοι ταῦτα&middot;</span>' +
        '    <span class="verseId">20</span><span class="verse" id="Luke.1.20">καὶ ἰδοὺ ἔσῃ σιωπῶν καὶ μὴ δυνάμενος λαλῆσαι ἄχρι ἧς ἡμέρας γένηται ταῦτα,  ἀνθ&rsquo; ὧν οὐκ ἐπίστευσας τοῖς λόγοις μου,  οἵτινες πληρωθήσονται εἰς τὸν καιρὸν αὐτῶν.</span>' +  
        '    ' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">21</span><span class="verse" id="Luke.1.21">Καὶ ἦν ὁ λαὸς προσδοκῶν τὸν Ζαχαρίαν,  καὶ ἐθαύμαζον ἐν τῷ χρονίζειν ἐν τῷ ναῷ αὐτόν⸃.</span>' +  
        '    <span class="verseId">22</span><span class="verse" id="Luke.1.22">ἐξελθὼν δὲ οὐκ ἐδύνατο λαλῆσαι αὐτοῖς,  καὶ ἐπέγνωσαν ὅτι ὀπτασίαν ἑώρακεν ἐν τῷ ναῷ&middot;  καὶ αὐτὸς ἦν διανεύων αὐτοῖς,  καὶ διέμενεν κωφός.</span>' +  
        '    <span class="verseId">23</span><span class="verse" id="Luke.1.23">καὶ ἐγένετο ὡς ἐπλήσθησαν αἱ ἡμέραι τῆς λειτουργίας αὐτοῦ,  ἀπῆλθεν εἰς τὸν οἶκον αὐτοῦ.</span>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">24</span><span class="verse" id="Luke.1.24">Μετὰ δὲ ταύτας τὰς ἡμέρας συνέλαβεν Ἐλισάβετ ἡ γυνὴ αὐτοῦ&middot;  καὶ περιέκρυβεν ἑαυτὴν μῆνας πέντε,  λέγουσα</span>' + 
        '    <span class="verseId">25</span><span class="verse" id="Luke.1.25">ὅτι Οὕτως μοι πεποίηκεν κύριος ἐν ἡμέραις αἷς ἐπεῖδεν ἀφελεῖν ὄνειδός μου ἐν ἀνθρώποις.</span>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">26</span><span class="verse" id="Luke.1.26">Ἐν δὲ τῷ μηνὶ τῷ ἕκτῳ ἀπεστάλη ὁ ἄγγελος Γαβριὴλ ἀπὸ τοῦ θεοῦ εἰς πόλιν τῆς Γαλιλαίας ᾗ ὄνομα Ναζαρὲθ</span>' +
        '    <span class="verseId">27</span><span class="verse" id="Luke.1.27">πρὸς παρθένον ἐμνηστευμένην ἀνδρὶ ᾧ ὄνομα Ἰωσὴφ ἐξ οἴκου Δαυὶδ,  καὶ τὸ ὄνομα τῆς παρθένου Μαριάμ.</span>' +  
        '    <span class="verseId">28</span><span class="verse" id="Luke.1.28">καὶ εἰσελθὼν πρὸς αὐτὴν εἶπεν&middot;  Χαῖρε,  κεχαριτωμένη,  ὁ κύριος μετὰ σοῦ.</span>' +  
        '    <span class="verseId">29</span><span class="verse" id="Luke.1.29">ἡ δὲ ἐπὶ τῷ λόγῳ διεταράχθη⸃  καὶ διελογίζετο ποταπὸς εἴη ὁ ἀσπασμὸς οὗτος.</span>' +  
        '    <span class="verseId">30</span><span class="verse" id="Luke.1.30">καὶ εἶπεν ὁ ἄγγελος αὐτῇ&middot;  Μὴ φοβοῦ,  Μαριάμ,  εὗρες γὰρ χάριν παρὰ τῷ θεῷ&middot;</span>' +  
        '    <span class="verseId">31</span><span class="verse" id="Luke.1.31">καὶ ἰδοὺ συλλήμψῃ ἐν γαστρὶ καὶ τέξῃ υἱόν,  καὶ καλέσεις τὸ ὄνομα αὐτοῦ Ἰησοῦν.</span>' +  
        '    <span class="verseId">32</span><span class="verse" id="Luke.1.32">οὗτος ἔσται μέγας καὶ υἱὸς Ὑψίστου κληθήσεται,  καὶ δώσει αὐτῷ κύριος ὁ θεὸς τὸν θρόνον Δαυὶδ τοῦ πατρὸς αὐτοῦ,</span>' +  
        '    <span class="verseId">33</span><span class="verse" id="Luke.1.33">καὶ βασιλεύσει ἐπὶ τὸν οἶκον Ἰακὼβ εἰς τοὺς αἰῶνας,  καὶ τῆς βασιλείας αὐτοῦ οὐκ ἔσται τέλος.</span>' +  
        '    <span class="verseId">34</span><span class="verse" id="Luke.1.34">εἶπεν δὲ Μαριὰμ πρὸς τὸν ἄγγελον&middot;  Πῶς ἔσται τοῦτο,  ἐπεὶ ἄνδρα οὐ γινώσκω;</span>' +  
        '    <span class="verseId">35</span><span class="verse" id="Luke.1.35">καὶ ἀποκριθεὶς ὁ ἄγγελος εἶπεν αὐτῇ&middot;  Πνεῦμα ἅγιον ἐπελεύσεται ἐπὶ σέ,  καὶ δύναμις Ὑψίστου ἐπισκιάσει σοι&middot;  διὸ καὶ τὸ γεννώμενον ἅγιον κληθήσεται,  υἱὸς θεοῦ&middot;</span>' +  
        '    <span class="verseId">36</span><span class="verse" id="Luke.1.36">καὶ ἰδοὺ Ἐλισάβετ ἡ συγγενίς σου καὶ αὐτὴ συνείληφεν υἱὸν ἐν γήρει αὐτῆς,  καὶ οὗτος μὴν ἕκτος ἐστὶν αὐτῇ τῇ καλουμένῃ στείρᾳ&middot;</span>' +  
        '    <span class="verseId">37</span><span class="verse" id="Luke.1.37">ὅτι οὐκ ἀδυνατήσει παρὰ τοῦ θεοῦ⸃  πᾶν ῥῆμα.</span>' +  
        '    <span class="verseId">38</span><span class="verse" id="Luke.1.38">εἶπεν δὲ Μαριάμ&middot;  Ἰδοὺ ἡ δούλη κυρίου&middot;  γένοιτό μοι κατὰ τὸ ῥῆμά σου.  καὶ ἀπῆλθεν ἀπ&rsquo; αὐτῆς ὁ ἄγγελος.</span>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">39</span><span class="verse" id="Luke.1.39">Ἀναστᾶσα δὲ Μαριὰμ ἐν ταῖς ἡμέραις ταύταις ἐπορεύθη εἰς τὴν ὀρεινὴν μετὰ σπουδῆς εἰς πόλιν Ἰούδα,</span>' +  
        '    <span class="verseId">40</span><span class="verse" id="Luke.1.40">καὶ εἰσῆλθεν εἰς τὸν οἶκον Ζαχαρίου καὶ ἠσπάσατο τὴν Ἐλισάβετ.</span>' +  
        '    <span class="verseId">41</span><span class="verse" id="Luke.1.41">καὶ ἐγένετο ὡς ἤκουσεν τὸν ἀσπασμὸν τῆς Μαρίας ἡ Ἐλισάβετ⸃,  ἐσκίρτησεν τὸ βρέφος ἐν τῇ κοιλίᾳ αὐτῆς,  καὶ ἐπλήσθη πνεύματος ἁγίου ἡ Ἐλισάβετ,</span>' +  
        '    <span class="verseId">42</span><span class="verse" id="Luke.1.42">καὶ ἀνεφώνησεν κραυγῇ μεγάλῃ καὶ εἶπεν&middot;  Εὐλογημένη σὺ ἐν γυναιξίν,  καὶ εὐλογημένος ὁ καρπὸς τῆς κοιλίας σου.</span>' +  
        '    <span class="verseId">43</span><span class="verse" id="Luke.1.43">καὶ πόθεν μοι τοῦτο ἵνα ἔλθῃ ἡ μήτηρ τοῦ κυρίου μου πρὸς ἐμέ;</span>' +  
        '    <span class="verseId">44</span><span class="verse" id="Luke.1.44">ἰδοὺ γὰρ ὡς ἐγένετο ἡ φωνὴ τοῦ ἀσπασμοῦ σου εἰς τὰ ὦτά μου,  ἐσκίρτησεν ἐν ἀγαλλιάσει τὸ βρέφος⸃  ἐν τῇ κοιλίᾳ μου.</span>' +  
        '    <span class="verseId">45</span><span class="verse" id="Luke.1.45">καὶ μακαρία ἡ πιστεύσασα ὅτι ἔσται τελείωσις τοῖς λελαλημένοις αὐτῇ παρὰ κυρίου.</span>' +
        '    ' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">46</span><span class="verse" id="Luke.1.46">Καὶ εἶπεν Μαριάμ&middot;  Μεγαλύνει ἡ ψυχή μου τὸν κύριον,</span>' +  
        '    <span class="verseId">47</span><span class="verse" id="Luke.1.47">καὶ ἠγαλλίασεν τὸ πνεῦμά μου ἐπὶ τῷ θεῷ τῷ σωτῆρί μου&middot;</span>' +  
        '    <span class="verseId">48</span><span class="verse" id="Luke.1.48">ὅτι ἐπέβλεψεν ἐπὶ τὴν ταπείνωσιν τῆς δούλης αὐτοῦ,  ἰδοὺ γὰρ ἀπὸ τοῦ νῦν μακαριοῦσίν με πᾶσαι αἱ γενεαί&middot;</span>' +  
        '    <span class="verseId">49</span><span class="verse" id="Luke.1.49">ὅτι ἐποίησέν μοι μεγάλα ὁ δυνατός,  καὶ ἅγιον τὸ ὄνομα αὐτοῦ,</span>' +  
        '    <span class="verseId">50</span><span class="verse" id="Luke.1.50">καὶ τὸ ἔλεος αὐτοῦ εἰς γενεὰς καὶ γενεὰς⸃  τοῖς φοβουμένοις αὐτόν.</span>' +  
        '    <span class="verseId">51</span><span class="verse" id="Luke.1.51">Ἐποίησεν κράτος ἐν βραχίονι αὐτοῦ,  διεσκόρπισεν ὑπερηφάνους διανοίᾳ καρδίας αὐτῶν&middot;</span>' +  
        '    <span class="verseId">52</span><span class="verse" id="Luke.1.52">καθεῖλεν δυνάστας ἀπὸ θρόνων καὶ ὕψωσεν ταπεινούς,</span>' +  
        '    <span class="verseId">53</span><span class="verse" id="Luke.1.53">πεινῶντας ἐνέπλησεν ἀγαθῶν καὶ πλουτοῦντας ἐξαπέστειλεν κενούς.</span>' +  
        '    <span class="verseId">54</span><span class="verse" id="Luke.1.54">ἀντελάβετο Ἰσραὴλ παιδὸς αὐτοῦ,  μνησθῆναι ἐλέους,</span>' +  
        '    <span class="verseId">55</span><span class="verse" id="Luke.1.55">καθὼς ἐλάλησεν πρὸς τοὺς πατέρας ἡμῶν,  τῷ Ἀβραὰμ καὶ τῷ σπέρματι αὐτοῦ εἰς τὸν αἰῶνα.</span>' +  
        '    <span class="verseId">56</span><span class="verse" id="Luke.1.56">Ἔμεινεν δὲ Μαριὰμ σὺν αὐτῇ ὡς μῆνας τρεῖς,  καὶ ὑπέστρεψεν εἰς τὸν οἶκον αὐτῆς.</span>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">57</span><span class="verse" id="Luke.1.57">Τῇ δὲ Ἐλισάβετ ἐπλήσθη ὁ χρόνος τοῦ τεκεῖν αὐτήν,  καὶ ἐγέννησεν υἱόν.</span>' +
        '    <span class="verseId">58</span><span class="verse" id="Luke.1.58">καὶ ἤκουσαν οἱ περίοικοι καὶ οἱ συγγενεῖς αὐτῆς ὅτι ἐμεγάλυνεν κύριος τὸ ἔλεος αὐτοῦ μετ&rsquo; αὐτῆς,  καὶ συνέχαιρον αὐτῇ.</span>' +
        '    <span class="verseId">59</span><span class="verse" id="Luke.1.59">Καὶ ἐγένετο ἐν τῇ ἡμέρᾳ τῇ ὀγδόῃ⸃  ἦλθον περιτεμεῖν τὸ παιδίον,  καὶ ἐκάλουν αὐτὸ ἐπὶ τῷ ὀνόματι τοῦ πατρὸς αὐτοῦ Ζαχαρίαν.</span>' +  
        '    <span class="verseId">60</span><span class="verse" id="Luke.1.60">καὶ ἀποκριθεῖσα ἡ μήτηρ αὐτοῦ εἶπεν&middot;  Οὐχί,  ἀλλὰ κληθήσεται Ἰωάννης.</span>' +
        '    <span class="verseId">61</span><span class="verse" id="Luke.1.61">καὶ εἶπαν πρὸς αὐτὴν ὅτι Οὐδείς ἐστιν ἐκ τῆς συγγενείας⸃  σου ὃς καλεῖται τῷ ὀνόματι τούτῳ.</span>' +  
        '    <span class="verseId">62</span><span class="verse" id="Luke.1.62">ἐνένευον δὲ τῷ πατρὶ αὐτοῦ τὸ τί ἂν θέλοι καλεῖσθαι αὐτό.</span>' +  
        '    <span class="verseId">63</span><span class="verse" id="Luke.1.63">καὶ αἰτήσας πινακίδιον ἔγραψεν λέγων&middot;  Ἰωάννης ἐστὶν ὄνομα αὐτοῦ.  καὶ ἐθαύμασαν πάντες.</span>' +  
        '    <span class="verseId">64</span><span class="verse" id="Luke.1.64">ἀνεῴχθη δὲ τὸ στόμα αὐτοῦ παραχρῆμα καὶ ἡ γλῶσσα αὐτοῦ,  καὶ ἐλάλει εὐλογῶν τὸν θεόν.</span>' +  
        '    <span class="verseId">65</span><span class="verse" id="Luke.1.65">καὶ ἐγένετο ἐπὶ πάντας φόβος τοὺς περιοικοῦντας αὐτούς,  καὶ ἐν ὅλῃ τῇ ὀρεινῇ τῆς Ἰουδαίας διελαλεῖτο πάντα τὰ ῥήματα ταῦτα,</span>' +  
        '    <span class="verseId">66</span><span class="verse" id="Luke.1.66">καὶ ἔθεντο πάντες οἱ ἀκούσαντες ἐν τῇ καρδίᾳ αὐτῶν,  λέγοντες&middot;  Τί ἄρα τὸ παιδίον τοῦτο ἔσται;  καὶ γὰρ χεὶρ κυρίου ἦν μετ&rsquo; αὐτοῦ.</span>' +  
        '    ' +
        '    <div class="paragraph">&nbsp;</div>' +
        '    <span class="verseId">67</span><span class="verse" id="Luke.1.67">Καὶ Ζαχαρίας ὁ πατὴρ αὐτοῦ ἐπλήσθη πνεύματος ἁγίου καὶ ἐπροφήτευσεν λέγων&middot;</span>' +  
        '    <span class="verseId">68</span><span class="verse" id="Luke.1.68">Εὐλογητὸς κύριος ὁ θεὸς τοῦ Ἰσραήλ,  ὅτι ἐπεσκέψατο καὶ ἐποίησεν λύτρωσιν τῷ λαῷ αὐτοῦ,</span>' +  
        '    <span class="verseId">69</span><span class="verse" id="Luke.1.69">καὶ ἤγειρεν κέρας σωτηρίας ἡμῖν ἐν οἴκῳ Δαυὶδ παιδὸς αὐτοῦ,</span>' +  
        '    <span class="verseId">70</span><span class="verse" id="Luke.1.70">καθὼς ἐλάλησεν διὰ στόματος τῶν ἁγίων ἀπ&rsquo; αἰῶνος προφητῶν αὐτοῦ,</span>' +  
        '    <span class="verseId">71</span><span class="verse" id="Luke.1.71">σωτηρίαν ἐξ ἐχθρῶν ἡμῶν καὶ ἐκ χειρὸς πάντων τῶν μισούντων ἡμᾶς,</span>' +  
        '    <span class="verseId">72</span><span class="verse" id="Luke.1.72">ποιῆσαι ἔλεος μετὰ τῶν πατέρων ἡμῶν καὶ μνησθῆναι διαθήκης ἁγίας αὐτοῦ,</span>' +  
        '    <span class="verseId">73</span><span class="verse" id="Luke.1.73">ὅρκον ὃν ὤμοσεν πρὸς Ἀβραὰμ τὸν πατέρα ἡμῶν,  τοῦ δοῦναι ἡμῖν</span>' + 
        '    <span class="verseId">74</span><span class="verse" id="Luke.1.74">ἀφόβως ἐκ χειρὸς ἐχθρῶν ῥυσθέντας λατρεύειν αὐτῷ</span>' + 
        '    <span class="verseId">75</span><span class="verse" id="Luke.1.75">ἐν ὁσιότητι καὶ δικαιοσύνῃ ἐνώπιον αὐτοῦ πάσαις ταῖς ἡμέραις⸃  ἡμῶν.</span>' +  
        '    <span class="verseId">76</span><span class="verse" id="Luke.1.76">καὶ σὺ δέ,  παιδίον,  προφήτης Ὑψίστου κληθήσῃ,  προπορεύσῃ γὰρ ἐνώπιον κυρίου ἑτοιμάσαι ὁδοὺς αὐτοῦ,</span>' +  
        '    <span class="verseId">77</span><span class="verse" id="Luke.1.77">τοῦ δοῦναι γνῶσιν σωτηρίας τῷ λαῷ αὐτοῦ ἐν ἀφέσει ἁμαρτιῶν αὐτῶν,</span>' +  
        '    <span class="verseId">78</span><span class="verse" id="Luke.1.78">διὰ σπλάγχνα ἐλέους θεοῦ ἡμῶν,  ἐν οἷς ἐπισκέψεται ἡμᾶς ἀνατολὴ ἐξ ὕψους,</span>' +  
        '    <span class="verseId">79</span><span class="verse" id="Luke.1.79">ἐπιφᾶναι τοῖς ἐν σκότει καὶ σκιᾷ θανάτου καθημένοις,  τοῦ κατευθῦναι τοὺς πόδας ἡμῶν εἰς ὁδὸν εἰρήνης.</span>' +
        '    ' +
        '    <div class="paragraph">&nbsp;</div>'+
        '    <span class="verseId">80</span><span class="verse" id="Luke.1.80">Τὸ δὲ παιδίον ηὔξανε καὶ ἐκραταιοῦτο πνεύματι,  καὶ ἦν ἐν ταῖς ἐρήμοις ἕως ἡμέρας ἀναδείξεως αὐτοῦ πρὸς τὸν Ἰσραήλ.</span>' +
        '    ' +
        '  </div>' +
        '</div>';

//========================================================================= 
// BASE TEXT PANEL
//=========================================================================
  

var CREATE_LINK_EVENT = "createlink";

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
    
    var m_size = null;
    
    /** The TC editor to display the base text's content. */
    var m_editor = null;
    
    // TODO need a smarter way to structure this 
    var m_text = m_cfg.props.text || EXAMPLE_TEXT;
    
    var m_animDuration = m_cfg.props.animDuration || 0.5;
    
    var m_heightOffset = 0;
    var m_widthOffest  = 0;
   
    //========================================================================= 
    // RENDERING AND EVENT LISTENING METHODS
    //=========================================================================

    // TODO save/restore state
    //      link to facsimile page
    //      sync with verse markers
     
    function renderUI() {
        m_body.innerHTML = "<textarea>" + EXAMPLE_TEXT + "</textarea>";
        var kmap = new IDCH.utils.KeyMap(IDCH.utils.KeyMap.GK_MAP),
            size = m_panel.getVisualProperties().size;
            m_editor = new TCEditor(Selector.query("textarea", m_body, true), {
                width   : size.width.get() + 'px',
                height  : size.height.get() + 'px',
                animate : true, // Animates the opening, closing and moving of Editor windows
                
                // The dompath display creates problems because it doesn't get 
                // its fully rendered size until after the editorContentLoaded 
                // event fires. This means that we can't resize the window 
                // to fit within the panel because we don't know how big this 
                // element will be. Also, the information that it contains isn't 
                // too helpful to users in this context.
                dompath : false, 
                kmap    : kmap
            });
        
        m_editor.render();

        m_panel.dd.addInvalidHandleClass("blockdrag");
        m_panel.dd.addInvalidHandleType("span");
        
        var controls = m_panel.getControls(),
            linkButton = new LinkButton(controls.top);
    }
    
    function bindUI() {
        m_size = m_panel.getVisualProperties().size;
        
        m_panel.provider.createEvent(CREATE_LINK_EVENT);
        
        // resize the editor on size changes
        m_size.on("change", function(obj) {
            var prop = obj.prop;
            if ((prop === m_size.width) || (prop === m_size.height)) {
                resize();
            }
        });
        
        // focus the panel on editor clicks
        m_editor.on("editorClick", function() {
            m_panel.focus();
        });
    }
    
    function syncUI() {
        m_editor.on("editorContentLoaded", function() {
            m_editor.set("height", m_size.height.get() + "px");
            m_editor.set("width", m_size.width.get() + "px");
            
            // see how much this needs to be adjusted
            var container = Selector.query(".yui-editor-container", m_body, true),
                cReg = dom.getRegion(container),
                bReg = dom.getRegion(m_body);
            
            m_widthOffset = cReg.right - bReg.right;
            m_heightOffset = cReg.bottom - bReg.bottom;
            
            resize();
            if (PageImageLink.hasLink(m_panel)) {
                try { PageImageLink.getLink(m_panel);} 
                catch (ex) { /* Surpress errors in third party code */ }
            }
        }, this, true);
    }
    
    /** 
     * Determines the size-offset between the panel size and the editor size.
     *  
     */
    function updateConstraints() {
        // set the size of the editor to be the size of the panel
       
    }
    
    function resize() {
        m_editor.set('width',  m_size.width.get()  - m_widthOffset  + 'px'); 
        m_editor.set('height', m_size.height.get() - m_heightOffset + 'px'); 
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
    
    
    //========================================================================= 
    // DOMAIN MODEL INTERACTION METHODS
    //=========================================================================
    
lang.augmentObject(m_panel, {
    getEditor : function() {
        return m_editor;
    },
    
    linkPageImage : function(panel) {
        if (panel.getType() !== IDCH.afed.PAGE_DISPLAY_PANEL)
            return false;

        if (PageImageLink.hasLink(panel)) {
            // FIXME for now, we'll clear any existing references
            PageImageLink.clear(panel);       
            return false;
        }
        
        var link = new PageImageLink(m_panel);
        link.addPanel(panel);
        
        m_panel.provider.fireEvent(CREATE_LINK_EVENT, 
                { panel : m_panel, link  : link });
        return true;
    },
    
    unlinkPageImage : function() {
        if (!PageImageLink.hasLink(m_panel))
            return;
        
        PageImageLink.getLink(m_panel, function(link) {
            if (!link) {
                return;
            }
            
            link.removePanel(m_panel);
        });
    },
    
    hasPageImageLink : function() {
        return PageImageLink.hasLink(m_panel);
    }
}, true);    
    
    
    function initialize() {
        m_panel.addClass("bt-panel");
        m_panel.configure(m_cfg);
        
        renderUI();
        bindUI();
        syncUI();
        
        m_panel._panelType = PANEL_TYPE; 
    }
      

    initialize();
    return m_panel;
}



//============================================================================
// CONTROL BUTTONS
//============================================================================

var CSS_BUTTON = "button",
    CSS_LINK   = "bt-link",
    CSS_ACTIVE = "active",
    CSS_LINKED = "linked";

/** 
 * A button that enables the base class panel to be linked to an 
 * IDCH.afed.PageDisplayPanel.
 * 
 * TODO refactor as plugin (assumes Panel has been refactored to YUI3).
 *  
 * @return
 */
function LinkButton(ctrlArea) {
    var panel = ctrlArea.getPanel();
    if (panel.getType() !== PANEL_TYPE)
        throw new Error("Invalid panel type. Expected " + PANEL_TYPE);
    
    LinkButton.superclass.constructor.call(this, CSS_LINK);
    
    // initialize event listeners
    this.provider.subscribe("click", function() {
        if (this.isLinked())       this.unlink();
        else if (this.isActive())  this.deactivate();
        else                       this.activate();
    }, this, true);
    
    ctrlArea.addButton(this);
    
    // if panel has link, set state accordingly
    if (panel.hasPageImageLink())
        dom.addClass(this.getEl(), CSS_LINKED);
}

    //========================================================================
    // PRIVATE METHODS
    //========================================================================

    var g_highlightedPanels = [];
    
    /** 
     * Handles user interactions that involve mousing over a panel. Highlights
     * the panel if it is an appropriate target for linking.
     * 
     * @method onPanelEnter
     * @private
     * @param obj
     */
    function onPanelEnter(obj) {
        var panel = obj.panel;
        
        if (panel.getType() === IDCH.afed.PAGE_DISPLAY_PANEL) {
            highlight(panel);
            g_highlightedPanels.push(panel);
        }
    } 
    
    /** 
     * Handles user interactions that involve the mouse moving away from a panel.
     * If the panel was highlighted, this clears the highlighting.
     * 
     * @method onPanelEnter
     * @private
     * @param obj
     */
    function onPanelLeave(obj) {
        var panel = obj.panel,
            ix    = g_highlightedPanels.indexOf(panel);
        
        if (ix >= 0) {
            clearHighlight(panel);
            g_highlightedPanels.splice(ix, 1);
        }
    }
    
    function onPanelClick(obj) {
        var link,
            target = obj.panel,
            panel  = this.getControl().getPanel();
        
        try {
            link = panel.linkPageImage(target);
            dom.addClass(this.getEl(), CSS_LINKED);
        } catch (ex) {
            link = null;
        }
        
        clearHighlight(target);
        this.deactivate();
    }
    
    /** 
     * Highlights a target panel to indicate that it is a candidate for selection.
     * 
     * @param panel
     */
    function highlight(panel) {
        var vprops = panel.getVisualProperties();
        vprops.border.width.preview(3);
        vprops.border.color.preview("#F00");
    }
    
    
    /** 
     * Clears the highlighting from a target panel.
     * 
     * @param panel
     */
    function clearHighlight(panel) {
        var vprops = panel.getVisualProperties();
        vprops.border.width.revert();
        vprops.border.color.revert();
    }


lang.extend(LinkButton, IDCH.critspace.ControlButton, {
    
    activate : function() {
        var panel = this.getControl().getPanel(),
            workspace = panel.getWorkspace();
        dom.addClass(this.getEl(), CSS_ACTIVE);
        
        this._displayMessage();
        workspace.on("panelEnter", onPanelEnter, this, true);
        workspace.on("panelLeave", onPanelLeave, this, true);
        workspace.on("panelClick", onPanelClick, this, true);
    },
    
    deactivate : function() {
        var panel = this.getControl().getPanel(),
            workspace = panel.getWorkspace();
        dom.removeClass(this.getEl(), CSS_ACTIVE);
        
        this._hideMessage();
        workspace.unsubscribe("panelEnter", onPanelEnter, this);
        workspace.unsubscribe("panelLeave", onPanelLeave, this);
        workspace.unsubscribe("panelClick", onPanelClick, this);
    },
    
    isActive : function() {
        return dom.hasClass(this.getEl(), CSS_ACTIVE);
    },
    
    isLinked : function() {
        return dom.hasClass(this.getEl(), CSS_LINKED);
    },

    /**
     * Destroys any link associated with the panel.
     */
    unlink : function() {
        var panel    = this.getControl().getPanel(),
            buttonEl = this.getEl();
        
        PageImageLink.getLink(panel, function(link) {
            if (link != null) 
                link.destroy();
            
            dom.removeClass(buttonEl, CSS_LINKED);
        });
    },
    
    /** 
     * Displays a message to the user to indicate what actions to take 
     * in order to establish a link between this panel and a PageDisplayPanel.
     */
    _displayMessage : function() {
        if (lang.isValue(this._infoDisplay))
            return;
        
        var panel  = this.getControl().getPanel(),
            bodyEl = panel.getBody(),
            attributes, anim;
            
        this._infoDisplay = $EL("div", "bt-link-notice", 
            "Select a Page Display Panel to edit.");
       
        attributes = {
                opacity : { to: 1 }
            };
        anim = new YAHOO.util.Anim(this._infoDisplay, attributes);

        anim.duration = 0.25;
        dom.setStyle(this._infoDisplay, "opacity", 0);
        bodyEl.appendChild(this._infoDisplay);
        anim.animate();
    },
    
    /**
     * Hides the message display associated with this button.
     */
    _hideMessage : function(canceled) {
        if (!lang.isValue(this._infoDisplay))
            return;
        
        var infoEl = this._infoDisplay,
            attributes = { opacity : { to: 0 }},
            anim = new YAHOO.util.Anim(this._infoDisplay, attributes);
        
        anim.duration = 0.25;
        anim.onComplete.subscribe(function() {
            // remove the element from the DOM once the animation has completed
            infoEl.parentNode.removeChild(infoEl);
            this._infoDisplay = null;
        }, this, true);
        
        anim.animate();
    }
});
    
//=============================================================================
// PAGE IMAGE LINK (PanelLink)  
//=============================================================================


/**
 * @param link { PageImageLink } The link with which this marker is 
 *      associated.
 * @param marker { Marker } The marker 
 */
function setMarkerVerse(link, marker, verse) {
    
}
var VERSE_CHANGED_EVENT = "versechanged";

var CSS_VSMARKER = "vsmarker";

function VerseMarker(marker, link) {
    
    // TODO shouldn't depend on the link
    // TODO refactor into a stand alone class
    
    
    function render() {
        var el = marker.getEl();
        
        dom.addClass(el, CSS_VSMARKER);
        el.innerHTML = "7";
        // TODO CREATE TOOLTIP
    }
    
    /**
     * Binds event listeners for this VerseMarker creates the events that will 
     * be generated
     */
    function bind() {
        var provider = marker.provider;
        
        provider.createEvent(VERSE_CHANGED_EVENT);
    }
    
    
    lang.augmentObject(marker, {
        
        /**
         * Sets the verse associated with this marker.
         */
        setVerseId : function(verseId) {
            var id      = this._verseId,
                markers = link._markers;
            
            if (verseId === null) {
                if (markers[id] === this)
                    markers[id] = null;
                
                this._verseId = null;
            } else if (lang.isString(verseId)) {
                verseId = verseId.trim();
                
                if (lang.isValue(id) && (markers[id] === this))
                    markers[id] = null;
                
                markers[verseId] = this;
                this._verseId = verseId;
            } else {
                throw new Error("Invalid verse id: not a string.");
            }
            
            this.provider.fireEvent(VERSE_CHANGED_EVENT, 
                    { marker : this, prevValue : id, value : verseId });
        },
        
        getVerseId : function() {
            // While you could access this directly, I really   
            // don't want people touching my privates. 
            this._verseId;      
        }
        
    }, true);
    
    render();
    bind();
    
    return marker;
}

function onMarkerPlaced(marker) {
    marker = new VerseMarker(marker, this);
    marker.setVerse(this.getNextVerse());
}

function onMarkerClicked(obj) {
    var maker = obj.marker,
        ev    = obj.ev;
    
    alert(marker);
}

function PageImageLink(basePanel, linkedPanels) {
    // make sure the base panel is a BaseText
    if (basePanel.getType() != IDCH.nt.BASE_TEXT_PANEL)
        throw new Error("Invalid PageImageLink. Base panel must be a " +
        		"CollationBaseText panel.");
    
    PageImageLink.superclass.constructor.call(
            this, basePanel, linkedPanels, PageImageLink.LINK_NAME);
    
    // This is a binary linking tool. We'll use this to keep track of the 
    // target panel.
    this.targetPanel = null;
}

PageImageLink.LINK_NAME = "IDCH.nt.PageImageLink";
PanelLink.attachFactoryMethods(PageImageLink);

lang.extend(PageImageLink, IDCH.critspace.PanelLink, {
    
    /**
     * Attaches event listeners to newly added panels to monitor their state.
     *
     * @method monitor
     * @protected
     * @param panel { Panel } The <code>Panel</code> to monitor.
     */
    monitor : function(panel) {
        var type = panel.getType();
        if (type === IDCH.afed.PAGE_DISPLAY_PANEL) {
            // TODO implement sanity checks
             
            var layer = new IDCH.tzivi.MarkerLayer();
            
            // monitor and adjust markers
            layer.on("marker:create", onMarkerPlaced, this, true);
            layer.on("marker:click", onMarkerClicked, this, true);
            
            this._markerLayer = layer;
            
            // delayed attach to layer (allows image layer to be added first)
            lang.later(1000, this, function() {             // FIXME ad hoc delay
                panel.viewport.addLayer(this._markerLayer);
            });
        } else if (type === IDCH.nt.BASE_TEXT_PANEL) {
            // monitor this as needed
            this._editor = panel.getEditor();
            this._verses = this._editor.listVerses();
            this._markers = [];
        }
    },
    
    /**
     * Dettaches event listeners monitoring the state of a linked panel.
     * 
     * @method stopMonitoring
     * @protected
     * @param panel { Panel } The <code>Panel</code> to stop monitoring.
     */
    stopMonitoring : function(panel) {
        var type = panel.getType();
        if (type === IDCH.afed.PAGE_DISPLAY_PANEL) {
            var viewport = panel.viewport;
            if (viewport && viewport.hasLayer(this._markerLayer))
                viewport.removeLayer(this._markerLayer);
            
            this._markerLayer = null;
        } else if (type === IDCH.nt.BASE_TEXT_PANEL) {
            // monitor this as needed 
        }
    },
    
    /**
     * Indicate whether the supplied panel is a valid link target.
     * 
     * @method isCompatible
     * @protected
     * @param panel { Panel } 
     * @returns { Boolean } 
     */
    isCompatible : function(panel) {
        // TEST should be either a base text panel or a facsimile panel
        var type = panel.getType();
        
        return (panel === this.basePanel)  
            ? (type === IDCH.nt.BASE_TEXT_PANEL)
            : (type === IDCH.afed.PAGE_DISPLAY_PANEL);
    },
    
    getNextVerse : function(referenceVerseId) {
        
    },
    
    getVerseIndex : function(verseId) {
        return (lang.isValue(this.basePanel) && lang.isString(verseId)) 
                    ? link._verses.indexOf(verseId.trim()) 
                    : -2;
    },
    
    highlight : function(verseId) {
        
    },
    
    /**
     * Event handler called whenever the selected image is changed. This 
     * updates all panels in the set with the new data.
     * 
     * @method onImageChanged
     * @private
     * @param panel {IDCH.critspace.Panel } The panel whose facsimile page 
     *      changed.
     */
    _onImageChanged : function(panel) {
      
    }
});


//=============================================================================
// BOILERPLATE FACTORY METHODS
//=============================================================================

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
