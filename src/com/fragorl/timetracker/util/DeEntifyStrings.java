package com.fragorl.timetracker.util;

/*
 * [DeEntifyStrings.java]
 *
 * Summary: Strips HTML entities such as &quot; from a string, replacing them by their Unicode equivalents.
 *
 * Copyright: (c) 2002-2013 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.6+
 *
 * Created with: JetBrains IntelliJ IDEA IDE http://www.jetbrains.com/idea/
 *
 * Version History:
 *  2.6 2009-04-05 StripEntities now leaves a space behind when it removes a <br><p><td> etc tag.
 *  2.7 2009-11-14 generate a table for the HTML cheat sheet of quote-like entities.
 *  2.8 2009-12-22 export table on HTML 5 entities. Now import csv file rather than embed entity facts.
 *  2.9 2010-01-29 export XHTML entities (currently same as HTML-4 entities).
 *  3.0 2011-02-10 rename to deEntify, delete deprecated methods.
 *  3.1 2011-09-02 correct error in tables reversing Y dieresis and y dieresis. Correctupper/lower categories in table.
 */

import java.util.HashMap;

/**
 * Strips HTML entities such as &quot; from a string, replacing them by their Unicode equivalents.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 3.1 2011-09-02 correct error in tables reversing Y dierisis and y dieresis. Corect upper/lower categories
 *          in tabel.
 * @see DeEntify
 * @see DeEntifyStrings
 * @see Entify
 * @see EntifyStrings
 * @see Flatten
 * @since 2002-07-14
 */
public class DeEntifyStrings
{
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * true to enable the testing code.
     */
    private static final boolean DEBUGGING = true;

    /**
     * unicode nbsp control char, 160, 0x0a.
     */
    @SuppressWarnings({ "WeakerAccess" })
    public static final char UNICODE_NBSP_160_0x0a = 160;

    /**
     * Longest an HTML4 entity can be, at least in our tables, including the lead & and trail ;.
     * Note HTM4 longest entity is {@value #LONGEST_HTML4_ENTITY}.
     *
     * @noinspection WeakerAccess, JavadocReference, WeakerAccess
     */
    public static final int LONGEST_HTML4_ENTITY = "&thetasym;".length();

    /**
     * Longest an HTML5 entity can be, at least in our tables, including the lead & and trail ;.
     * Note HTML5 longest entity is {@value #LONGEST_HTML5_ENTITY},.
     *
     * @noinspection WeakerAccess, JavadocReference, WeakerAccess
     */
    public static final int LONGEST_HTML5_ENTITY = "&CounterClockwiseContourIntegral;".length();

    /**
     * The shortest an entity can be {@value #SHORTEST_HTML4_ENTITY}, at least in our tables, including the lead & and
     * trailing ;.
     *
     * @noinspection WeakerAccess, JavadocReference, WeakerAccess
     */
    public static final int SHORTEST_HTML4_ENTITY = 4;/* &#1; &lt; */

    /**
     * The shortest an entity can be {@value #SHORTEST_HTML5_ENTITY}, at least in our tables, including the lead & and
     * trailing ;.
     *
     * @noinspection WeakerAccess, JavadocReference, WeakerAccess
     */
    public static final int SHORTEST_HTML5_ENTITY = 4;/* &#1; &lt; */

    /**
     * tags, that when removed should leave a space behind.
     */
    private static final String[] spacingTags = { "tr", "td", "th", "p", "br", "dl", "dt", "li" };

    /**
     * allows lookup by entity name, to get the corresponding char.
     * Loaded from two hard-coded generated arrays burning into this class.
     * Does not deal with HTML5 entities.
     */
    private static final HashMap<String, Character> entityToChar;

    // -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * convert an entity to a single char.  Does not deal with HTML5 entities.
     *
     * @param bareEntity         String entity to convert convert. must have lead & and trail ; stripped; may have
     *                           form: #x12ff or #123 or lt or nbsp
     *                           style entity. Works faster if entity in lower case.
     * @param howToTranslateNbsp char you would like &nbsp translated to, usually ' ' or (char) 160
     *
     * @return equivalent character. 0 if not recognised.
     * @noinspection WeakerAccess
     */
    public static char bareHTMLEntityToChar( String bareEntity, char howToTranslateNbsp )
    {
        // first check for alpha entity
        Character code = entityToChar.get( bareEntity );
        if ( code != null )
        {
            return code;
        }
        code = entityToChar.get( bareEntity.toLowerCase() );
        if ( code != null )
        {
            return code;
        }
        // nbsp is not in hashMap.  We test for it specially.
        if ( bareEntity.length() == 4 && bareEntity.equals( "nbsp" ) || bareEntity.equals( "NBSP" ) )
        {
            return howToTranslateNbsp;
        }
        // check at least have &_#1_;  (no & or ; at this point )
        if ( bareEntity.length() < 2 )
        {
            return 0;
        }
        try
        {
            if ( bareEntity.charAt( 0 ) == '#' )
            {
                final char secondChar = bareEntity.charAt( 1 );
                if ( secondChar == 'x' || secondChar == 'X' )
                {
                    // handle hex entities  of form  &_#x12ff_;
                    // ensure at least have &_#xf_;
                    if ( bareEntity.length() < 3 )
                    {
                        return 0;
                    }
                    // had &_#x123D_;
                    return ( char ) Integer.parseInt( bareEntity.substring( 2 ),
                            /* hex */
                            16 );
                }
                else
                {
                    // handle decimal entities
                    // had &_#123_;
                    return ( char ) Integer.parseInt( bareEntity.substring( 1 ) );
                }
            }
            else
            {
                // some unrecognized/malformed bareEntity
                return 0;
            }
        }
        catch ( NumberFormatException e )
        {
            return 0;
        }
    }// end entityToChar

    /**
     * Converts HTML to text converting entities such as &quot; back to " and &lt; back to &lt; Ordinary text passes
     * unchanged. Also strips decimal and hex entities and stray HTML entities.  Does not deal with HTML5 entities.
     *
     * @param text            raw text to be processed. Must not be null.
     * @param translateNbspTo char you would like &nbsp; translated to, usually ' ' or (char) 160 .
     *
     * @return translated text. It also handles HTML 4.0 entities such as &hearts; &#123; and &#xffff; &nbsp; -> 160.
     *         null input returns null.
     * @noinspection WeakerAccess
     */
    public static String deEntifyHTML( String text, char translateNbspTo )
    {
        if ( text == null )
        {
            return null;
        }
        if ( text.indexOf( '&' ) < 0 )
        {
            // are no entities, nothing to do
            return text;
        }
        int originalTextLength = text.length();
        StringBuilder sb = new StringBuilder( originalTextLength );
        for ( int i = 0; i < originalTextLength; i++ )
        {
            int whereAmp = text.indexOf( '&', i );
            if ( whereAmp < 0 )
            {
                // no more &s, we are done
                // append all remaining text
                sb.append( text.substring( i ) );
                break;
            }
            else
            {
                // append all text to left of next &
                sb.append( text.substring( i, whereAmp ) );
                // avoid reprocessing those chars
                i = whereAmp;
                // text.charAt(i) is an &
                // possEntity has lead & stripped.
                String possEntity =
                        text.substring( i + 1,
                                Math.min( i + LONGEST_HTML4_ENTITY,
                                        text.length() ) );
                char t = possBareHTMLEntityWithSemicolonToChar( possEntity, translateNbspTo );
                if ( t != 0 )
                {
                    // was a good entity, keep its equivalent char.
                    sb.append( t );
                    // avoid reprocessing chars forming the entity
                    int whereSemi =
                            possEntity.indexOf( ";", SHORTEST_HTML4_ENTITY - 2 );
                    i += whereSemi + 1;
                }
                else
                {
                    // treat & just as ordinary character
                    sb.append( '&' );
                }
            }// end else
        }// end for
        // if result is not shorter, we did not do anything. Saves RAM.
        return ( sb.length() == originalTextLength ) ? text : sb.toString();
    }// end stripEntities

    /**
     * Converts XML to text converting entities such as &quot; back to " and &lt; back to &lt; Ordinary text passes
     * unchanged. Also strips decimal and hex entities and stray HTML entities.
     *
     * @param text raw XML text to be processed. Must not be null.
     *
     * @return translated text.   null input returns null.
     * @noinspection WeakerAccess
     */
    public static String deEntifyXML( String text )
    {
        return deEntifyHTML( text, ' ' );
    }

    /**
     * strips tags and entities from HTML.  Does not deal with HTML5 entities.  Leaves \n \r unchanged.
     *
     * @param text            to flatten
     * @param translateNbspTo char you would like &nbsp; translated to, usually ' ' or (char) 160 .
     *
     * @return flattened text
     * @noinspection WeakerAccess
     */
    public static String flattenHTML( String text, char translateNbspTo )
    {
        return deEntifyHTML( stripHTMLTags( text ), translateNbspTo );
    }

    /**
     * strips tags and entities from XML..
     *
     * @param text to flatten
     *
     * @return flattened text
     * @noinspection WeakerAccess
     */
    public static String flattenXML( String text )
    {
        return deEntifyXML( stripXMLTags( text ) );
    }

    /**
     * Checks a number of gauntlet conditions to ensure this is a valid entity. Converts Entity to corresponding char.
     * Does not deal with HTML5 entities.
     *
     * @param possBareEntityWithSemicolon string that may hold an entity. Lead & must be stripped,
     *                                    but may optionally contain text past the ;
     *
     * @return corresponding unicode character, or 0 if the entity is invalid.  nbsp -> (char) 160
     * @noinspection WeakerAccess
     */
    public static char possEntityToChar( String possBareEntityWithSemicolon )
    {
        return possBareHTMLEntityWithSemicolonToChar( possBareEntityWithSemicolon, UNICODE_NBSP_160_0x0a );
    }

    /**
     * Removes tags from HTML leaving just the raw text. Leaves entities as is, e.g. does not convert &amp; back to &.
     * similar to code in Quoter. Also removes &lt;!-- --&gt; comments. Presumes perfectly formed HTML, no &gt; in
     * comments, all &lt;...&gt; balanced. Also removes text between applet, style and script tag pairs.
     * Leaves &nbsp; and other entities as is. Does not deal with HTML5 entities.
     *
     * @param html input HTML
     *
     * @return raw text, with whitespaces collapsed to a single space, trimmed.
     * @noinspection WeakerAccess
     */
    public static String stripHTMLTags( String html )
    {
        assert html != null : "attempt to strip HTML tags from a null String";
        html = stripHTMLTagPairs( html );
        return stripIndividualTags( html );
    }

    /**
     * Removes tags from XML leaving just the raw text. Leaves entities as is, e.g. does not convert &amp; back to &.
     * similar to code in Quoter. Also removes &lt;!-- --&gt; comments. Presumes perfectly formed XML, no &gt; in
     * comments, all &lt;...&gt; balanced.
     * Leaves entities as is.
     *
     * @param xml input XML
     *
     * @return raw text, with whitespaces collapsed to a single space, trimmed.
     * @noinspection WeakerAccess
     */
    public static String stripXMLTags( String xml )
    {
        assert xml != null : "attempt to strip XML tags from a null String";
        return stripIndividualTags( xml );
    }

    // -------------------------- STATIC METHODS --------------------------

    static
    {
        // build HashMap to look up entity name to get corresponding Unicode
        // char number. Following code generated by Entities.
        String[] entityKeys = {
                // W A R N I N G !  _ M A N U A L L Y  _ I N S E R T E D _ G E N E R A T E D  _ C O D E
                // generated by Entities. Insert from com\mindprod\entities\entitiesjustkeys.javafrag
                "AElig"    /*  198 : &#xc6; Latin capital letter AE */,
                "Aacute"   /*  193 : &#xc1; Latin capital letter A with acute */,
                "Acirc"    /*  194 : &#xc2; Latin capital letter A with circumflex */,
                "Agrave"   /*  192 : &#xc0; Latin capital letter A with grave */,
                "Alpha"    /*  913 : &#x391; Greek capital letter Alpha */,
                "Aring"    /*  197 : &#xc5; Latin capital letter A with ring above */,
                "Atilde"   /*  195 : &#xc3; Latin capital letter A with tilde */,
                "Auml"     /*  196 : &#xc4; Latin capital letter A with diaeresis */,
                "Beta"     /*  914 : &#x392; Greek capital letter Beta */,
                "Ccedil"   /*  199 : &#xc7; Latin capital letter C with cedilla */,
                "Chi"      /*  935 : &#x3a7; Greek capital letter Chi */,
                "Dagger"   /* 8225 : &#x2021; double dagger */,
                "Delta"    /*  916 : &#x394; Greek capital letter Delta */,
                "ETH"      /*  208 : &#xd0; Latin capital letter Eth */,
                "Eacute"   /*  201 : &#xc9; Latin capital letter E with acute */,
                "Ecirc"    /*  202 : &#xca; Latin capital letter E with circumflex */,
                "Egrave"   /*  200 : &#xc8; Latin capital letter E with grave */,
                "Epsilon"  /*  917 : &#x395; Greek capital letter Epsilon */,
                "Eta"      /*  919 : &#x397; Greek capital letter Eta */,
                "Euml"     /*  203 : &#xcb; Latin capital letter E with diaeresis */,
                "Gamma"    /*  915 : &#x393; Greek capital letter Gamma */,
                "Iacute"   /*  205 : &#xcd; Latin capital letter I with acute */,
                "Icirc"    /*  206 : &#xce; Latin capital letter I with circumflex */,
                "Igrave"   /*  204 : &#xcc; Latin capital letter I with grave */,
                "Iota"     /*  921 : &#x399; Greek capital letter Iota */,
                "Iuml"     /*  207 : &#xcf; Latin capital letter I with diaeresis */,
                "Kappa"    /*  922 : &#x39a; Greek capital letter Kappa */,
                "Lambda"   /*  923 : &#x39b; Greek capital letter Lambda */,
                "Mu"       /*  924 : &#x39c; Greek capital letter Mu */,
                "Ntilde"   /*  209 : &#xd1; Latin capital letter N with tilde */,
                "Nu"       /*  925 : &#x39d; Greek capital letter Nu */,
                "OElig"    /*  338 : &#x152; Latin capital ligature oe */,
                "Oacute"   /*  211 : &#xd3; Latin capital letter O with acute */,
                "Ocirc"    /*  212 : &#xd4; Latin capital letter O with circumflex */,
                "Ograve"   /*  210 : &#xd2; Latin capital letter O with grave */,
                "Omega"    /*  937 : &#x3a9; Greek capital letter Omega */,
                "Omicron"  /*  927 : &#x39f; Greek capital letter Omicron */,
                "Oslash"   /*  216 : &#xd8; Latin capital letter O with stroke */,
                "Otilde"   /*  213 : &#xd5; Latin capital letter O with tilde */,
                "Ouml"     /*  214 : &#xd6; Latin capital letter O with diaeresis */,
                "Phi"      /*  934 : &#x3a6; Greek capital letter Phi */,
                "Pi"       /*  928 : &#x3a0; Greek capital letter Pi */,
                "Prime"    /* 8243 : &#x2033; double prime */,
                "Psi"      /*  936 : &#x3a8; Greek capital letter Psi */,
                "Rho"      /*  929 : &#x3a1; Greek capital letter Rho */,
                "Scaron"   /*  352 : &#x160; Latin capital letter S with caron */,
                "Sigma"    /*  931 : &#x3a3; Greek capital letter Sigma */,
                "THORN"    /*  222 : &#xde; Latin capital letter Thorn */,
                "Tau"      /*  932 : &#x3a4; Greek capital letter Tau */,
                "Theta"    /*  920 : &#x398; Greek capital letter Theta */,
                "Uacute"   /*  218 : &#xda; Latin capital letter U with acute */,
                "Ucirc"    /*  219 : &#xdb; Latin capital letter U with circumflex */,
                "Ugrave"   /*  217 : &#xd9; Latin capital letter U with grave */,
                "Upsilon"  /*  933 : &#x3a5; Greek capital letter Upsilon */,
                "Uuml"     /*  220 : &#xdc; Latin capital letter U with diaeresis */,
                "Xi"       /*  926 : &#x39e; Greek capital letter Xi */,
                "Yacute"   /*  221 : &#xdd; Latin capital letter Y with acute */,
                "Yuml"     /*  376 : &#x178; Latin capital letter Y with diaeresis */,
                "Zeta"     /*  918 : &#x396; Greek capital letter Zeta */,
                "aacute"   /*  225 : &#xe1; Latin small letter a with acute */,
                "acirc"    /*  226 : &#xe2; Latin small letter a with circumflex */,
                "acute"    /*  180 : &#xb4; acute accent */,
                "aelig"    /*  230 : &#xe6; Latin lowercase ligature ae */,
                "agrave"   /*  224 : &#xe0; Latin small letter a with grave */,
                "alefsym"  /* 8501 : &#x2135; alef symbol */,
                "alpha"    /*  945 : &#x3b1; Greek small letter alpha */,
                "amp"      /*   38 : &#x26; ampersand */,
                "and"      /* 8743 : &#x2227; logical and */,
                "ang"      /* 8736 : &#x2220; angle */,
                "aring"    /*  229 : &#xe5; Latin small letter a with ring above */,
                "asymp"    /* 8776 : &#x2248; asymptotic to */,
                "atilde"   /*  227 : &#xe3; Latin small letter a with tilde */,
                "auml"     /*  228 : &#xe4; Latin small letter a with diaeresis */,
                "bdquo"    /* 8222 : &#x201e; double low-99 quotation mark */,
                "beta"     /*  946 : &#x3b2; Greek small letter beta */,
                "brvbar"   /*  166 : &#xa6; broken bar */,
                "bull"     /* 8226 : &#x2022; bullet */,
                "cap"      /* 8745 : &#x2229; intersection */,
                "ccedil"   /*  231 : &#xe7; Latin small letter c with cedilla */,
                "cedil"    /*  184 : &#xb8; cedilla */,
                "cent"     /*  162 : &#xa2; cent sign */,
                "chi"      /*  967 : &#x3c7; Greek small letter chi */,
                "circ"     /*  710 : &#x2c6; modifier letter circumflex accent */,
                "clubs"    /* 9827 : &#x2663; black club suit */,
                "cong"     /* 8773 : &#x2245; congruent to */,
                "copy"     /*  169 : &#xa9; copyright sign circled c */,
                "crarr"    /* 8629 : &#x21b5; downwards arrow with corner leftwards */,
                "cup"      /* 8746 : &#x222a; union */,
                "curren"   /*  164 : &#xa4; currency sign */,
                "dArr"     /* 8659 : &#x21d3; downwards double arrow */,
                "dagger"   /* 8224 : &#x2020; dagger */,
                "darr"     /* 8595 : &#x2193; downwards arrow */,
                "deg"      /*  176 : &#xb0; degree sign */,
                "delta"    /*  948 : &#x3b4; Greek small letter delta */,
                "diams"    /* 9830 : &#x2666; black diamond suit */,
                "divide"   /*  247 : &#xf7; division sign */,
                "eacute"   /*  233 : &#xe9; Latin small letter e with acute */,
                "ecirc"    /*  234 : &#xea; Latin small letter e with circumflex */,
                "egrave"   /*  232 : &#xe8; Latin small letter e with grave */,
                "empty"    /* 8709 : &#x2205; empty set */,
                "emsp"     /* 8195 : &#x2003; em space */,
                "ensp"     /* 8194 : &#x2002; en space */,
                "epsilon"  /*  949 : &#x3b5; Greek small letter epsilon */,
                "equiv"    /* 8801 : &#x2261; identical to */,
                "eta"      /*  951 : &#x3b7; Greek small letter eta */,
                "eth"      /*  240 : &#xf0; Latin small letter eth */,
                "euml"     /*  235 : &#xeb; Latin small letter e with diaeresis */,
                "euro"     /* 8364 : &#x20ac; Euro currency sign */,
                "exist"    /* 8707 : &#x2203; there exists */,
                "fnof"     /*  402 : &#x192; Latin small letter f with hook */,
                "forall"   /* 8704 : &#x2200; for all */,
                "frac12"   /*  189 : &#xbd; vulgar fraction 1/2 */,
                "frac14"   /*  188 : &#xbc; vulgar fraction 1/4 */,
                "frac34"   /*  190 : &#xbe; vulgar fraction 3/4 */,
                "frasl"    /* 8260 : &#x2044; fraction slash */,
                "gamma"    /*  947 : &#x3b3; Greek small letter gamma */,
                "ge"       /* 8805 : &#x2265; greater-than or equal to */,
                "gt"       /*   62 : &#x3e; greater-than sign */,
                "hArr"     /* 8660 : &#x21d4; left right double arrow */,
                "harr"     /* 8596 : &#x2194; left right arrow */,
                "hearts"   /* 9829 : &#x2665; black heart suit */,
                "hellip"   /* 8230 : &#x2026; horizontal ellipsis */,
                "iacute"   /*  237 : &#xed; Latin small letter i with acute */,
                "icirc"    /*  238 : &#xee; Latin small letter i with circumflex */,
                "iexcl"    /*  161 : &#xa1; inverted exclamation mark */,
                "igrave"   /*  236 : &#xec; Latin small letter i with grave */,
                "image"    /* 8465 : &#x2111; black-letter capital i */,
                "infin"    /* 8734 : &#x221e; infinity */,
                "int"      /* 8747 : &#x222b; integral */,
                "iota"     /*  953 : &#x3b9; Greek small letter iota */,
                "iquest"   /*  191 : &#xbf; inverted question mark */,
                "isin"     /* 8712 : &#x2208; element of */,
                "iuml"     /*  239 : &#xef; Latin small letter i with diaeresis */,
                "kappa"    /*  954 : &#x3ba; Greek small letter kappa */,
                "lArr"     /* 8656 : &#x21d0; leftwards double arrow */,
                "lambda"   /*  955 : &#x3bb; Greek small letter lambda */,
                "lang"     /* 9001 : &#x2329; left-pointing angle bracket */,
                "laquo"    /*  171 : &#xab; left guillemot */,
                "larr"     /* 8592 : &#x2190; leftwards arrow */,
                "lceil"    /* 8968 : &#x2308; left ceiling */,
                "ldquo"    /* 8220 : &#x201c; left double-66 quotation mark */,
                "le"       /* 8804 : &#x2264; less-than or equal to */,
                "lfloor"   /* 8970 : &#x230a; left floor */,
                "lowast"   /* 8727 : &#x2217; asterisk operator */,
                "loz"      /* 9674 : &#x25ca; open lozenge */,
                "lrm"      /* 8206 : &#x200e; left-to-right mark */,
                "lsaquo"   /* 8249 : &#x2039; single left-pointing angle quotation mark */,
                "lsquo"    /* 8216 : &#x2018; left single-6 quotation mark */,
                "lt"       /*   60 : &#x3c; less-than sign */,
                "macr"     /*  175 : &#xaf; macron */,
                "mdash"    /* 8212 : &#x2014; em dash */,
                "micro"    /*  181 : &#xb5; micro sign */,
                "middot"   /*  183 : &#xb7; middle dot */,
                "minus"    /* 8722 : &#x2212; minus sign */,
                "mu"       /*  956 : &#x3bc; Greek small letter mu */,
                "nabla"    /* 8711 : &#x2207; nabla */,
                "nbsp"     /*  160 : &#xa0; non-breaking space */,
                "ndash"    /* 8211 : &#x2013; en dash */,
                "ne"       /* 8800 : &#x2260; not equal to */,
                "ni"       /* 8715 : &#x220b; like backwards epsilon */,
                "not"      /*  172 : &#xac; not sign */,
                "notin"    /* 8713 : &#x2209; not an element of */,
                "nsub"     /* 8836 : &#x2284; not a subset of */,
                "ntilde"   /*  241 : &#xf1; Latin small letter n with tilde */,
                "nu"       /*  957 : &#x3bd; Greek small letter nu */,
                "oacute"   /*  243 : &#xf3; Latin small letter o with acute */,
                "ocirc"    /*  244 : &#xf4; Latin small letter o with circumflex */,
                "oelig"    /*  339 : &#x153; Latin small ligature oe */,
                "ograve"   /*  242 : &#xf2; Latin small letter o with grave */,
                "oline"    /* 8254 : &#x203e; overline */,
                "omega"    /*  969 : &#x3c9; Greek small letter omega */,
                "omicron"  /*  959 : &#x3bf; Greek small letter omicron */,
                "oplus"    /* 8853 : &#x2295; circled plus */,
                "or"       /* 8744 : &#x2228; vee */,
                "ordf"     /*  170 : &#xaa; feminine ordinal indicator */,
                "ordm"     /*  186 : &#xba; masculine ordinal indicator */,
                "oslash"   /*  248 : &#xf8; Latin small letter o with stroke */,
                "otilde"   /*  245 : &#xf5; Latin small letter o with tilde */,
                "otimes"   /* 8855 : &#x2297; circled times */,
                "ouml"     /*  246 : &#xf6; Latin small letter o with diaeresis */,
                "para"     /*  182 : &#xb6; pilcrow sign */,
                "part"     /* 8706 : &#x2202; partial differential */,
                "permil"   /* 8240 : &#x2030; per mille sign */,
                "perp"     /* 8869 : &#x22a5; up tack */,
                "phi"      /*  966 : &#x3c6; Greek small letter phi */,
                "pi"       /*  960 : &#x3c0; Greek small letter pi */,
                "piv"      /*  982 : &#x3d6; Greek pi symbol */,
                "plusmn"   /*  177 : &#xb1; plus-minus sign */,
                "pound"    /*  163 : &#xa3; pound sign */,
                "prime"    /* 8242 : &#x2032; prime */,
                "prod"     /* 8719 : &#x220f; n-ary product */,
                "prop"     /* 8733 : &#x221d; proportional to */,
                "psi"      /*  968 : &#x3c8; Greek small letter psi */,
                "quot"     /*   34 : &#x22; quotation mark */,
                "rArr"     /* 8658 : &#x21d2; rightwards double arrow */,
                "radic"    /* 8730 : &#x221a; square root */,
                "rang"     /* 9002 : &#x232a; right-pointing angle bracket */,
                "raquo"    /*  187 : &#xbb; right guillemot */,
                "rarr"     /* 8594 : &#x2192; rightwards arrow */,
                "rceil"    /* 8969 : &#x2309; right ceiling */,
                "rdquo"    /* 8221 : &#x201d; right double-99 quotation mark */,
                "real"     /* 8476 : &#x211c; black-letter capital r */,
                "reg"      /*  174 : &#xae; registered sign. circled R. */,
                "rfloor"   /* 8971 : &#x230b; right floor */,
                "rho"      /*  961 : &#x3c1; Greek small letter rho */,
                "rlm"      /* 8207 : &#x200f; right-to-left mark */,
                "rsaquo"   /* 8250 : &#x203a; single right-pointing angle quotation mark */,
                "rsquo"    /* 8217 : &#x2019; right single-9 quotation mark */,
                "sbquo"    /* 8218 : &#x201a; single low-9 quotation mark */,
                "scaron"   /*  353 : &#x161; Latin small letter s with caron */,
                "sdot"     /* 8901 : &#x22c5; dot operator */,
                "sect"     /*  167 : &#xa7; section sign */,
                "shy"      /*  173 : &#xad; soft hyphen */,
                "sigma"    /*  963 : &#x3c3; Greek small letter sigma */,
                "sigmaf"   /*  962 : &#x3c2; Greek small letter final sigma */,
                "sim"      /* 8764 : &#x223c; tilde operator */,
                "spades"   /* 9824 : &#x2660; black spade suit */,
                "sub"      /* 8834 : &#x2282; subset of */,
                "sube"     /* 8838 : &#x2286; subset of or equal to */,
                "sum"      /* 8721 : &#x2211; n-ary summation */,
                "sup1"     /*  185 : &#xb9; superscript one */,
                "sup2"     /*  178 : &#xb2; superscript two */,
                "sup3"     /*  179 : &#xb3; superscript three */,
                "sup"      /* 8835 : &#x2283; superset of */,
                "supe"     /* 8839 : &#x2287; superset of or equal to */,
                "szlig"    /*  223 : &#xdf; Latin small letter sharp s */,
                "tau"      /*  964 : &#x3c4; Greek small letter tau */,
                "there4"   /* 8756 : &#x2234; therefore three dots */,
                "theta"    /*  952 : &#x3b8; Greek small letter theta */,
                "thetasym" /*  977 : &#x3d1; Greek theta symbol */,
                "thinsp"   /* 8201 : &#x2009; thin space */,
                "thorn"    /*  254 : &#xfe; Latin small letter thorn */,
                "tilde"    /*  732 : &#x2dc; small tilde */,
                "times"    /*  215 : &#xd7; multiplication sign */,
                "trade"    /* 8482 : &#x2122; trademark sign */,
                "uArr"     /* 8657 : &#x21d1; upwards double arrow */,
                "uacute"   /*  250 : &#xfa; Latin small letter u with acute */,
                "uarr"     /* 8593 : &#x2191; upwards arrow */,
                "ucirc"    /*  251 : &#xfb; Latin small letter u with circumflex */,
                "ugrave"   /*  249 : &#xf9; Latin small letter u with grave */,
                "uml"      /*  168 : &#xa8; diaeresis */,
                "upsih"    /*  978 : &#x3d2; Greek upsilon with hook symbol */,
                "upsilon"  /*  965 : &#x3c5; Greek small letter upsilon */,
                "uuml"     /*  252 : &#xfc; Latin small letter u with diaeresis */,
                "weierp"   /* 8472 : &#x2118; script capital p */,
                "xi"       /*  958 : &#x3be; Greek small letter xi */,
                "yacute"   /*  253 : &#xfd; Latin small letter y with acute */,
                "yen"      /*  165 : &#xa5; yen sign */,
                "yuml"     /*  255 : &#xff; Latin small letter y with diaeresis */,
                "zeta"     /*  950 : &#x3b6; Greek small letter zeta */,
                "zwj"      /* 8205 : &#x200d; zero width joiner */,
                "zwnj"     /* 8204 : &#x200c; zero width non-joiner */,
        };
        char[] entityValues = {
                // W A R N I N G !  _ M A N U A L L Y  _ I N S E R T E D _ G E N E R A T E D  _ C O D E
                // generated by Entities. Insert from com\mindprod\entities\entitiesjustkeys.javafrag
                198 /* &AElig;    : &#xc6; Latin capital letter AE */,
                193 /* &Aacute;   : &#xc1; Latin capital letter A with acute */,
                194 /* &Acirc;    : &#xc2; Latin capital letter A with circumflex */,
                192 /* &Agrave;   : &#xc0; Latin capital letter A with grave */,
                913 /* &Alpha;    : &#x391; Greek capital letter Alpha */,
                197 /* &Aring;    : &#xc5; Latin capital letter A with ring above */,
                195 /* &Atilde;   : &#xc3; Latin capital letter A with tilde */,
                196 /* &Auml;     : &#xc4; Latin capital letter A with diaeresis */,
                914 /* &Beta;     : &#x392; Greek capital letter Beta */,
                199 /* &Ccedil;   : &#xc7; Latin capital letter C with cedilla */,
                935 /* &Chi;      : &#x3a7; Greek capital letter Chi */,
                8225 /* &Dagger;   : &#x2021; double dagger */,
                916 /* &Delta;    : &#x394; Greek capital letter Delta */,
                208 /* &ETH;      : &#xd0; Latin capital letter Eth */,
                201 /* &Eacute;   : &#xc9; Latin capital letter E with acute */,
                202 /* &Ecirc;    : &#xca; Latin capital letter E with circumflex */,
                200 /* &Egrave;   : &#xc8; Latin capital letter E with grave */,
                917 /* &Epsilon;  : &#x395; Greek capital letter Epsilon */,
                919 /* &Eta;      : &#x397; Greek capital letter Eta */,
                203 /* &Euml;     : &#xcb; Latin capital letter E with diaeresis */,
                915 /* &Gamma;    : &#x393; Greek capital letter Gamma */,
                205 /* &Iacute;   : &#xcd; Latin capital letter I with acute */,
                206 /* &Icirc;    : &#xce; Latin capital letter I with circumflex */,
                204 /* &Igrave;   : &#xcc; Latin capital letter I with grave */,
                921 /* &Iota;     : &#x399; Greek capital letter Iota */,
                207 /* &Iuml;     : &#xcf; Latin capital letter I with diaeresis */,
                922 /* &Kappa;    : &#x39a; Greek capital letter Kappa */,
                923 /* &Lambda;   : &#x39b; Greek capital letter Lambda */,
                924 /* &Mu;       : &#x39c; Greek capital letter Mu */,
                209 /* &Ntilde;   : &#xd1; Latin capital letter N with tilde */,
                925 /* &Nu;       : &#x39d; Greek capital letter Nu */,
                338 /* &OElig;    : &#x152; Latin capital ligature oe */,
                211 /* &Oacute;   : &#xd3; Latin capital letter O with acute */,
                212 /* &Ocirc;    : &#xd4; Latin capital letter O with circumflex */,
                210 /* &Ograve;   : &#xd2; Latin capital letter O with grave */,
                937 /* &Omega;    : &#x3a9; Greek capital letter Omega */,
                927 /* &Omicron;  : &#x39f; Greek capital letter Omicron */,
                216 /* &Oslash;   : &#xd8; Latin capital letter O with stroke */,
                213 /* &Otilde;   : &#xd5; Latin capital letter O with tilde */,
                214 /* &Ouml;     : &#xd6; Latin capital letter O with diaeresis */,
                934 /* &Phi;      : &#x3a6; Greek capital letter Phi */,
                928 /* &Pi;       : &#x3a0; Greek capital letter Pi */,
                8243 /* &Prime;    : &#x2033; double prime */,
                936 /* &Psi;      : &#x3a8; Greek capital letter Psi */,
                929 /* &Rho;      : &#x3a1; Greek capital letter Rho */,
                352 /* &Scaron;   : &#x160; Latin capital letter S with caron */,
                931 /* &Sigma;    : &#x3a3; Greek capital letter Sigma */,
                222 /* &THORN;    : &#xde; Latin capital letter Thorn */,
                932 /* &Tau;      : &#x3a4; Greek capital letter Tau */,
                920 /* &Theta;    : &#x398; Greek capital letter Theta */,
                218 /* &Uacute;   : &#xda; Latin capital letter U with acute */,
                219 /* &Ucirc;    : &#xdb; Latin capital letter U with circumflex */,
                217 /* &Ugrave;   : &#xd9; Latin capital letter U with grave */,
                933 /* &Upsilon;  : &#x3a5; Greek capital letter Upsilon */,
                220 /* &Uuml;     : &#xdc; Latin capital letter U with diaeresis */,
                926 /* &Xi;       : &#x39e; Greek capital letter Xi */,
                221 /* &Yacute;   : &#xdd; Latin capital letter Y with acute */,
                376 /* &Yuml;     : &#x178; Latin capital letter Y with diaeresis */,
                918 /* &Zeta;     : &#x396; Greek capital letter Zeta */,
                225 /* &aacute;   : &#xe1; Latin small letter a with acute */,
                226 /* &acirc;    : &#xe2; Latin small letter a with circumflex */,
                180 /* &acute;    : &#xb4; acute accent */,
                230 /* &aelig;    : &#xe6; Latin lowercase ligature ae */,
                224 /* &agrave;   : &#xe0; Latin small letter a with grave */,
                8501 /* &alefsym;  : &#x2135; alef symbol */,
                945 /* &alpha;    : &#x3b1; Greek small letter alpha */,
                38 /* &amp;      : &#x26; ampersand */,
                8743 /* &and;      : &#x2227; logical and */,
                8736 /* &ang;      : &#x2220; angle */,
                229 /* &aring;    : &#xe5; Latin small letter a with ring above */,
                8776 /* &asymp;    : &#x2248; asymptotic to */,
                227 /* &atilde;   : &#xe3; Latin small letter a with tilde */,
                228 /* &auml;     : &#xe4; Latin small letter a with diaeresis */,
                8222 /* &bdquo;    : &#x201e; double low-99 quotation mark */,
                946 /* &beta;     : &#x3b2; Greek small letter beta */,
                166 /* &brvbar;   : &#xa6; broken bar */,
                8226 /* &bull;     : &#x2022; bullet */,
                8745 /* &cap;      : &#x2229; intersection */,
                231 /* &ccedil;   : &#xe7; Latin small letter c with cedilla */,
                184 /* &cedil;    : &#xb8; cedilla */,
                162 /* &cent;     : &#xa2; cent sign */,
                967 /* &chi;      : &#x3c7; Greek small letter chi */,
                710 /* &circ;     : &#x2c6; modifier letter circumflex accent */,
                9827 /* &clubs;    : &#x2663; black club suit */,
                8773 /* &cong;     : &#x2245; congruent to */,
                169 /* &copy;     : &#xa9; copyright sign circled c */,
                8629 /* &crarr;    : &#x21b5; downwards arrow with corner leftwards */,
                8746 /* &cup;      : &#x222a; union */,
                164 /* &curren;   : &#xa4; currency sign */,
                8659 /* &dArr;     : &#x21d3; downwards double arrow */,
                8224 /* &dagger;   : &#x2020; dagger */,
                8595 /* &darr;     : &#x2193; downwards arrow */,
                176 /* &deg;      : &#xb0; degree sign */,
                948 /* &delta;    : &#x3b4; Greek small letter delta */,
                9830 /* &diams;    : &#x2666; black diamond suit */,
                247 /* &divide;   : &#xf7; division sign */,
                233 /* &eacute;   : &#xe9; Latin small letter e with acute */,
                234 /* &ecirc;    : &#xea; Latin small letter e with circumflex */,
                232 /* &egrave;   : &#xe8; Latin small letter e with grave */,
                8709 /* &empty;    : &#x2205; empty set */,
                8195 /* &emsp;     : &#x2003; em space */,
                8194 /* &ensp;     : &#x2002; en space */,
                949 /* &epsilon;  : &#x3b5; Greek small letter epsilon */,
                8801 /* &equiv;    : &#x2261; identical to */,
                951 /* &eta;      : &#x3b7; Greek small letter eta */,
                240 /* &eth;      : &#xf0; Latin small letter eth */,
                235 /* &euml;     : &#xeb; Latin small letter e with diaeresis */,
                8364 /* &euro;     : &#x20ac; Euro currency sign */,
                8707 /* &exist;    : &#x2203; there exists */,
                402 /* &fnof;     : &#x192; Latin small letter f with hook */,
                8704 /* &forall;   : &#x2200; for all */,
                189 /* &frac12;   : &#xbd; vulgar fraction 1/2 */,
                188 /* &frac14;   : &#xbc; vulgar fraction 1/4 */,
                190 /* &frac34;   : &#xbe; vulgar fraction 3/4 */,
                8260 /* &frasl;    : &#x2044; fraction slash */,
                947 /* &gamma;    : &#x3b3; Greek small letter gamma */,
                8805 /* &ge;       : &#x2265; greater-than or equal to */,
                62 /* &gt;       : &#x3e; greater-than sign */,
                8660 /* &hArr;     : &#x21d4; left right double arrow */,
                8596 /* &harr;     : &#x2194; left right arrow */,
                9829 /* &hearts;   : &#x2665; black heart suit */,
                8230 /* &hellip;   : &#x2026; horizontal ellipsis */,
                237 /* &iacute;   : &#xed; Latin small letter i with acute */,
                238 /* &icirc;    : &#xee; Latin small letter i with circumflex */,
                161 /* &iexcl;    : &#xa1; inverted exclamation mark */,
                236 /* &igrave;   : &#xec; Latin small letter i with grave */,
                8465 /* &image;    : &#x2111; black-letter capital i */,
                8734 /* &infin;    : &#x221e; infinity */,
                8747 /* &int;      : &#x222b; integral */,
                953 /* &iota;     : &#x3b9; Greek small letter iota */,
                191 /* &iquest;   : &#xbf; inverted question mark */,
                8712 /* &isin;     : &#x2208; element of */,
                239 /* &iuml;     : &#xef; Latin small letter i with diaeresis */,
                954 /* &kappa;    : &#x3ba; Greek small letter kappa */,
                8656 /* &lArr;     : &#x21d0; leftwards double arrow */,
                955 /* &lambda;   : &#x3bb; Greek small letter lambda */,
                9001 /* &lang;     : &#x2329; left-pointing angle bracket */,
                171 /* &laquo;    : &#xab; left guillemot */,
                8592 /* &larr;     : &#x2190; leftwards arrow */,
                8968 /* &lceil;    : &#x2308; left ceiling */,
                8220 /* &ldquo;    : &#x201c; left double-66 quotation mark */,
                8804 /* &le;       : &#x2264; less-than or equal to */,
                8970 /* &lfloor;   : &#x230a; left floor */,
                8727 /* &lowast;   : &#x2217; asterisk operator */,
                9674 /* &loz;      : &#x25ca; open lozenge */,
                8206 /* &lrm;      : &#x200e; left-to-right mark */,
                8249 /* &lsaquo;   : &#x2039; single left-pointing angle quotation mark */,
                8216 /* &lsquo;    : &#x2018; left single-6 quotation mark */,
                60 /* &lt;       : &#x3c; less-than sign */,
                175 /* &macr;     : &#xaf; macron */,
                8212 /* &mdash;    : &#x2014; em dash */,
                181 /* &micro;    : &#xb5; micro sign */,
                183 /* &middot;   : &#xb7; middle dot */,
                8722 /* &minus;    : &#x2212; minus sign */,
                956 /* &mu;       : &#x3bc; Greek small letter mu */,
                8711 /* &nabla;    : &#x2207; nabla */,
                160 /* &nbsp;     : &#xa0; non-breaking space */,
                8211 /* &ndash;    : &#x2013; en dash */,
                8800 /* &ne;       : &#x2260; not equal to */,
                8715 /* &ni;       : &#x220b; like backwards epsilon */,
                172 /* &not;      : &#xac; not sign */,
                8713 /* &notin;    : &#x2209; not an element of */,
                8836 /* &nsub;     : &#x2284; not a subset of */,
                241 /* &ntilde;   : &#xf1; Latin small letter n with tilde */,
                957 /* &nu;       : &#x3bd; Greek small letter nu */,
                243 /* &oacute;   : &#xf3; Latin small letter o with acute */,
                244 /* &ocirc;    : &#xf4; Latin small letter o with circumflex */,
                339 /* &oelig;    : &#x153; Latin small ligature oe */,
                242 /* &ograve;   : &#xf2; Latin small letter o with grave */,
                8254 /* &oline;    : &#x203e; overline */,
                969 /* &omega;    : &#x3c9; Greek small letter omega */,
                959 /* &omicron;  : &#x3bf; Greek small letter omicron */,
                8853 /* &oplus;    : &#x2295; circled plus */,
                8744 /* &or;       : &#x2228; vee */,
                170 /* &ordf;     : &#xaa; feminine ordinal indicator */,
                186 /* &ordm;     : &#xba; masculine ordinal indicator */,
                248 /* &oslash;   : &#xf8; Latin small letter o with stroke */,
                245 /* &otilde;   : &#xf5; Latin small letter o with tilde */,
                8855 /* &otimes;   : &#x2297; circled times */,
                246 /* &ouml;     : &#xf6; Latin small letter o with diaeresis */,
                182 /* &para;     : &#xb6; pilcrow sign */,
                8706 /* &part;     : &#x2202; partial differential */,
                8240 /* &permil;   : &#x2030; per mille sign */,
                8869 /* &perp;     : &#x22a5; up tack */,
                966 /* &phi;      : &#x3c6; Greek small letter phi */,
                960 /* &pi;       : &#x3c0; Greek small letter pi */,
                982 /* &piv;      : &#x3d6; Greek pi symbol */,
                177 /* &plusmn;   : &#xb1; plus-minus sign */,
                163 /* &pound;    : &#xa3; pound sign */,
                8242 /* &prime;    : &#x2032; prime */,
                8719 /* &prod;     : &#x220f; n-ary product */,
                8733 /* &prop;     : &#x221d; proportional to */,
                968 /* &psi;      : &#x3c8; Greek small letter psi */,
                34 /* &quot;     : &#x22; quotation mark */,
                8658 /* &rArr;     : &#x21d2; rightwards double arrow */,
                8730 /* &radic;    : &#x221a; square root */,
                9002 /* &rang;     : &#x232a; right-pointing angle bracket */,
                187 /* &raquo;    : &#xbb; right guillemot */,
                8594 /* &rarr;     : &#x2192; rightwards arrow */,
                8969 /* &rceil;    : &#x2309; right ceiling */,
                8221 /* &rdquo;    : &#x201d; right double-99 quotation mark */,
                8476 /* &real;     : &#x211c; black-letter capital r */,
                174 /* &reg;      : &#xae; registered sign. circled R. */,
                8971 /* &rfloor;   : &#x230b; right floor */,
                961 /* &rho;      : &#x3c1; Greek small letter rho */,
                8207 /* &rlm;      : &#x200f; right-to-left mark */,
                8250 /* &rsaquo;   : &#x203a; single right-pointing angle quotation mark */,
                8217 /* &rsquo;    : &#x2019; right single-9 quotation mark */,
                8218 /* &sbquo;    : &#x201a; single low-9 quotation mark */,
                353 /* &scaron;   : &#x161; Latin small letter s with caron */,
                8901 /* &sdot;     : &#x22c5; dot operator */,
                167 /* &sect;     : &#xa7; section sign */,
                173 /* &shy;      : &#xad; soft hyphen */,
                963 /* &sigma;    : &#x3c3; Greek small letter sigma */,
                962 /* &sigmaf;   : &#x3c2; Greek small letter final sigma */,
                8764 /* &sim;      : &#x223c; tilde operator */,
                9824 /* &spades;   : &#x2660; black spade suit */,
                8834 /* &sub;      : &#x2282; subset of */,
                8838 /* &sube;     : &#x2286; subset of or equal to */,
                8721 /* &sum;      : &#x2211; n-ary summation */,
                185 /* &sup1;     : &#xb9; superscript one */,
                178 /* &sup2;     : &#xb2; superscript two */,
                179 /* &sup3;     : &#xb3; superscript three */,
                8835 /* &sup;      : &#x2283; superset of */,
                8839 /* &supe;     : &#x2287; superset of or equal to */,
                223 /* &szlig;    : &#xdf; Latin small letter sharp s */,
                964 /* &tau;      : &#x3c4; Greek small letter tau */,
                8756 /* &there4;   : &#x2234; therefore three dots */,
                952 /* &theta;    : &#x3b8; Greek small letter theta */,
                977 /* &thetasym; : &#x3d1; Greek theta symbol */,
                8201 /* &thinsp;   : &#x2009; thin space */,
                254 /* &thorn;    : &#xfe; Latin small letter thorn */,
                732 /* &tilde;    : &#x2dc; small tilde */,
                215 /* &times;    : &#xd7; multiplication sign */,
                8482 /* &trade;    : &#x2122; trademark sign */,
                8657 /* &uArr;     : &#x21d1; upwards double arrow */,
                250 /* &uacute;   : &#xfa; Latin small letter u with acute */,
                8593 /* &uarr;     : &#x2191; upwards arrow */,
                251 /* &ucirc;    : &#xfb; Latin small letter u with circumflex */,
                249 /* &ugrave;   : &#xf9; Latin small letter u with grave */,
                168 /* &uml;      : &#xa8; diaeresis */,
                978 /* &upsih;    : &#x3d2; Greek upsilon with hook symbol */,
                965 /* &upsilon;  : &#x3c5; Greek small letter upsilon */,
                252 /* &uuml;     : &#xfc; Latin small letter u with diaeresis */,
                8472 /* &weierp;   : &#x2118; script capital p */,
                958 /* &xi;       : &#x3be; Greek small letter xi */,
                253 /* &yacute;   : &#xfd; Latin small letter y with acute */,
                165 /* &yen;      : &#xa5; yen sign */,
                255 /* &yuml;     : &#xff; Latin small letter y with diaeresis */,
                950 /* &zeta;     : &#x3b6; Greek small letter zeta */,
                8205 /* &zwj;      : &#x200d; zero width joiner */,
                8204 /* &zwnj;     : &#x200c; zero width non-joiner */,
        };
        // allow 50% extra space for faster lookup.
        entityToChar = new HashMap<String, Character>( entityKeys.length * 150 / 100 );
        for ( int i = 0; i < entityKeys.length; i++ )
        {
            // leave out nbsp so it can be specially handled if entity not found.
            if ( !entityKeys[ i ].equals( "nbsp" ) )
            {
                entityToChar.put( entityKeys[ i ], entityValues[ i ] );
            }
            // add also &apos; for strip but not insert. optional for XML, not used in HTML.
            entityToChar.put( "apos", ( char ) 39 );
        }
    }// end static

    /**
     * Checks a number of gauntlet conditions to ensure this is a valid entity. Converts Entity to corresponding char.
     * Does not deal with HTML5 entities.
     *
     * @param possBareEntityWithSemicolon string that may hold an entity. Lead & must be stripped,
     *                                    but may optionally contain text past the ;
     * @param translateNbspTo             char you would like nbsp translated to, usually ' ' or (char) 160 .
     *
     * @return corresponding unicode character, or 0 if the entity is invalid.
     * @noinspection WeakerAccess
     */
    protected static char possBareHTMLEntityWithSemicolonToChar( String possBareEntityWithSemicolon,
                                                                 char translateNbspTo )
    {
        if ( possBareEntityWithSemicolon.length() < SHORTEST_HTML4_ENTITY - 1 )
        {
            return 0;
        }
        // find the trailing ;
        int whereSemi = possBareEntityWithSemicolon
                .indexOf( ';', SHORTEST_HTML4_ENTITY - 2/* where start looking */ );
        if ( whereSemi < SHORTEST_HTML4_ENTITY - 2 )
        {
            return 0;
        }
        return bareHTMLEntityToChar( possBareEntityWithSemicolon.substring( 0, whereSemi ), translateNbspTo );
    }

    /**
     * Prepares tags for removal, to ensure they are replaced by a space
     * <tr><td><th><br><br /><p>  </tr></td></th></br></p>   --> _<tr  _</tr etc.
     *
     * @param html input HTML or XML
     *
     * @return raw text, with spacing tags fluffed up with a space so they will later be removed with a space.
     */
    private static String preStripIndividualTags( String html )
    {
        StringBuilder sb = new StringBuilder( html.length() * 110 / 100 );
        char prevChar = 0;
        for ( int i = 0; i < html.length(); i++ )
        {
            final char c = html.charAt( i );
            if ( c == '<' )
            {
                // startsWith index need not be inside the String.
                int look = ( html.startsWith( "/", i + 1 ) ) ? i + 2 : i + 1;
                // handle <tr  </tr etc.
                for ( String tag : spacingTags )
                {
                    if ( html.startsWith( tag, look ) )
                    {
                        // no need to add space if one there already.
                        if ( prevChar > ' ' )
                        {
                            // insert space before <
                            sb.append( ' ' );
                        }
                        break;
                    }
                }
            }
            sb.append( c );
            prevChar = c;
        }
        return sb.toString();
    }

    /**
     * remove all text between &lt;applet.. &lt;/applet&gt;, &lt;style... &lt;/style&gt; &lt;script... &lt;/script&gt;
     *
     * @param s HTML string to strip tag pairs out of.
     *
     * @return string with tag pairs stripped out.
     */
    private static String stripHTMLTagPairs( String s )
    {
        String[] tags =
                { "applet", "APPLET", "style", "STYLE", "script", "SCRIPT" };
        for ( final String tag : tags )
        {
            final String beginTag = "<" + tag;
            final String endTag = "</" + tag + ">";
            int begin = 0;
            while ( begin < s.length()
                    && ( begin = s.indexOf( beginTag, begin ) ) >= 0 )
            {
                final int end;
                if ( ( end = s.indexOf( endTag, begin + beginTag.length() ) )
                        > 0 )
                {
                    // chop out the <applet ... </applet>
                    s = s.substring( 0, begin ) + s.substring( end + endTag.length() );
                }
                else
                {
                    // no matching end tag, chop off entire end
                    s = s.substring( 0, begin );
                }
            }
        }
        return s;
    }

    /**
     * Removes tags from HTML leaving just the raw text. Leaves entities as is, e.g. Presumes perfectly formed HTML.
     * <strong> </strong> <em> </em> <tr xxx> etc removed leaving nothing behind.
     *
     * @param html input HTML or XML
     *
     * @return raw text, with whitespaces collapsed to a single space, trimmed.
     * @noinspection WeakerAccess
     */
    private static String stripIndividualTags( String html )
    {
        html = html.trim();
        // condition  String so that some tags will always turn into space.
        html = preStripIndividualTags( html );
        int numChars = html.length();
        // will only shrink. Don't use FastCat
        final StringBuilder sb = new StringBuilder( numChars );
        /**
         * are we inside a tag, eg. inside <td xxxx>
         */
        boolean inside = false;
        /**
         * Have we cleaned any White Space?
         */
        boolean cleanedAnyWhitespace = false;
        /**
         * Was the last char we saw a space? We use this to collapse spaces.
         */
        boolean lastCharSpace = false;
        for ( int i = 0; i < numChars; i++ )
        {
            char c = html.charAt( i );
            switch ( c )
            {
                default:
                    if ( c < ' ' )
                    {
                        // handle stray whitespace
                        if ( !inside )
                        {
                            lastCharSpace = true;
                            cleanedAnyWhitespace = true;
                        }
                    }
                    else
                    {
                        // ordinary character, ignored inside a tag
                        if ( !inside )
                        {
                            if ( lastCharSpace )
                            {
                                // deal with pending whitespace
                                sb.append( ' ' );
                                lastCharSpace = false;
                            }
                            sb.append( c );
                        }
                    }
                    break;
                case '<':
                    inside = true;
                    // ignore
                    break;
                case '>':
                    inside = false;
                    // ignore
                    break;
                case ' ':
                    if ( !inside )
                    {
                        lastCharSpace = true;
                    }
                    break;
                // whitespace
                case '\r':
                case '\t':
                case '\n':
                case 127:
                case UNICODE_NBSP_160_0x0a:
                    if ( !inside )
                    {
                        lastCharSpace = true;
                        cleanedAnyWhitespace = true;
                    }
                    break;
            }
        }// end for
        // return original string if we did not really change anything
        final String result = ( cleanedAnyWhitespace || sb.length() != numChars ) ? sb
                .toString() : html;
        return condense( result ); // collapse multiple spaces.
    }

    /**
     * Collapse multiple whitespace in string down to a single space. Remove lead and trailing whitespace.
     * Earlier version collapsed only spaces, not whitespace
     *
     * @param s String to condense whitespace.
     *
     * @return String with all whitespace condensed and lead/trail whitespace removed.
     * @noinspection WeakerAccess, SameParameterValue
     * @see #squish(String)
     * @see com.mindprod.common11.ST#condense(String)
     */
    public static String condense( String s )
    {
        if ( s == null )
        {
            return null;
        }
        s = s.trim();

        final int len = s.length();
        if ( len == 0 )
        {
            return s;
        }
        // StringBuilder is faster than FastCat for char by char work
        StringBuilder b = new StringBuilder( len );
        boolean suppressSpaces = false;
        for ( int i = 0; i < len; i++ )
        {
            char c = s.charAt( i );
            if ( Character.isWhitespace( c ) )
            {
                if ( suppressSpaces )
                {
                    // subsequent space
                }
                else
                {
                    // first space
                    b.append( ' ' );
                    suppressSpaces = true;
                }
            }
            else
            {
                // was not a space
                b.append( c );
                suppressSpaces = false;
            }
        }// end for
        return b.toString();
    }

    //    /**
    //     * Test harness.
    //    //     * @param args not used.
    //    //     * @noinspection ConstantConditions
    //     */
    //    public static void main
    //    ( String[] args )
    //    {
    //    if ( DEBUGGING )
    //        {
    //        out.println( deEntifyHTML( " Bed &amp; Breakfast ", ' ' ) );
    //        out.println( stripHTMLTags( " <a href=\"ibm.html\">big blue</a> " ) );
    //        out.println( stripHTMLTags( "<a href=\"ibm.html\">big\nblue</a>" ) );
    //        out.println( stripHTMLTags( "big\nblue" ) );
    //        out.println( stripHTMLTags( "big blue" ) );
    //        out.println( stripHTMLTags( "big<br>blue" ) );
    //        out.println( stripHTMLTags( "big\n<br />    blue" ) );
    //        }
    //    }
}

// end DeEntifyStrings