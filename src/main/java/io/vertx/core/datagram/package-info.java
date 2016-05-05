/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/**
 * == Datagram sockets (UDP)
 * <p>
 * Using User Datagram Protocol (UDP) with Vert.x is a piece of cake.
 * <p>
 * UDP is a connection-less transport which basically means you have no persistent connection to a remote peer.
 * <p>
 * Instead you can send and receive packages and the remote address is contained in each of them.
 * <p>
 * Beside this UDP is not as safe as TCP to use, which means there are no guarantees that a send Datagram packet will
 * receive it's endpoint at all.
 * <p>
 * The only guarantee is that it will either receive complete or not at all.
 * <p>
 * Also you usually can't send data which is bigger then the MTU size of your network interface, this is because each
 * packet will be send as one packet.
 * <p>
 * But be aware even if the packet size is smaller then the MTU it may still fail.
 * <p>
 * At which size it will fail depends on the Operating System etc. So rule of thumb is to try to send small packets.
 * <p>
 * Because of the nature of UDP it is best fit for Applications where you are allowed to drop packets (like for
 * example a monitoring application).
 * <p>
 * The benefits are that it has a lot less overhead compared to TCP, which can be handled by the NetServer
 * and NetClient (see above).
 * <p>
 * === Creating a DatagramSocket
 * <p>
 * To use UDP you first need t create a {@link io.vertx.core.datagram.DatagramSocket}. It does not matter here if you only want to send data or send
 * and receive.
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example1}
 * ----
 * <p>
 * The returned {@link io.vertx.core.datagram.DatagramSocket} will not be bound to a specific port. This is not a
 * problem if you only want to send data (like a client), but more on this in the next section.
 * <p>
 * === Sending Datagram packets
 * <p>
 * As mentioned before, User Datagram Protocol (UDP) sends data in packets to remote peers but is not connected to
 * them in a persistent fashion.
 * <p>
 * This means each packet can be sent to a different remote peer.
 * <p>
 * Sending packets is as easy as shown here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example2}
 * ----
 * <p>
 * === Receiving Datagram packets
 * <p>
 * If you want to receive packets you need to bind the {@link io.vertx.core.datagram.DatagramSocket} by calling
 * `listen(...)}` on it.
 * <p>
 * This way you will be able to receive {@link io.vertx.core.datagram.DatagramPacket}s that were sent to the address and port on
 * which the {@link io.vertx.core.datagram.DatagramSocket} listens.
 * <p>
 * Beside this you also want to set a `Handler` which will be called for each received {@link io.vertx.core.datagram.DatagramPacket}.
 * <p>
 * The {@link io.vertx.core.datagram.DatagramPacket} has the following methods:
 * <p>
 * - {@link io.vertx.core.datagram.DatagramPacket#sender()}: The InetSocketAddress which represent the sender of the packet
 * - {@link io.vertx.core.datagram.DatagramPacket#data()}: The Buffer which holds the data which was received.
 * <p>
 * So to listen on a specific address and port you would do something like shown here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example3}
 * ----
 * <p>
 * Be aware that even if the {code AsyncResult} is successed it only means it might be written on the network
 * stack, but gives no guarantee that it ever reached or will reach the remote peer at all.
 * <p>
 * If you need such a guarantee then you want to use TCP with some handshaking logic build on top.
 * <p>
 * === Multicast
 * <p>
 * ==== Sending Multicast packets
 * <p>
 * Multicast allows multiple sockets to receive the same packets. This works by have same join a multicast group
 * to which you can send packets.
 * <p>
 * We will look at how you can joint a Multicast Group and so receive packets in the next section.
 * <p>
 * For now let us focus on how to send those. Sending multicast packets is not different to send normal Datagram Packets.
 * <p>
 * The only difference is that you would pass in a multicast group address to the send method.
 * <p>
 * This is show here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example4}
 * ----
 * <p>
 * All sockets that have joined the multicast group 230.0.0.1 will receive the packet.
 * <p>
 * ===== Receiving Multicast packets
 * <p>
 * If you want to receive packets for specific Multicast group you need to bind the {@link io.vertx.core.datagram.DatagramSocket} by
 * calling `listen(...)` on it and join the Multicast group.
 * <p>
 * This way you will be able to receive DatagramPackets that were sent to the address and port on which the
 * {@link io.vertx.core.datagram.DatagramSocket} listens and also to those sent to the Multicast group.
 * <p>
 * Beside this you also want to set a Handler which will be called for each received DatagramPacket.
 * <p>
 * The {@link io.vertx.core.datagram.DatagramPacket} has the following methods:
 * <p>
 * - `sender()`: The InetSocketAddress which represent the sender of the packet
 * - `data()`: The Buffer which holds the data which was received.
 * <p>
 * So to listen on a specific address and port and also receive packets for the Multicast group 230.0.0.1 you
 * would do something like shown here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example5}
 * ----
 * <p>
 * ===== Unlisten / leave a Multicast group
 * <p>
 * There are sometimes situations where you want to receive packets for a Multicast group for a limited time.
 * <p>
 * In this situations you can first start to listen for them and then later unlisten.
 * <p>
 * This is shown here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example6}
 * ----
 * <p>
 * ===== Blocking multicast
 * <p>
 * Beside unlisten a Multicast address it's also possible to just block multicast for a specific sender address.
 * <p>
 * Be aware this only work on some Operating Systems and kernel versions. So please check the Operating System
 * documentation if it's supported.
 * <p>
 * This an expert feature.
 * <p>
 * To block multicast from a specific address you can call `blockMulticastGroup(...)` on the DatagramSocket
 * like shown here:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.DatagramExamples#example7}
 * ----
 * <p>
 * ==== DatagramSocket properties
 * <p>
 * When creating a {@link io.vertx.core.datagram.DatagramSocket} there are multiple properties you can set to
 * change it's behaviour with the {@link io.vertx.core.datagram.DatagramSocketOptions} object. Those are listed here:
 * <p>
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setSendBufferSize(int)} Sets the send buffer size in bytes.
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setReceiveBufferSize(int)} Sets the TCP receive buffer size
 * in bytes.
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setReuseAddress(boolean)} If true then addresses in TIME_WAIT
 * state can be reused after they have been closed.
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setTrafficClass(int)}
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setBroadcast(boolean)} Sets or clears the SO_BROADCAST socket
 * option. When this option is set, Datagram (UDP) packets may be sent to a local interface's broadcast address.
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setMulticastNetworkInterface(java.lang.String)} Sets or clears
 * the IP_MULTICAST_LOOP socket option. When this option is set, multicast packets will also be received on the
 * local interface.
 * - {@link io.vertx.core.datagram.DatagramSocketOptions#setMulticastTimeToLive(int)} Sets the IP_MULTICAST_TTL socket
 * option. TTL stands for "Time to Live," but in this context it specifies the number of IP hops that a packet is
 * allowed to go through, specifically for multicast traffic. Each router or gateway that forwards a packet decrements
 * the TTL. If the TTL is decremented to 0 by a router, it will not be forwarded.
 * <p>
 * ==== DatagramSocket Local Address
 * <p>
 * You can find out the local address of the socket (i.e. the address of this side of the UDP Socket) by calling
 * {@link io.vertx.core.datagram.DatagramSocket#localAddress()}. This will only return an `InetSocketAddress` if you
 * bound the {@link io.vertx.core.datagram.DatagramSocket} with `listen(...)` before, otherwise it will return null.
 * <p>
 * ==== Closing a DatagramSocket
 * <p>
 * You can close a socket by invoking the {@link io.vertx.core.datagram.DatagramSocket#close} method. This will close
 * the socket and release all resources
 */
@Document(fileName = "datagrams.adoc")
package io.vertx.core.datagram;

import io.vertx.docgen.Document;

