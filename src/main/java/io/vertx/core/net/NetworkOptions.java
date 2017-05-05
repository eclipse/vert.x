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

package io.vertx.core.net;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonObject;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
// TODO: 16/12/18 by zmyer
@DataObject(generateConverter = true)
public abstract class NetworkOptions {

    /**
     * The default value of TCP send buffer size
     */
    //默认的发送缓冲区大小
    public static final int DEFAULT_SEND_BUFFER_SIZE = -1;

    /**
     * The default value of TCP receive buffer size
     */
    //默认的接受缓冲区大小
    public static final int DEFAULT_RECEIVE_BUFFER_SIZE = -1;

    /**
     * The default value of traffic class
     */
    //默认的流控类对象
    public static final int DEFAULT_TRAFFIC_CLASS = -1;

    /**
     * The default value of reuse address
     */
    //地址重用标记
    public static final boolean DEFAULT_REUSE_ADDRESS = true;

    /**
     * The default log enabled = false
     */
    //日志开关
    public static final boolean DEFAULT_LOG_ENABLED = false;

    private int sendBufferSize;
    private int receiveBufferSize;
    private int trafficClass;
    private boolean reuseAddress;
    private boolean logActivity;

    /**
     * Default constructor
     */
    public NetworkOptions() {
        sendBufferSize = DEFAULT_SEND_BUFFER_SIZE;
        receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
        reuseAddress = DEFAULT_REUSE_ADDRESS;
        trafficClass = DEFAULT_TRAFFIC_CLASS;
        logActivity = DEFAULT_LOG_ENABLED;
    }

    /**
     * Copy constructor
     *
     * @param other the options to copy
     */
    // TODO: 16/12/18 by zmyer
    public NetworkOptions(NetworkOptions other) {
        this.sendBufferSize = other.getSendBufferSize();
        this.receiveBufferSize = other.getReceiveBufferSize();
        this.reuseAddress = other.isReuseAddress();
        this.trafficClass = other.getTrafficClass();
        this.logActivity = other.logActivity;
    }

    /**
     * Constructor from JSON
     *
     * @param json the JSON
     */
    // TODO: 16/12/18 by zmyer
    public NetworkOptions(JsonObject json) {
        this();
        NetworkOptionsConverter.fromJson(json, this);
    }

    /**
     * Return the TCP send buffer size, in bytes.
     *
     * @return the send buffer size
     */
    public int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Set the TCP send buffer size
     *
     * @param sendBufferSize the buffers size, in bytes
     * @return a reference to this, so the API can be used fluently
     */
    // TODO: 16/12/18 by zmyer
    public NetworkOptions setSendBufferSize(int sendBufferSize) {
        Arguments.require(sendBufferSize > 0 || sendBufferSize == DEFAULT_SEND_BUFFER_SIZE, "sendBufferSize must be > 0");
        this.sendBufferSize = sendBufferSize;
        return this;
    }

    /**
     * Return the TCP receive buffer size, in bytes
     *
     * @return the receive buffer size
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Set the TCP receive buffer size
     *
     * @param receiveBufferSize the buffers size, in bytes
     * @return a reference to this, so the API can be used fluently
     */
    // TODO: 16/12/18 by zmyer
    public NetworkOptions setReceiveBufferSize(int receiveBufferSize) {
        Arguments.require(receiveBufferSize > 0 || receiveBufferSize == DEFAULT_RECEIVE_BUFFER_SIZE, "receiveBufferSize must be > 0");
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    /**
     * @return the value of reuse address
     */
    public boolean isReuseAddress() {
        return reuseAddress;
    }

    /**
     * Set the value of reuse address
     *
     * @param reuseAddress the value of reuse address
     * @return a reference to this, so the API can be used fluently
     */
    public NetworkOptions setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
        return this;
    }

    /**
     * @return the value of traffic class
     */
    public int getTrafficClass() {
        return trafficClass;
    }

    /**
     * Set the value of traffic class
     *
     * @param trafficClass the value of traffic class
     * @return a reference to this, so the API can be used fluently
     */
    public NetworkOptions setTrafficClass(int trafficClass) {
        Arguments.requireInRange(trafficClass, DEFAULT_TRAFFIC_CLASS, 255, "trafficClass tc must be 0 <= tc <= 255");
        this.trafficClass = trafficClass;
        return this;
    }

    /**
     * @return true when network activity logging is enabled
     */
    public boolean getLogActivity() {
        return logActivity;
    }

    /**
     * Set to true to enabled network activity logging: Netty's pipeline is configured for logging on Netty's logger.
     *
     * @param logActivity true for logging the network activity
     * @return a reference to this, so the API can be used fluently
     */
    public NetworkOptions setLogActivity(boolean logActivity) {
        this.logActivity = logActivity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkOptions)) return false;

        NetworkOptions that = (NetworkOptions) o;

        if (receiveBufferSize != that.receiveBufferSize) return false;
        if (reuseAddress != that.reuseAddress) return false;
        if (sendBufferSize != that.sendBufferSize) return false;
        if (trafficClass != that.trafficClass) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sendBufferSize;
        result = 31 * result + receiveBufferSize;
        result = 31 * result + trafficClass;
        result = 31 * result + (reuseAddress ? 1 : 0);
        return result;
    }
}
