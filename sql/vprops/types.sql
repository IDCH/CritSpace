-- Default property types

INSERT INTO PropertyTypes (type_id, css, name, description, format, config)
VALUES ('background-color', 'background-color', 'Background Color', 'The background color.', 'txt',
            '{"format":"txt","regex":"^#([0-9A-F]{3}|[0-9A-F]{6})$","regext":"i","options":null,"restrict":false }'),
       ('opacity', 'opacity', 'Opacity', '', 'num', '{"format":"num","range" : [0, 1]}'),
       ('z-index', 'z-index', 'Z Index', '', 'num', '{"format":"num","range" : [-100, 100]}'),
       ('top', 'top', 'Top', '', 'num', '{"format":"num","units":"px", "range":null}'),
       ('left', 'left', 'Left', '', 'num', '{"format":"num","units":"px", "range":null}'),
       ('width', 'width', 'Width', '', 'num', '{"format":"num","units":"px", "range":null}'),
       ('height', 'height', 'Height', '', 'num', '{"format":"num","units":"px", "range":null}'),
       ('x-aspect', 'x-aspect', 'Aspect Ratio', '', 'num', '{"format":"num","range":[-100,100], "enabled":false}'),
       
       ('font-family', 'font-family', 'Font', '', 'txt', 
            '{"format":"txt","options":["Arial","Courier","Calibri","Cambria","Helvetica","Garamond","Georgia","Times","Verdana","serif","sans-serif","cursive","fantasy","monospace"],"restrict":false }'),
       ('font-color', 'color', 'Font Color', '', 'txt',
            '{"format":"txt","regex":"^#([0-9A-F]{3}|[0-9A-F]{6})$","regext":"i","options":null,"restrict":false }'),
       ('font-size', 'font-size', 'Font Size', '', 'num', '{"format":"num","units":"em","range":[0.5, 20]}'),
       ('italic', 'font-style', 'Italic', '', 'tog', '{"format":"tog","on":"italic","off":"normal","value":"normal"}'),
       ('bold', 'font-weight', 'Bold', '', 'tog', '{"format":"tog","on":"bold","off":"normal","value":"normal"}'),
       ('small-caps', 'font-variant', 'Small Caps', '', 'tog', '{"format":"tog","on":"small-caps","off":"normal","value":"normal"}'),
       ('word-spacing', 'word-spacing', 'Word Spacing', '', 'num', '{"format":"num","units":"ex","range":[-1, 1]}'),
       ('letter-spacing', 'letter-spacing', 'Letter Spacing', '', 'num', '{"format":"num","units":"ex","range":[-1, 1]}'),
       ('text-decoration', 'text-decoration', 'Decoration', '', 'txt',     
            '{"format":"txt","options":["none","underline","overline","line-through"],"restrict":true}'),
       ('text-transform', 'text-transform', 'Transform', '', 'txt',     
            '{"format":"txt","options":["none","capitalize","uppercase","lowercase"],"restrict":true}'),
       ('text-align', 'text-align', 'Align', '', 'txt',     
            '{"format":"txt","options":["left","right","center","justify"],"restrict":true}'),
       ('text-indent', 'text-indent', 'Indent', '', 'num', '{"format":"num","units":"ex","range":[-5,20]}'),
       
       ('border-width', 'border-width', 'Border Width', '', 'num', '{"format":"num","units":"px","range":[0,20]}'),
       ('border-color', 'border-color', 'Border Color', '', 'txt',
            '{"format":"txt","regex":"^#([0-9A-F]{3}|[0-9A-F]{6})$","regext":"i","options":null,"restrict":false }'),
       ('border-style', 'border-style', 'Border Style', '', 'txt',     
            '{"format":"txt","options":["none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset","inherit"],"restrict":true}'),
       
       ('padding', 'padding', 'Padding', '', 'num', '{"format":"num","units":"px","range":[0,50]}'),
       ('margin', 'margin', 'Margin', '', 'num', '{"format":"num","units":"px","range":[0,50]}');