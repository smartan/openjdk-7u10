/*
 * Copyright (c) 2000, 2010, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.krb5;

import sun.security.krb5.internal.*;
import sun.security.krb5.internal.crypto.Nonce;
import sun.security.krb5.internal.crypto.KeyUsage;
import java.io.IOException;

/**
 * This class encapsulates the KRB-AS-REQ message that the client
 * sends to the KDC.
 */
public class KrbAsReq {
    private ASReq asReqMessg;

    private boolean DEBUG = Krb5.DEBUG;

    /**
     * Constructs an AS-REQ message.
     */
                                                // Can be null? has default?
    public KrbAsReq(EncryptionKey pakey,        // ok
                      KDCOptions options,       // ok, new KDCOptions()
                      PrincipalName cname,      // NO and must have realm
                      PrincipalName sname,      // ok, krgtgt@CREALM
                      KerberosTime from,        // ok
                      KerberosTime till,        // ok, will use
                      KerberosTime rtime,       // ok
                      int[] eTypes,             // NO
                      HostAddresses addresses   // ok
                      )
            throws KrbException, IOException {

        if (options == null) {
            options = new KDCOptions();
        }

        // check if they are valid arguments. The optional fields should be
        // consistent with settings in KDCOptions. Mar 17 2000
        if (options.get(KDCOptions.FORWARDED) ||
            options.get(KDCOptions.PROXY) ||
            options.get(KDCOptions.ENC_TKT_IN_SKEY) ||
            options.get(KDCOptions.RENEW) ||
            options.get(KDCOptions.VALIDATE)) {
            // this option is only specified in a request to the
            // ticket-granting server
            throw new KrbException(Krb5.KRB_AP_ERR_REQ_OPTIONS);
        }
        if (options.get(KDCOptions.POSTDATED)) {
            //  if (from == null)
            //          throw new KrbException(Krb5.KRB_AP_ERR_REQ_OPTIONS);
        } else {
            if (from != null)  from = null;
        }
        if (options.get(KDCOptions.RENEWABLE)) {
            //  if (rtime == null)
            //          throw new KrbException(Krb5.KRB_AP_ERR_REQ_OPTIONS);
        } else {
            if (rtime != null)  rtime = null;
        }

        PAData[] paData = null;
        if (pakey != null) {
            PAEncTSEnc ts = new PAEncTSEnc();
            byte[] temp = ts.asn1Encode();
            EncryptedData encTs = new EncryptedData(pakey, temp,
                KeyUsage.KU_PA_ENC_TS);
            paData = new PAData[1];
            paData[0] = new PAData( Krb5.PA_ENC_TIMESTAMP,
                                    encTs.asn1Encode());
        }

        if (cname.getRealm() == null) {
            throw new RealmException(Krb5.REALM_NULL,
                                     "default realm not specified ");
        }

        if (DEBUG) {
            System.out.println(">>> KrbAsReq creating message");
        }

        // check to use addresses in tickets
        if (addresses == null && Config.getInstance().useAddresses()) {
            addresses = HostAddresses.getLocalAddresses();
        }

        if (sname == null) {
            sname = new PrincipalName("krbtgt" +
                                      PrincipalName.NAME_COMPONENT_SEPARATOR +
                                      cname.getRealmAsString(),
                            PrincipalName.KRB_NT_SRV_INST);
        }

        if (till == null) {
            till = new KerberosTime(0); // Choose KDC maximum allowed
        }

        // enc-authorization-data and additional-tickets never in AS-REQ
        KDCReqBody kdc_req_body = new KDCReqBody(options,
                                                 cname,
                                                 cname.getRealm(),
                                                 sname,
                                                 from,
                                                 till,
                                                 rtime,
                                                 Nonce.value(),
                                                 eTypes,
                                                 addresses,
                                                 null,
                                                 null);

        asReqMessg = new ASReq(
                         paData,
                         kdc_req_body);
    }

    byte[] encoding() throws IOException, Asn1Exception {
        return asReqMessg.asn1Encode();
    }

    // Used by KrbAsRep to validate AS-REP
    ASReq getMessage() {
        return asReqMessg;
    }
}
