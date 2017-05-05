/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.core.impl.verticle;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;

/**
 * @author Janne Hietam&auml;ki
 */
// TODO: 17/1/1 by zmyer
public class CustomJavaFileObject implements JavaFileObject {
    //二进制类名称
    private final String binaryName;
    //文件对象类型信息
    private final Kind kind;
    //资源定位符信息
    private final URI uri;

    // TODO: 17/1/1 by zmyer
    protected CustomJavaFileObject(URI uri, Kind kind, String binaryName) {
        this.uri = uri;
        this.kind = kind;
        this.binaryName = binaryName;
    }

    public String binaryName() {
        return binaryName;
    }

    // TODO: 17/1/1 by zmyer
    @Override
    public InputStream openInputStream() throws IOException {
        return uri.toURL().openStream();
    }

    public Kind getKind() {
        return kind;
    }

    public NestingKind getNestingKind() {
        return null;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    public String getName() {
        return toUri().getPath();
    }

    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    public long getLastModified() {
        return 0L;
    }

    public boolean delete() {
        return false;
    }

    public boolean isNameCompatible(String simpleName, Kind kind) {
        String name = simpleName + kind.extension;
        return (name.equals(toUri().getPath()) || toUri().getPath().endsWith('/' + name)) && kind.equals(getKind());
    }

    public Modifier getAccessLevel() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + '[' + toUri() + ']';
    }
}