/*
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

/*
 *
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 */

package sun.security.krb5.internal.crypto;

import sun.security.krb5.*;
import sun.security.krb5.internal.*;
import java.util.zip.CRC32;

public class Crc32CksumType extends CksumType {

    public Crc32CksumType() {
    }

    public int confounderSize() {
        return 0;
    }

    public int cksumType() {
        return Checksum.CKSUMTYPE_CRC32;
    }

    public boolean isSafe() {
        return false;
    }

    public int cksumSize() {
        return 4;
    }

    public int keyType() {
        return Krb5.KEYTYPE_NULL;
    }

    public int keySize() {
        return 0;
    }

    public byte[] calculateChecksum(byte[] data, int size) {
        return crc32.byte2crc32sum_bytes(data, size);
    }

    public byte[] calculateKeyedChecksum(byte[] data, int size,
                                         byte[] key, int usage) {
                                             return null;
                                         }

    public boolean verifyKeyedChecksum(byte[] data, int size,
                                       byte[] key, byte[] checksum, int usage) {
        return false;
    }

    public static byte[] int2quad(long input) {
        byte[] output = new byte[4];
        for (int i = 0; i < 4; i++) {
            output[i] = (byte)((input >>> (i * 8)) & 0xff);
        }
        return output;
    }

    public static long bytes2long(byte[] input) {
        long result = 0;

        result |= (((long)input[0]) & 0xffL) << 24;
        result |= (((long)input[1]) & 0xffL) << 16;
        result |= (((long)input[2]) & 0xffL) << 8;
        result |= (((long)input[3]) & 0xffL);
        return result;
    }
}
