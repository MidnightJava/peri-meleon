Type 1 Font for FOP

Do it on Windows!

C:\nobackups\mine\members>java -cp lib\fop.jar;lib\avalon-framework.jar;lib\commons-logging.jar;lib\commons-io.jar org.apache.fop.fonts.apps.PFMReader
 -fn AGaramondTitling /Library/Application Support/com.tamelea.perimeleon/resource\GDTTL___.PFM /Library/Application Support/com.tamelea.perimeleon/resource\gdttl.xml
PFM Reader for Apache FOP 0.94

Parsing font...
Reading /Library/Application Support/com.tamelea.perimeleon/resource\GDTTL___.PFM...

Font: AGaramond Titling
Name: AGaramond-Titling
CharSet: WinAnsi
CapHeight: 663
XHeight: 478
LowerCaseAscent: 478
LowerCaseDescent: 65333
Having widths for 223 characters (32-255).
for example: Char 32 has a width of 250

Creating xml font file...

Writing xml font file /Library/Application Support/com.tamelea.perimeleon/resource\gdttl.xml...
XML font metrics file successfullly created.

C:\nobackups\mine\members>

So notice that we process the PFM, but we need the PFB as the /Library/Application Support/com.tamelea.perimeleon/resource!


TTF works as advertised; OTF doesn't work with their reader (yet).

Note on AGaramond-Titling: The .xml metrics file had descender and bottom values that were very large
negative numbers. And one line in the font seemed to want to extend to the south pole. So I usbstituted
the original values (see gdttl-orig.xml) with more reasonable numbers cribbed from one of the Minion
files. Cleared up the problem. Yea XML!