/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.xjc.generator.annotation.spec;

import javax.xml.bind.annotation.XmlElementWrapper;
import com.sun.codemodel.internal.JAnnotationWriter;

public interface XmlElementWrapperWriter
    extends JAnnotationWriter<XmlElementWrapper>
{


    XmlElementWrapperWriter name(String value);

    XmlElementWrapperWriter namespace(String value);

    XmlElementWrapperWriter required(boolean value);

    XmlElementWrapperWriter nillable(boolean value);

}
