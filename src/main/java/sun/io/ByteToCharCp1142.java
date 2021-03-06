/*
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.io;

/**
 * A table to convert Cp1142 to Unicode.  This converter differs from
 * Cp277 is one code point, 0x5A, which changes from \u00A4 to \u20AC.
 * @author  Alan Liu
 */
public class ByteToCharCp1142 extends ByteToCharCp277 {
    public ByteToCharCp1142() {}

    public String getCharacterEncoding() {
        return "Cp1142";
    }

    protected char getUnicode(int byteIndex) {
        // Change single code point with respect to parent.
        // [Careful -- if the code point in question is >= 0x80, make
        //  sure you do the comparison like this: (byteIndex == (byte)0x??)]
        return (byteIndex == 0x5A) ? '\u20AC' : super.getUnicode(byteIndex);
    }
}

//eof
