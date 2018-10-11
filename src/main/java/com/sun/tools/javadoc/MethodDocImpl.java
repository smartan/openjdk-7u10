/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.javadoc;

import com.sun.javadoc.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Position;

import java.lang.reflect.Modifier;

/**
 * Represents a method of a java class.
 *
 * @since 1.2
 * @author Robert Field
 * @author Neal Gafter (rewrite)
 */

public class MethodDocImpl
        extends ExecutableMemberDocImpl implements MethodDoc {

    /**
     * constructor.
     */
    public MethodDocImpl(DocEnv env, MethodSymbol sym) {
        super(env, sym);
    }

    /**
     * constructor.
     */
    public MethodDocImpl(DocEnv env, MethodSymbol sym,
                         String docComment, JCMethodDecl tree, Position.LineMap lineMap) {
        super(env, sym, docComment, tree, lineMap);
    }

    /**
     * Return true if it is a method, which it is.
     * Note: constructors are not methods.
     * This method is overridden by AnnotationTypeElementDocImpl.
     *
     * @return true
     */
    public boolean isMethod() {
        return true;
    }

    /**
     * Return true if this method is abstract
     */
    public boolean isAbstract() {
        //### This is dubious, but old 'javadoc' apparently does it.
        //### I regard this as a bug and an obstacle to treating the
        //### doclet API as a proper compile-time reflection facility.
        //### (maddox 09/26/2000)
        if (containingClass().isInterface()) {
            //### Don't force creation of ClassDocImpl for super here.
            // Abstract modifier is implicit.  Strip/canonicalize it.
            return false;
        }
        return Modifier.isAbstract(getModifiers());
    }

    /**
     * Get return type.
     *
     * @return the return type of this method, null if it
     * is a constructor.
     */
    public com.sun.javadoc.Type returnType() {
        return TypeMaker.getType(env, sym.type.getReturnType(), false);
    }

    /**
     * Return the class that originally defined the method that
     * is overridden by the current definition, or null if no
     * such class exists.
     *
     * @return a ClassDocImpl representing the superclass that
     * originally defined this method, null if this method does
     * not override a definition in a superclass.
     */
    public ClassDoc overriddenClass() {
        com.sun.javadoc.Type t = overriddenType();
        return (t != null) ? t.asClassDoc() : null;
    }

    /**
     * Return the type containing the method that this method overrides.
     * It may be a <code>ClassDoc</code> or a <code>ParameterizedType</code>.
     */
    public com.sun.javadoc.Type overriddenType() {

        if ((sym.flags() & Flags.STATIC) != 0) {
            return null;
        }

        ClassSymbol origin = (ClassSymbol)sym.owner;
        for (Type t = env.types.supertype(origin.type);
             t.tag == TypeTags.CLASS;
             t = env.types.supertype(t)) {
            ClassSymbol c = (ClassSymbol)t.tsym;
            for (Scope.Entry e = c.members().lookup(sym.name); e.scope != null; e = e.next()) {
                if (sym.overrides(e.sym, origin, env.types, true)) {
                    return TypeMaker.getType(env, t);
                }
            }
        }
        return null;
    }

    /**
     * Return the method that this method overrides.
     *
     * @return a MethodDoc representing a method definition
     * in a superclass this method overrides, null if
     * this method does not override.
     */
    public MethodDoc overriddenMethod() {

        // Real overriding only.  Static members are simply hidden.
        // Likewise for constructors, but the MethodSymbol.overrides
        // method takes this into account.
        if ((sym.flags() & Flags.STATIC) != 0) {
            return null;
        }

        // Derived from  com.sun.tools.javac.comp.Check.checkOverride .

        ClassSymbol origin = (ClassSymbol)sym.owner;
        for (Type t = env.types.supertype(origin.type);
             t.tag == TypeTags.CLASS;
             t = env.types.supertype(t)) {
            ClassSymbol c = (ClassSymbol)t.tsym;
            for (Scope.Entry e = c.members().lookup(sym.name); e.scope != null; e = e.next()) {
                if (sym.overrides(e.sym, origin, env.types, true)) {
                    return env.getMethodDoc((MethodSymbol)e.sym);
                }
            }
        }
        return null;
    }

    /**
     * Tests whether this method overrides another.
     * The overridden method may be one declared in a superclass or
     * a superinterface (unlike {@link #overriddenMethod()}).
     *
     * <p> When a non-abstract method overrides an abstract one, it is
     * also said to <i>implement</i> the other.
     *
     * @param meth  the other method to examine
     * @return <tt>true</tt> if this method overrides the other
     */
    public boolean overrides(MethodDoc meth) {
        MethodSymbol overridee = ((MethodDocImpl) meth).sym;
        ClassSymbol origin = (ClassSymbol) sym.owner;

        return sym.name == overridee.name &&

               // not reflexive as per JLS
               sym != overridee &&

               // we don't care if overridee is static, though that wouldn't
               // compile
               !sym.isStatic() &&

               // sym, whose declaring type is the origin, must be
               // in a subtype of overridee's type
               env.types.asSuper(origin.type, overridee.owner) != null &&

               // check access and signatures; don't check return types
               sym.overrides(overridee, origin, env.types, false);
    }


    public String name() {
        return sym.name.toString();
    }

    public String qualifiedName() {
        return sym.enclClass().getQualifiedName() + "." + sym.name;
    }

    /**
     * Returns a string representation of this method.  Includes the
     * qualified signature, the qualified method name, and any type
     * parameters.  Type parameters follow the class name, as they do
     * in the syntax for invoking methods with explicit type parameters.
     */
    public String toString() {
        return sym.enclClass().getQualifiedName() +
                "." + typeParametersString() + name() + signature();
    }
}
