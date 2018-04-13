/**
 * Copyright (C) 2014-2017 Regents of the University of California.
 * @author: Jeff Thompson <jefft0@remap.ucla.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * A copy of the GNU Lesser General Public License is in the file COPYING.
 */

package net.named_data.jndn;

import java.io.IOException;
import java.nio.ByteBuffer;

import ndn.layer2.Interface;
import ndn.layer2.L2Message;
import ndn.nfd.face.DataFilter;
import ndn.nfd.face.DataFilterTable;
import ndn.nfd.face.DataFilterTableEntry;
import ndn.nfd.face.FRCFaceListener;
//import ndn.nfd.face.InterestFilter;
import ndn.nfd.face.InterestFilterTable;
import ndn.nfd.face.InterestFilterTableEntry;
import ndn.nfd.forwarder.Forwarding;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.encoding.WireFormat;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.transport.TcpTransport;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.transport.UdpTransport;
import net.named_data.jndn.util.Blob;
import net.named_data.jndn.util.Common;

import ndn.layer2.Packet;
import ndn.message.MessageType;
import ndn.message.NDNMessage;
import forwarder.RMICNFD;;

/**
 * The Face class provides the main methods for NDN communication.
 */
public class Face {
	/**
	 * Create a new Face for communication with an NDN hub with the given
	 * Transport object and connectionInfo.
	 * 
	 * @param transport
	 *            A Transport object used for communication.
	 * @param connectionInfo
	 *            A Transport.ConnectionInfo to be used to connect to the
	 *            transport.
	 */
	public Face(Transport transport, Transport.ConnectionInfo connectionInfo) {
		node_ = new Node(transport, connectionInfo);
	}

	/**
	 * Create a new Face for communication with an NDN hub at host:port using
	 * the default TcpTransport.
	 * 
	 * @param host
	 *            The host of the NDN hub.
	 * @param port
	 *            The port of the NDN hub.
	 */
	public Face(String host, int port) {
		node_ = new Node(new TcpTransport(), new TcpTransport.ConnectionInfo(host, port));
	}

	/**
	 * Create a new Face for communication with an NDN hub at host using the
	 * default port 6363 and the default TcpTransport.
	 * 
	 * @param host
	 *            The host of the NDN hub.
	 */
	public Face(String host) {
		node_ = new Node(new TcpTransport(), new TcpTransport.ConnectionInfo(host, 6363));
	}

	/**
	 * Create a new Face for communication with an NDN hub at "localhost" using
	 * the default port 6363 and the default TcpTransport.
	 */
	public Face() {
		node_ = new Node(new TcpTransport(), new TcpTransport.ConnectionInfo("localhost", 6363));
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData, onTimeout or onNetworkNack as described below.
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData, OnTimeout onTimeout, OnNetworkNack onNetworkNack,
			WireFormat wireFormat) throws IOException {
		long pendingInterestId = node_.getNextEntryId();

		// This copies the interest as required by Node.expressInterest.
		node_.expressInterest(pendingInterestId, interest, onData, onTimeout, onNetworkNack, wireFormat, this);

		return pendingInterestId;
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData, onTimeout or onNetworkNack as described below. This uses the
	 * default WireFormat.getDefaultWireFormat().
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData, OnTimeout onTimeout, OnNetworkNack onNetworkNack)
			throws IOException {
		return expressInterest(interest, onData, onTimeout, onNetworkNack, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData or onTimeout as described below.
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData, OnTimeout onTimeout, WireFormat wireFormat)
			throws IOException {
		return expressInterest(interest, onData, onTimeout, null, wireFormat);
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData or onTimeout as described below. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData, OnTimeout onTimeout) throws IOException {
		return expressInterest(interest, onData, onTimeout, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData as described below. Ignore if the interest times out.
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData, WireFormat wireFormat) throws IOException {
		return expressInterest(interest, onData, null, wireFormat);
	}

	/**
	 * Send the Interest through the transport, read the entire response and
	 * call onData as described below. Ignore if the interest times out. This
	 * uses the default WireFormat.getDefaultWireFormat().
	 * 
	 * @param interest
	 *            The Interest to send. This copies the Interest.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Interest interest, OnData onData) throws IOException {
		return expressInterest(interest, onData, null, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData, onTimeout or onNetworkNack as described
	 * below.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData, OnTimeout onTimeout,
			OnNetworkNack onNetworkNack, WireFormat wireFormat) throws IOException {
		long pendingInterestId = node_.getNextEntryId();

		// This copies the name object as required by Node.expressInterest.
		node_.expressInterest(pendingInterestId, getInterestCopy(name, interestTemplate), onData, onTimeout,
				onNetworkNack, wireFormat, this);

		return pendingInterestId;
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData, onTimeout or onNetworkNack as described
	 * below. This uses the default WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData, OnTimeout onTimeout,
			OnNetworkNack onNetworkNack) throws IOException {
		return expressInterest(name, interestTemplate, onData, onTimeout, onNetworkNack,
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData,
	 * onTimeout or onNetworkNack as described below.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData, OnTimeout onTimeout, OnNetworkNack onNetworkNack,
			WireFormat wireFormat) throws IOException {
		return expressInterest(name, null, onData, onTimeout, onNetworkNack, wireFormat);
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData,
	 * onTimeout or onNetworkNack as described below. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param onNetworkNack
	 *            When a network Nack packet for the interest is received and
	 *            onNetworkNack is not null, this calls
	 *            onNetworkNack.onNetworkNack(interest, networkNack) and does
	 *            not call onTimeout. However, if a network Nack is received and
	 *            onNetworkNack is null, do nothing and wait for the interest to
	 *            time out. (Therefore, an application which does not yet
	 *            process a network Nack reason treats a Nack the same as a
	 *            timeout.) NOTE: The library will log any exceptions thrown by
	 *            this callback, but for better error handling the callback
	 *            should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData, OnTimeout onTimeout, OnNetworkNack onNetworkNack)
			throws IOException {
		return expressInterest(name, null, onData, onTimeout, onNetworkNack, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData or onTimeout as described below.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData, OnTimeout onTimeout,
			WireFormat wireFormat) throws IOException {
		return expressInterest(name, interestTemplate, onData, onTimeout, null, wireFormat);
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData
	 * or onTimeout as described below.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData, OnTimeout onTimeout, WireFormat wireFormat)
			throws IOException {
		return expressInterest(name, null, onData, onTimeout, wireFormat);
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData as described below. Ignore if the
	 * interest times out.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData, WireFormat wireFormat)
			throws IOException {
		return expressInterest(name, interestTemplate, onData, null, wireFormat);
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData or onTimeout as described below. This
	 * uses the default WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData, OnTimeout onTimeout)
			throws IOException {
		return expressInterest(name, interestTemplate, onData, onTimeout, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest. If interestTemplate is not null, use its
	 * interest selectors. Send the Interest through the transport, read the
	 * entire response and call onData as described below. Ignore if the
	 * interest times out. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            If not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, Interest interestTemplate, OnData onData) throws IOException {
		return expressInterest(name, interestTemplate, onData, null, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData
	 * or onTimeout as described below. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onTimeout
	 *            If the interest times out according to the interest lifetime,
	 *            this calls onTimeout.onTimeout(interest) where interest is the
	 *            interest given to expressInterest. If onTimeout is null, this
	 *            does not use it. NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData, OnTimeout onTimeout) throws IOException {
		return expressInterest(name, null, onData, onTimeout, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData
	 * as described below. Ignore if the interest times out.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData, WireFormat wireFormat) throws IOException {
		return expressInterest(name, null, onData, null, wireFormat);
	}

	/**
	 * Encode name as an Interest, using a default interest lifetime. Send the
	 * Interest through the transport, read the entire response and call onData
	 * as described below. Ignore if the interest times out. This uses the
	 * default WireFormat.getDefaultWireFormat().
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param onData
	 *            When a matching data packet is received, this calls
	 *            onData.onData(interest, data) where interest is the interest
	 *            given to expressInterest and data is the received Data object.
	 *            NOTE: You must not change the interest object - if you need to
	 *            change it then make a copy. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @return The pending interest ID which can be used with
	 *         removePendingInterest.
	 * @throws IOException
	 *             For I/O error in sending the interest.
	 * @throws Error
	 *             If the encoded interest size exceeds getMaxNdnPacketSize().
	 */
	public long expressInterest(Name name, OnData onData) throws IOException {
		return expressInterest(name, null, onData, null, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Remove the pending interest entry with the pendingInterestId from the
	 * pending interest table. This does not affect another pending interest
	 * with a different pendingInterestId, even if it has the same interest
	 * name. If there is no entry with the pendingInterestId, do nothing.
	 * 
	 * @param pendingInterestId
	 *            The ID returned from expressInterest.
	 */
	public void removePendingInterest(long pendingInterestId) {
		node_.removePendingInterest(pendingInterestId);
	}

	/**
	 * Set the KeyChain and certificate name used to sign command interests
	 * (e.g. for registerPrefix).
	 * 
	 * @param keyChain
	 *            The KeyChain object for signing interests, which must remain
	 *            valid for the life of this Face. You must create the KeyChain
	 *            object and pass it in. You can create a default KeyChain for
	 *            your system with the default KeyChain constructor.
	 * @param certificateName
	 *            The certificate name for signing interests. This makes a copy
	 *            of the Name. You can get the default certificate name with
	 *            keyChain.getDefaultCertificateName() .
	 */
	public void setCommandSigningInfo(KeyChain keyChain, Name certificateName) {
		commandKeyChain_ = keyChain;
		commandCertificateName_ = new Name(certificateName);
	}

	/**
	 * Set the certificate name used to sign command interest (e.g. for
	 * registerPrefix), using the KeyChain that was set with
	 * setCommandSigningInfo.
	 * 
	 * @param certificateName
	 *            The certificate name for signing interest. This makes a copy
	 *            of the Name.
	 */
	public void setCommandCertificateName(Name certificateName) {
		commandCertificateName_ = new Name(certificateName);
	}

	/**
	 * Append a timestamp component and a random value component to interest's
	 * name. Then use the keyChain and certificateName from
	 * setCommandSigningInfo to sign the interest. If the interest lifetime is
	 * not set, this sets it.
	 * 
	 * @param interest
	 *            The interest whose name is appended with components.
	 * @param wireFormat
	 *            A WireFormat object used to encode the SignatureInfo and to
	 *            encode the interest name for signing.
	 * @throws SecurityException
	 *             If cannot find the private key for the certificateName.
	 * @note This method is an experimental feature. See the API docs for more
	 *       detail at
	 *       http://named-data.net/doc/ndn-ccl-api/face.html#face-makecommandinterest-method
	 *       .
	 */
	public void makeCommandInterest(Interest interest, WireFormat wireFormat) throws SecurityException {
		node_.makeCommandInterest(interest, commandKeyChain_, commandCertificateName_, wireFormat);
	}

	/**
	 * Append a timestamp component and a random value component to interest's
	 * name. Then use the keyChain and certificateName from
	 * setCommandSigningInfo to sign the interest. If the interest lifetime is
	 * not set, this sets it. Use the default WireFormat to encode the
	 * SignatureInfo and to encode the interest name for signing.
	 * 
	 * @param interest
	 *            The interest whose name is appended with components.
	 * @throws SecurityException
	 *             If cannot find the private key for the certificateName.
	 * @note This method is an experimental feature. See the API docs for more
	 *       detail at
	 *       http://named-data.net/doc/ndn-ccl-api/face.html#face-makecommandinterest-method
	 *       .
	 */
	public void makeCommandInterest(Interest interest) throws SecurityException {
		makeCommandInterest(interest, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. To register a prefix with NFD, you must
	 * first call setCommandSigningInfo.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param onRegisterSuccess
	 *            This calls onRegisterSuccess.onRegisterSuccess(prefix,
	 *            registeredPrefixId) when this receives a success message from
	 *            the forwarder. If onRegisterSuccess is null, this does not use
	 *            it. (The onRegisterSuccess parameter comes after
	 *            onRegisterFailed because it can be null or omitted, unlike
	 *            onRegisterFailed.) NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param flags
	 *            The flags for finer control of which interests are forwarded
	 *            to the application.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			OnRegisterSuccess onRegisterSuccess, ForwardingFlags flags, WireFormat wireFormat)
			throws IOException, SecurityException {
		// Get the registeredPrefixId now so we can return it to the caller.
		long registeredPrefixId = node_.getNextEntryId();

		node_.registerPrefix(registeredPrefixId, prefix, onInterest, onRegisterFailed, onRegisterSuccess, flags,
				wireFormat, commandKeyChain_, commandCertificateName_, this);

		return registeredPrefixId;
	}

	/**
	 * @deprecated Use registerPrefix(prefix, onInterest, onRegisterFailed,
	 *             onRegisterSuccess, flags, wireFormat) where the
	 *             onRegisterSuccess parameter comes after onRegisterFailed.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterSuccess onRegisterSuccess,
			OnRegisterFailed onRegisterFailed, ForwardingFlags flags, WireFormat wireFormat)
			throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, flags, wireFormat);
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param onRegisterSuccess
	 *            This calls onRegisterSuccess.onRegisterSuccess(prefix,
	 *            registeredPrefixId) when this receives a success message from
	 *            the forwarder. If onRegisterSuccess is null, this does not use
	 *            it. (The onRegisterSuccess parameter comes after
	 *            onRegisterFailed because it can be null or omitted, unlike
	 *            onRegisterFailed.) NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param flags
	 *            The flags for finer control of which interests are forwarded
	 *            to the application.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			OnRegisterSuccess onRegisterSuccess, ForwardingFlags flags) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, flags,
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * @deprecated Use registerPrefix(prefix, onInterest, onRegisterFailed,
	 *             onRegisterSuccess, flags) where the onRegisterSuccess
	 *             parameter comes after onRegisterFailed.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterSuccess onRegisterSuccess,
			OnRegisterFailed onRegisterFailed, ForwardingFlags flags) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, flags,
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. Use default ForwardingFlags.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param onRegisterSuccess
	 *            This calls onRegisterSuccess.onRegisterSuccess(prefix,
	 *            registeredPrefixId) when this receives a success message from
	 *            the forwarder. If onRegisterSuccess is null, this does not use
	 *            it. (The onRegisterSuccess parameter comes after
	 *            onRegisterFailed because it can be null or omitted, unlike
	 *            onRegisterFailed.) NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			OnRegisterSuccess onRegisterSuccess, WireFormat wireFormat) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, new ForwardingFlags(),
				wireFormat);
	}

	/**
	 * @deprecated Use registerPrefix(prefix, onInterest, onRegisterFailed,
	 *             onRegisterSuccess, wireFormat) where the onRegisterSuccess
	 *             parameter comes after onRegisterFailed.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterSuccess onRegisterSuccess,
			OnRegisterFailed onRegisterFailed, WireFormat wireFormat) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, new ForwardingFlags(),
				wireFormat);
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. This uses the default
	 * WireFormat.getDefaultWireFormat(). Use default ForwardingFlags.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param onRegisterSuccess
	 *            This calls onRegisterSuccess.onRegisterSuccess(prefix,
	 *            registeredPrefixId) when this receives a success message from
	 *            the forwarder. If onRegisterSuccess is null, this does not use
	 *            it. (The onRegisterSuccess parameter comes after
	 *            onRegisterFailed because it can be null or omitted, unlike
	 *            onRegisterFailed.) NOTE: The library will log any exceptions
	 *            thrown by this callback, but for better error handling the
	 *            callback should catch and properly handle any exceptions.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			OnRegisterSuccess onRegisterSuccess) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, new ForwardingFlags(),
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * @deprecated Use registerPrefix(prefix, onInterest, onRegisterFailed,
	 *             onRegisterSuccess) where the onRegisterSuccess parameter
	 *             comes after onRegisterFailed.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterSuccess onRegisterSuccess,
			OnRegisterFailed onRegisterFailed) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, onRegisterSuccess, new ForwardingFlags(),
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. To register a prefix with NFD, you must
	 * first call setCommandSigningInfo.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param flags
	 *            The flags for finer control of which interests are forwarded
	 *            to the application.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			ForwardingFlags flags, WireFormat wireFormat) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, null, flags, wireFormat);
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. To register a prefix with NFD, you must
	 * first call setCommandSigningInfo. This uses the default
	 * WireFormat.getDefaultWireFormat().
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param flags
	 *            The flags for finer control of which interests are forwarded
	 *            to the application.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			ForwardingFlags flags) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, null, flags, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. To register a prefix with NFD, you must
	 * first call setCommandSigningInfo. Use default ForwardingFlags.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @param wireFormat
	 *            A WireFormat object used to encode the message.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed,
			WireFormat wireFormat) throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, null, new ForwardingFlags(), wireFormat);
	}

	/**
	 * Register prefix with the connected NDN hub and call onInterest when a
	 * matching interest is received. To register a prefix with NFD, you must
	 * first call setCommandSigningInfo. This uses the default
	 * WireFormat.getDefaultWireFormat(). Use default ForwardingFlags.
	 * 
	 * @param prefix
	 *            A Name for the prefix to register. This copies the Name.
	 * @param onInterest
	 *            If not null, this creates an interest filter from prefix so
	 *            that when an Interest is received which matches the filter,
	 *            this calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). The onInterest callback should
	 *            supply the Data with face.putData(). NOTE: You must not change
	 *            the prefix or filter objects - if you need to change them then
	 *            make a copy. If onInterest is null, it is ignored and you must
	 *            call setInterestFilter. NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @param onRegisterFailed
	 *            If register prefix fails for any reason, this calls
	 *            onRegisterFailed.onRegisterFailed(prefix). NOTE: The library
	 *            will log any exceptions thrown by this callback, but for
	 *            better error handling the callback should catch and properly
	 *            handle any exceptions.
	 * @return The registered prefix ID which can be used with
	 *         removeRegisteredPrefix.
	 * @throws IOException
	 *             For I/O error in sending the registration request.
	 * @throws SecurityException
	 *             If signing a command interest for NFD and cannot find the
	 *             private key for the certificateName.
	 */
	public long registerPrefix(Name prefix, OnInterestCallback onInterest, OnRegisterFailed onRegisterFailed)
			throws IOException, SecurityException {
		return registerPrefix(prefix, onInterest, onRegisterFailed, null, new ForwardingFlags(),
				WireFormat.getDefaultWireFormat());
	}

	/**
	 * Remove the registered prefix entry with the registeredPrefixId from the
	 * registered prefix table. This does not affect another registered prefix
	 * with a different registeredPrefixId, even if it has the same prefix name.
	 * If an interest filter was automatically created by registerPrefix, also
	 * remove it. If there is no entry with the registeredPrefixId, do nothing.
	 * 
	 * @param registeredPrefixId
	 *            The ID returned from registerPrefix.
	 */
	public void removeRegisteredPrefix(long registeredPrefixId) {
		node_.removeRegisteredPrefix(registeredPrefixId);
	}

	/**
	 * Add an entry to the local interest filter table to call the onInterest
	 * callback for a matching incoming Interest. This method only modifies the
	 * library's local callback table and does not register the prefix with the
	 * forwarder. It will always succeed. To register a prefix with the
	 * forwarder, use registerPrefix.
	 * 
	 * @param filter
	 *            The InterestFilter with a prefix and optional regex filter
	 *            used to match the name of an incoming Interest. This makes a
	 *            copy of filter.
	 * @param onInterest
	 *            When an Interest is received which matches the filter, this
	 *            calls onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @return The interest filter ID which can be used with
	 *         unsetInterestFilter.
	 */
	public long setInterestFilter(InterestFilter filter, OnInterestCallback onInterest) {
		long interestFilterId = node_.getNextEntryId();

		node_.setInterestFilter(interestFilterId, filter, onInterest, this);

		return interestFilterId;
	}

	/**
	 * Add an entry to the local interest filter table to call the onInterest
	 * callback for a matching incoming Interest. This method only modifies the
	 * library's local callback table and does not register the prefix with the
	 * forwarder. It will always succeed. To register a prefix with the
	 * forwarder, use registerPrefix.
	 * 
	 * @param prefix
	 *            The Name prefix used to match the name of an incoming
	 *            Interest.
	 * @param onInterest
	 *            This creates an interest filter from prefix so that when an
	 *            Interest is received which matches the filter, this calls
	 *            onInterest.onInterest(prefix, interest, face,
	 *            interestFilterId, filter). NOTE: The library will log any
	 *            exceptions thrown by this callback, but for better error
	 *            handling the callback should catch and properly handle any
	 *            exceptions.
	 * @return The interest filter ID which can be used with
	 *         unsetInterestFilter.
	 */
	public long setInterestFilter(Name prefix, OnInterestCallback onInterest) {
		return setInterestFilter(new InterestFilter(prefix), onInterest);
	}

	/**
	 * Remove the interest filter entry which has the interestFilterId from the
	 * interest filter table. This does not affect another interest filter with
	 * a different interestFilterId, even if it has the same prefix name. If
	 * there is no entry with the interestFilterId, do nothing.
	 * 
	 * @param interestFilterId
	 *            The ID returned from setInterestFilter.
	 */
	public void unsetInterestFilter(long interestFilterId) {
		node_.unsetInterestFilter(interestFilterId);
	}

	/**
	 * The OnInterestCallback calls this to put a Data packet which satisfies an
	 * Interest.
	 * 
	 * @param data
	 *            The Data packet which satisfies the interest.
	 * @param wireFormat
	 *            A WireFormat object used to encode the Data packet.
	 * @throws Error
	 *             If the encoded Data packet size exceeds
	 *             getMaxNdnPacketSize().
	 */
	public void putData(Data data, WireFormat wireFormat) throws IOException {
		node_.putData(data, wireFormat);
	}

	/**
	 * The OnInterestCallback calls this to put a Data packet which satisfies an
	 * Interest. This uses the default WireFormat.getDefaultWireFormat() to
	 * encode data.
	 * 
	 * @param data
	 *            The Data packet which satisfies the interest.
	 * @throws Error
	 *             If the encoded Data packet size exceeds
	 *             getMaxNdnPacketSize().
	 */
	public void putData(Data data) throws IOException {
		putData(data, WireFormat.getDefaultWireFormat());
	}

	/**
	 * Send the encoded packet out through the face.
	 * 
	 * @param encoding
	 *            The blob with the the encoded packet to send.
	 * @throws Error
	 *             If the encoded packet size exceeds getMaxNdnPacketSize().
	 */
	public void send(Blob encoding) throws IOException {
		send(encoding.buf());
	}

	/**
	 * Send the encoded packet out through the face.
	 * 
	 * @param encoding
	 *            The array of bytes for the encoded packet to send. This reads
	 *            from position() to limit(), but does not change the position.
	 * @throws Error
	 *             If the encoded packet size exceeds getMaxNdnPacketSize().
	 */
	public void send(ByteBuffer encoding) throws IOException {
		node_.send(encoding);
	}

	/**
	 * Process any packets to receive and call callbacks such as onData,
	 * onInterest or onTimeout. This returns immediately if there is no data to
	 * receive. This blocks while calling the callbacks. You should repeatedly
	 * call this from an event loop, with calls to sleep as needed so that the
	 * loop doesn’t use 100% of the CPU. Since processEvents modifies the
	 * pending interest table, your application should make sure that it calls
	 * processEvents in the same thread as expressInterest (which also modifies
	 * the pending interest table). This may throw an exception for reading data
	 * or in the callback for processing the data. If you call this from an main
	 * event loop, you may want to catch and log/disregard all exceptions.
	 */
	public void processEvents() throws IOException, EncodingException {
		// Just call Node's processEvents.
		node_.processEvents();
	}

	/**
	 * Check if the face is local based on the current connection through the
	 * Transport; some Transport may cause network IO (e.g. an IP host name
	 * lookup).
	 * 
	 * @return True if the face is local, false if not.
	 * @throws IOException
	 * @note This is an experimental feature. This API may change in the future.
	 */
	public boolean isLocal() throws IOException {
		return node_.isLocal();
	}

	/**
	 * Shut down and disconnect this Face.
	 */
	public void shutdown() {
		node_.shutdown();
	}

	/**
	 * Get the practical limit of the size of a network-layer packet. If a
	 * packet is larger than this, the library or application MAY drop it.
	 * 
	 * @return The maximum NDN packet size.
	 */
	public static int getMaxNdnPacketSize() {
		return Common.MAX_NDN_PACKET_SIZE;
	}

	/**
	 * Call callback.run() after the given delay. Even though this is public, it
	 * is not part of the public API of Face. This default implementation just
	 * calls Node.callLater, but a subclass can override.
	 * 
	 * @param delayMilliseconds
	 *            The delay in milliseconds.
	 * @param callback
	 *            This calls callback.run() after the delay.
	 */
	public void callLater(double delayMilliseconds, Runnable callback) {
		node_.callLater(delayMilliseconds, callback);
	}

	/**
	 * Do the work of expressInterest to make an Interest based on name and
	 * interestTemplate.
	 * 
	 * @param name
	 *            A Name for the interest. This copies the Name.
	 * @param interestTemplate
	 *            if not null, copy interest selectors from the template. This
	 *            does not keep a pointer to the Interest object.
	 * @return The Interest, suitable for Node.expressInterest.
	 */
	static protected Interest getInterestCopy(Name name, Interest interestTemplate) {
		if (interestTemplate != null) {
			// Copy the interestTemplate.
			Interest interestCopy = new Interest(interestTemplate);
			interestCopy.setName(name);
			return interestCopy;
		} else {
			Interest interestCopy = new Interest(name);
			interestCopy.setInterestLifetimeMilliseconds(4000.0);
			return interestCopy;
		}
	}

	// added
	public Face(int faceID, String remoteURI, Forwarding forwarding) {
		this.faceID = faceID;
		this.iFace = forwarding.getNFD().getNode().getInterface();
		this.remoteURI = remoteURI;
		this.isActive = false;
		this.forwarding = forwarding;
		this.ift = new InterestFilterTable();
		this.dft = new DataFilterTable();
		node_ = new Node(new TcpTransport(), new TcpTransport.ConnectionInfo("localhost", 6363));
	}

	public void send(NDNMessage message) {
		// Packet packet = new Packet(iFace.getInterfaceURI(), remoteURI,
		// message);

		// Log
		// forwarding.getNFD().log("Face", "Send " +
		// MessageType.toStr(message.getType()),
		// "source: " + packet.getSourceURI() + " dest: " +
		// packet.getDestinationURI()
		// + " name: " + message.getName());

		// iFace.send(packet);
		if (iFace.isConnected(remoteURI) || !(iFace.isRMICN())) {

			try {
				if (message.getType() == MessageType.INTEREST) {

					SendInterestListener listener = new SendInterestListener(this);
					this.expressInterest(new Interest(message.getName()), listener, listener);
					this.processEvents();

					System.out.println("Sent\t\tInterest\t" + message.getName());

				} else if (message.getType() == MessageType.DATA) {
					FRCFaceListener listener = new FRCFaceListener(this, new Name("/RMICN"));
					Name prefix = new Name(message.getName());

					// Make and sign a Data packet.
					Data data = new Data(prefix);
					String content = message.getContent();
					if (content != null)
						data.setContent(new Blob(content));

					try {
						listener.keyChain_.sign(data, listener.certificateName_);
					} catch (SecurityException exception) {
						// Don't expect this to happen.
						throw new Error("SecurityException in sign: " + exception.getMessage());
					}

					System.out.println("Sent\t\tData\t\t" + prefix);
					try {
						this.putData(data);
					} catch (IOException ex) {
						System.out.println("Echo: IOException in sending data " + ex.getMessage());
					}

				}

			} catch (Exception e) {
				System.out.println("exception: " + e.getMessage());
			}

		}
	}

	public void receive(Packet packet) {
		NDNMessage message = packet.getNDNMessage();

		// Log
		forwarding.getNFD().log("Face", "Receive " + MessageType.toStr(message.getType()), "source: "
				+ packet.getSourceURI() + " dest: " + packet.getDestinationURI() + " name: " + message.getName());

		if (message.getType() == MessageType.INTEREST) {
			/*
			 * InterestFilterTableEntry iftEntry = (InterestFilterTableEntry)
			 * ift.findByLM(message.getName()); if (iftEntry != null)
			 * iftEntry.getInterestFilter().onInterest(this, new
			 * Interest(message)); else
			 */
			forwarding.onReceiveInterest(this, new Interest(message));
		} else if (message.getType() == MessageType.DATA) {
			/*
			 * DataFilterTableEntry dftEntry = (DataFilterTableEntry)
			 * dft.findByLM(message.getName()); if (dftEntry != null)
			 * dftEntry.getDataFilter().onData(this, new Data(message)); else
			 */
			forwarding.onReceiveData(this, new Data(message));
		}
	}

	public int getFaceID() {
		return faceID;
	}

	public void setFaceID(int faceID) {
		this.faceID = faceID;
	}

	public Interface getInterface() {
		return iFace;
	}

	public void setInterface(Interface iFace) {
		this.iFace = iFace;
	}

	public Forwarding getForwarding() {
		return forwarding;
	}

	public void setForwarding(Forwarding nfd) {
		this.forwarding = nfd;
	}

	public String getRemoteURI() {
		return remoteURI;
	}

	public void setRemoteURI(String remoteURI) {
		this.remoteURI = remoteURI;
	}

	public InterestFilterTable getInterestFilterTable() {
		return ift;
	}

	public void setInterestFilterTable(InterestFilterTable ift) {
		this.ift = ift;
	}

	public DataFilterTable getDataFilterTable() {
		return dft;
	}

	public void setDataFilterTable(DataFilterTable dft) {
		this.dft = dft;
	}

	public void addInterestFilter(String name, InterestFilter interestFilter) {
		InterestFilterTableEntry iftEntry = new InterestFilterTableEntry(name, interestFilter);
		ift.insert(iftEntry);
	}

	public void addDataFilter(String name, DataFilter dataFilter) {
		DataFilterTableEntry dftEntry = new DataFilterTableEntry(name, dataFilter);
		dft.insert(dftEntry);
	}

	public void deleteInterestFilter(String name) {
		ift.delete(name);
	}

	public void deleteDataFilter(String name) {
		dft.delete(name);
	}

	/*
	 * public long registerPrefix (Name prefix) throws IOException,
	 * SecurityException { return registerPrefix (prefix, this.RegisterListener,
	 * this.RegisterListener, null, new ForwardingFlags(),
	 * WireFormat.getDefaultWireFormat()); }
	 */

	private static ByteBuffer toBuffer(int[] array) {
		ByteBuffer result = ByteBuffer.allocate(array.length);
		for (int i = 0; i < array.length; ++i)
			result.put((byte) (array[i] & 0xff));

		result.flip();
		return result;
	}

	protected final Node node_;
	protected KeyChain commandKeyChain_ = null;
	protected Name commandCertificateName_ = new Name();

	// added

	protected int faceID;
	protected Interface iFace;
	protected String remoteURI;
	protected boolean isActive;
	protected Forwarding forwarding;
	protected InterestFilterTable ift;
	protected DataFilterTable dft;

	protected int sleepTime;

	public class SendInterestListener implements OnData, OnTimeout {

		public SendInterestListener(Face f) {
			face = f;
		}

		public void onData(Interest interest, Data data) {
			++callbackCount_;

			/*
			 * ByteBuffer content = data.getContent().buf(); for (int i =
			 * content.position(); i < content.limit(); ++i)
			 * System.out.print((char)content.get(i)); System.out.println("");
			 */

			System.out.println("Received\tData\t\t" + data.getName());

			if (data.getName().toUri().contains("RetrieveInfo")) {
				((RMICNFD) face.getForwarding().getNFD()).setStatusRetrieveInfo(RMICNFD.ReceivedData);
			}

			NDNMessage message = new NDNMessage(data.getName().toUri(), MessageType.DATA, data.getContent().toString());
			Packet packet = new Packet(remoteURI, iFace.getInterfaceURI(), message);
			receive(packet);

		}

		public int callbackCount_ = 0;

		public void onTimeout(Interest interest) {
			++callbackCount_;
			System.out.println("Time out for Interest\t\t" + interest.getName().toUri());
			System.out.println("Retransmit");

			this.face.send(new NDNMessage(interest.getNDNName(), interest.getType()));

		}

		private Face face;
	}

	public class RegisterListener implements OnInterestCallback, OnRegisterFailed {

		public RegisterListener(Face face, Name KeyPrefix) {
			try {
				MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
				MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
				KeyChain keyChain = new KeyChain(new IdentityManager(identityStorage, privateKeyStorage),
						new SelfVerifyPolicyManager(identityStorage));
				keyChain.setFace(face);

				// Initialize the storage.
				Name keyName = new Name(KeyPrefix.append("DSK-123"));
				Name certificateName = keyName.getSubName(0, keyName.size() - 1).append("KEY").append(keyName.get(-1))
						.append("ID-CERT").append("0");
				identityStorage.addKey(keyName, KeyType.RSA, new Blob(DEFAULT_RSA_PUBLIC_KEY_DER, false));
				privateKeyStorage.setKeyPairForKeyName(keyName, KeyType.RSA, DEFAULT_RSA_PUBLIC_KEY_DER,
						DEFAULT_RSA_PRIVATE_KEY_DER);

				face.setCommandSigningInfo(keyChain, certificateName);

				keyChain_ = keyChain;
				certificateName_ = certificateName;
			}

			catch (Exception e) {
				System.out.println("exception: " + e.getMessage());
			}

		}

		public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId,
				InterestFilter filter) {
			++responseCount_;

			NDNMessage message = new NDNMessage(interest.getName().toUri(), MessageType.INTEREST);
			Packet packet = new Packet(remoteURI, iFace.getInterfaceURI(), message);
			receive(packet);
			/*
			 * 
			 * // Make and sign a Data packet. Data data = new
			 * Data(interest.getName()); String content = "Echo " +
			 * interest.getName().toUri(); data.setContent(new Blob(content));
			 * 
			 * 
			 * try { keyChain_.sign(data, certificateName_); } catch
			 * (SecurityException exception) { // Don't expect this to happen.
			 * throw new Error ("SecurityException in sign: " +
			 * exception.getMessage()); }
			 * 
			 * 
			 * System.out.println("Sent content " + content); try {
			 * face.putData(data); } catch (IOException ex) {
			 * System.out.println("Echo: IOException in sending data " +
			 * ex.getMessage()); }
			 */

		}

		public void onRegisterFailed(Name prefix) {
			++responseCount_;
			System.out.println("Register failed for prefix " + prefix.toUri());
		}

		KeyChain keyChain_;
		Name certificateName_;

		int responseCount_ = 0;

	}

	private static final ByteBuffer DEFAULT_RSA_PUBLIC_KEY_DER = toBuffer(new int[] { 0x30, 0x82, 0x01, 0x22, 0x30,
			0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f,
			0x00, 0x30, 0x82, 0x01, 0x0a, 0x02, 0x82, 0x01, 0x01, 0x00, 0xb8, 0x09, 0xa7, 0x59, 0x82, 0x84, 0xec, 0x4f,
			0x06, 0xfa, 0x1c, 0xb2, 0xe1, 0x38, 0x93, 0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88, 0x1a, 0xf8, 0x25, 0x11, 0xe4,
			0xfa, 0x1d, 0x61, 0x24, 0x5b, 0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5, 0x8d, 0x54, 0xbd, 0xfb, 0x23,
			0xfd, 0xe8, 0x8e, 0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17, 0xb6, 0x05, 0xae, 0xce, 0x43,
			0xbe, 0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc, 0xc9, 0x7b, 0x62, 0x3c, 0xf5, 0xe1,
			0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb, 0x59, 0xc0, 0x94, 0x0b, 0x2c, 0xd9, 0x7d, 0xbc,
			0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0, 0x12, 0x1d, 0x42, 0x07, 0xb6, 0x5d, 0xdb, 0xe1, 0xf6, 0xb1,
			0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c, 0xa3, 0x11, 0x9b, 0xf9, 0xeb, 0xbe, 0xb3, 0x95, 0xca, 0xa5, 0x87, 0x3f,
			0x31, 0x18, 0x1a, 0xc9, 0x99, 0x01, 0xec, 0xaa, 0x90, 0xfd, 0x8a, 0x36, 0x35, 0x5e, 0x12, 0x81, 0xbe, 0x84,
			0x88, 0xa1, 0x0d, 0x19, 0x2a, 0x4a, 0x66, 0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d, 0x3d, 0xb8, 0xd4, 0xab, 0x34,
			0x90, 0x06, 0x3e, 0x1a, 0x61, 0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d, 0x56, 0xfc, 0x83, 0xb7, 0x60,
			0xc1, 0x5e, 0x9d, 0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7, 0x4a, 0x5e, 0x18, 0xbf, 0xc0,
			0x58, 0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2, 0xdb, 0xfd, 0x72, 0x2f, 0x3c, 0x47,
			0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25, 0x20, 0x92, 0x15, 0x4b, 0x04, 0xfe, 0x15, 0x19,
			0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21, 0x91, 0x54, 0x60, 0x8b, 0x92, 0x41, 0x02, 0x03, 0x01, 0x00,
			0x01 });

	// Java uses an unencrypted PKCS #8 PrivateKeyInfo, not a PKCS #1
	// RSAPrivateKey.
	private static final ByteBuffer DEFAULT_RSA_PRIVATE_KEY_DER = toBuffer(new int[] { 0x30, 0x82, 0x04, 0xbf, 0x02,
			0x01, 0x00, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x04,
			0x82, 0x04, 0xa9, 0x30, 0x82, 0x04, 0xa5, 0x02, 0x01, 0x00, 0x02, 0x82, 0x01, 0x01, 0x00, 0xb8, 0x09, 0xa7,
			0x59, 0x82, 0x84, 0xec, 0x4f, 0x06, 0xfa, 0x1c, 0xb2, 0xe1, 0x38, 0x93, 0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88,
			0x1a, 0xf8, 0x25, 0x11, 0xe4, 0xfa, 0x1d, 0x61, 0x24, 0x5b, 0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5,
			0x8d, 0x54, 0xbd, 0xfb, 0x23, 0xfd, 0xe8, 0x8e, 0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17,
			0xb6, 0x05, 0xae, 0xce, 0x43, 0xbe, 0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc, 0xc9,
			0x7b, 0x62, 0x3c, 0xf5, 0xe1, 0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb, 0x59, 0xc0, 0x94,
			0x0b, 0x2c, 0xd9, 0x7d, 0xbc, 0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0, 0x12, 0x1d, 0x42, 0x07, 0xb6,
			0x5d, 0xdb, 0xe1, 0xf6, 0xb1, 0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c, 0xa3, 0x11, 0x9b, 0xf9, 0xeb, 0xbe, 0xb3,
			0x95, 0xca, 0xa5, 0x87, 0x3f, 0x31, 0x18, 0x1a, 0xc9, 0x99, 0x01, 0xec, 0xaa, 0x90, 0xfd, 0x8a, 0x36, 0x35,
			0x5e, 0x12, 0x81, 0xbe, 0x84, 0x88, 0xa1, 0x0d, 0x19, 0x2a, 0x4a, 0x66, 0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d,
			0x3d, 0xb8, 0xd4, 0xab, 0x34, 0x90, 0x06, 0x3e, 0x1a, 0x61, 0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d,
			0x56, 0xfc, 0x83, 0xb7, 0x60, 0xc1, 0x5e, 0x9d, 0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7,
			0x4a, 0x5e, 0x18, 0xbf, 0xc0, 0x58, 0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2, 0xdb,
			0xfd, 0x72, 0x2f, 0x3c, 0x47, 0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25, 0x20, 0x92, 0x15,
			0x4b, 0x04, 0xfe, 0x15, 0x19, 0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21, 0x91, 0x54, 0x60, 0x8b, 0x92,
			0x41, 0x02, 0x03, 0x01, 0x00, 0x01, 0x02, 0x82, 0x01, 0x01, 0x00, 0x8a, 0x05, 0xfb, 0x73, 0x7f, 0x16, 0xaf,
			0x9f, 0xa9, 0x4c, 0xe5, 0x3f, 0x26, 0xf8, 0x66, 0x4d, 0xd2, 0xfc, 0xd1, 0x06, 0xc0, 0x60, 0xf1, 0x9f, 0xe3,
			0xa6, 0xc6, 0x0a, 0x48, 0xb3, 0x9a, 0xca, 0x21, 0xcd, 0x29, 0x80, 0x88, 0x3d, 0xa4, 0x85, 0xa5, 0x7b, 0x82,
			0x21, 0x81, 0x28, 0xeb, 0xf2, 0x43, 0x24, 0xb0, 0x76, 0xc5, 0x52, 0xef, 0xc2, 0xea, 0x4b, 0x82, 0x41, 0x92,
			0xc2, 0x6d, 0xa6, 0xae, 0xf0, 0xb2, 0x26, 0x48, 0xa1, 0x23, 0x7f, 0x02, 0xcf, 0xa8, 0x90, 0x17, 0xa2, 0x3e,
			0x8a, 0x26, 0xbd, 0x6d, 0x8a, 0xee, 0xa6, 0x0c, 0x31, 0xce, 0xc2, 0xbb, 0x92, 0x59, 0xb5, 0x73, 0xe2, 0x7d,
			0x91, 0x75, 0xe2, 0xbd, 0x8c, 0x63, 0xe2, 0x1c, 0x8b, 0xc2, 0x6a, 0x1c, 0xfe, 0x69, 0xc0, 0x44, 0xcb, 0x58,
			0x57, 0xb7, 0x13, 0x42, 0xf0, 0xdb, 0x50, 0x4c, 0xe0, 0x45, 0x09, 0x8f, 0xca, 0x45, 0x8a, 0x06, 0xfe, 0x98,
			0xd1, 0x22, 0xf5, 0x5a, 0x9a, 0xdf, 0x89, 0x17, 0xca, 0x20, 0xcc, 0x12, 0xa9, 0x09, 0x3d, 0xd5, 0xf7, 0xe3,
			0xeb, 0x08, 0x4a, 0xc4, 0x12, 0xc0, 0xb9, 0x47, 0x6c, 0x79, 0x50, 0x66, 0xa3, 0xf8, 0xaf, 0x2c, 0xfa, 0xb4,
			0x6b, 0xec, 0x03, 0xad, 0xcb, 0xda, 0x24, 0x0c, 0x52, 0x07, 0x87, 0x88, 0xc0, 0x21, 0xf3, 0x02, 0xe8, 0x24,
			0x44, 0x0f, 0xcd, 0xa0, 0xad, 0x2f, 0x1b, 0x79, 0xab, 0x6b, 0x49, 0x4a, 0xe6, 0x3b, 0xd0, 0xad, 0xc3, 0x48,
			0xb9, 0xf7, 0xf1, 0x34, 0x09, 0xeb, 0x7a, 0xc0, 0xd5, 0x0d, 0x39, 0xd8, 0x45, 0xce, 0x36, 0x7a, 0xd8, 0xde,
			0x3c, 0xb0, 0x21, 0x96, 0x97, 0x8a, 0xff, 0x8b, 0x23, 0x60, 0x4f, 0xf0, 0x3d, 0xd7, 0x8f, 0xf3, 0x2c, 0xcb,
			0x1d, 0x48, 0x3f, 0x86, 0xc4, 0xa9, 0x00, 0xf2, 0x23, 0x2d, 0x72, 0x4d, 0x66, 0xa5, 0x01, 0x02, 0x81, 0x81,
			0x00, 0xdc, 0x4f, 0x99, 0x44, 0x0d, 0x7f, 0x59, 0x46, 0x1e, 0x8f, 0xe7, 0x2d, 0x8d, 0xdd, 0x54, 0xc0, 0xf7,
			0xfa, 0x46, 0x0d, 0x9d, 0x35, 0x03, 0xf1, 0x7c, 0x12, 0xf3, 0x5a, 0x9d, 0x83, 0xcf, 0xdd, 0x37, 0x21, 0x7c,
			0xb7, 0xee, 0xc3, 0x39, 0xd2, 0x75, 0x8f, 0xb2, 0x2d, 0x6f, 0xec, 0xc6, 0x03, 0x55, 0xd7, 0x00, 0x67, 0xd3,
			0x9b, 0xa2, 0x68, 0x50, 0x6f, 0x9e, 0x28, 0xa4, 0x76, 0x39, 0x2b, 0xb2, 0x65, 0xcc, 0x72, 0x82, 0x93, 0xa0,
			0xcf, 0x10, 0x05, 0x6a, 0x75, 0xca, 0x85, 0x35, 0x99, 0xb0, 0xa6, 0xc6, 0xef, 0x4c, 0x4d, 0x99, 0x7d, 0x2c,
			0x38, 0x01, 0x21, 0xb5, 0x31, 0xac, 0x80, 0x54, 0xc4, 0x18, 0x4b, 0xfd, 0xef, 0xb3, 0x30, 0x22, 0x51, 0x5a,
			0xea, 0x7d, 0x9b, 0xb2, 0x9d, 0xcb, 0xba, 0x3f, 0xc0, 0x1a, 0x6b, 0xcd, 0xb0, 0xe6, 0x2f, 0x04, 0x33, 0xd7,
			0x3a, 0x49, 0x71, 0x02, 0x81, 0x81, 0x00, 0xd5, 0xd9, 0xc9, 0x70, 0x1a, 0x13, 0xb3, 0x39, 0x24, 0x02, 0xee,
			0xb0, 0xbb, 0x84, 0x17, 0x12, 0xc6, 0xbd, 0x65, 0x73, 0xe9, 0x34, 0x5d, 0x43, 0xff, 0xdc, 0xf8, 0x55, 0xaf,
			0x2a, 0xb9, 0xe1, 0xfa, 0x71, 0x65, 0x4e, 0x50, 0x0f, 0xa4, 0x3b, 0xe5, 0x68, 0xf2, 0x49, 0x71, 0xaf, 0x15,
			0x88, 0xd7, 0xaf, 0xc4, 0x9d, 0x94, 0x84, 0x6b, 0x5b, 0x10, 0xd5, 0xc0, 0xaa, 0x0c, 0x13, 0x62, 0x99, 0xc0,
			0x8b, 0xfc, 0x90, 0x0f, 0x87, 0x40, 0x4d, 0x58, 0x88, 0xbd, 0xe2, 0xba, 0x3e, 0x7e, 0x2d, 0xd7, 0x69, 0xa9,
			0x3c, 0x09, 0x64, 0x31, 0xb6, 0xcc, 0x4d, 0x1f, 0x23, 0xb6, 0x9e, 0x65, 0xd6, 0x81, 0xdc, 0x85, 0xcc, 0x1e,
			0xf1, 0x0b, 0x84, 0x38, 0xab, 0x93, 0x5f, 0x9f, 0x92, 0x4e, 0x93, 0x46, 0x95, 0x6b, 0x3e, 0xb6, 0xc3, 0x1b,
			0xd7, 0x69, 0xa1, 0x0a, 0x97, 0x37, 0x78, 0xed, 0xd1, 0x02, 0x81, 0x80, 0x33, 0x18, 0xc3, 0x13, 0x65, 0x8e,
			0x03, 0xc6, 0x9f, 0x90, 0x00, 0xae, 0x30, 0x19, 0x05, 0x6f, 0x3c, 0x14, 0x6f, 0xea, 0xf8, 0x6b, 0x33, 0x5e,
			0xee, 0xc7, 0xf6, 0x69, 0x2d, 0xdf, 0x44, 0x76, 0xaa, 0x32, 0xba, 0x1a, 0x6e, 0xe6, 0x18, 0xa3, 0x17, 0x61,
			0x1c, 0x92, 0x2d, 0x43, 0x5d, 0x29, 0xa8, 0xdf, 0x14, 0xd8, 0xff, 0xdb, 0x38, 0xef, 0xb8, 0xb8, 0x2a, 0x96,
			0x82, 0x8e, 0x68, 0xf4, 0x19, 0x8c, 0x42, 0xbe, 0xcc, 0x4a, 0x31, 0x21, 0xd5, 0x35, 0x6c, 0x5b, 0xa5, 0x7c,
			0xff, 0xd1, 0x85, 0x87, 0x28, 0xdc, 0x97, 0x75, 0xe8, 0x03, 0x80, 0x1d, 0xfd, 0x25, 0x34, 0x41, 0x31, 0x21,
			0x12, 0x87, 0xe8, 0x9a, 0xb7, 0x6a, 0xc0, 0xc4, 0x89, 0x31, 0x15, 0x45, 0x0d, 0x9c, 0xee, 0xf0, 0x6a, 0x2f,
			0xe8, 0x59, 0x45, 0xc7, 0x7b, 0x0d, 0x6c, 0x55, 0xbb, 0x43, 0xca, 0xc7, 0x5a, 0x01, 0x02, 0x81, 0x81, 0x00,
			0xab, 0xf4, 0xd5, 0xcf, 0x78, 0x88, 0x82, 0xc2, 0xdd, 0xbc, 0x25, 0xe6, 0xa2, 0xc1, 0xd2, 0x33, 0xdc, 0xef,
			0x0a, 0x97, 0x2b, 0xdc, 0x59, 0x6a, 0x86, 0x61, 0x4e, 0xa6, 0xc7, 0x95, 0x99, 0xa6, 0xa6, 0x55, 0x6c, 0x5a,
			0x8e, 0x72, 0x25, 0x63, 0xac, 0x52, 0xb9, 0x10, 0x69, 0x83, 0x99, 0xd3, 0x51, 0x6c, 0x1a, 0xb3, 0x83, 0x6a,
			0xff, 0x50, 0x58, 0xb7, 0x28, 0x97, 0x13, 0xe2, 0xba, 0x94, 0x5b, 0x89, 0xb4, 0xea, 0xba, 0x31, 0xcd, 0x78,
			0xe4, 0x4a, 0x00, 0x36, 0x42, 0x00, 0x62, 0x41, 0xc6, 0x47, 0x46, 0x37, 0xea, 0x6d, 0x50, 0xb4, 0x66, 0x8f,
			0x55, 0x0c, 0xc8, 0x99, 0x91, 0xd5, 0xec, 0xd2, 0x40, 0x1c, 0x24, 0x7d, 0x3a, 0xff, 0x74, 0xfa, 0x32, 0x24,
			0xe0, 0x11, 0x2b, 0x71, 0xad, 0x7e, 0x14, 0xa0, 0x77, 0x21, 0x68, 0x4f, 0xcc, 0xb6, 0x1b, 0xe8, 0x00, 0x49,
			0x13, 0x21, 0x02, 0x81, 0x81, 0x00, 0xb6, 0x18, 0x73, 0x59, 0x2c, 0x4f, 0x92, 0xac, 0xa2, 0x2e, 0x5f, 0xb6,
			0xbe, 0x78, 0x5d, 0x47, 0x71, 0x04, 0x92, 0xf0, 0xd7, 0xe8, 0xc5, 0x7a, 0x84, 0x6b, 0xb8, 0xb4, 0x30, 0x1f,
			0xd8, 0x0d, 0x58, 0xd0, 0x64, 0x80, 0xa7, 0x21, 0x1a, 0x48, 0x00, 0x37, 0xd6, 0x19, 0x71, 0xbb, 0x91, 0x20,
			0x9d, 0xe2, 0xc3, 0xec, 0xdb, 0x36, 0x1c, 0xca, 0x48, 0x7d, 0x03, 0x32, 0x74, 0x1e, 0x65, 0x73, 0x02, 0x90,
			0x73, 0xd8, 0x3f, 0xb5, 0x52, 0x35, 0x79, 0x1c, 0xee, 0x93, 0xa3, 0x32, 0x8b, 0xed, 0x89, 0x98, 0xf1, 0x0c,
			0xd8, 0x12, 0xf2, 0x89, 0x7f, 0x32, 0x23, 0xec, 0x67, 0x66, 0x52, 0x83, 0x89, 0x99, 0x5e, 0x42, 0x2b, 0x42,
			0x4b, 0x84, 0x50, 0x1b, 0x3e, 0x47, 0x6d, 0x74, 0xfb, 0xd1, 0xa6, 0x10, 0x20, 0x6c, 0x6e, 0xbe, 0x44, 0x3f,
			0xb9, 0xfe, 0xbc, 0x8d, 0xda, 0xcb, 0xea, 0x8f });

}
