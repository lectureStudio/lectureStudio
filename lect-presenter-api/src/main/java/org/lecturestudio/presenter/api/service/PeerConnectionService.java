/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.RTCStatsReport;
import dev.onvoid.webrtc.media.video.VideoFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.presenter.api.model.ChatMessage;
import org.lecturestudio.presenter.api.model.Contact;
import org.lecturestudio.presenter.api.model.Contacts;
import org.lecturestudio.presenter.api.model.Room;
import org.lecturestudio.presenter.api.model.RoomParameters;
import org.lecturestudio.presenter.api.net.webrtc.PeerConnectionClient;
import org.lecturestudio.presenter.api.net.webrtc.PeerConnectionContext;
import org.lecturestudio.presenter.api.net.webrtc.SignalingClient;
import org.lecturestudio.presenter.api.net.webrtc.SignalingListener;
import org.lecturestudio.presenter.api.net.webrtc.config.Configuration;

@Singleton
public class PeerConnectionService implements SignalingListener {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final Configuration config;

	private final SignalingClient signalingClient;

	private final PeerConnectionContext peerConnectionContext;

	private final Map<Contact, PeerConnectionClient> connections;

	private Contact activeContact;


	@Inject
	PeerConnectionService(Configuration config, SignalingClient client) {
		this.config = config;

		signalingClient = client;
		signalingClient.setSignalingListener(this);

		connections = new HashMap<>();

		peerConnectionContext = new PeerConnectionContext();
		peerConnectionContext.audioDirection = RTCRtpTransceiverDirection.SEND_RECV;
		peerConnectionContext.videoDirection = RTCRtpTransceiverDirection.SEND_RECV;
	}

	@Override
	public void onRoomJoined(RoomParameters parameters) {
		peerConnectionContext.videoDirection = RTCRtpTransceiverDirection.SEND_RECV;
		activeContact = new Contact();

		config.getRTCConfig().iceServers.clear();
		config.getRTCConfig().iceServers.addAll(parameters.getIceServers());

		CompletableFuture.runAsync(() -> {
			var peerConnectionClient = createPeerConnection(activeContact);

			if (parameters.isInitiator()) {
				peerConnectionClient.initCall();
			}
		}).join();
	}

	@Override
	public void onRoomLeft() {

	}

	@Override
	public void onRemoteSessionDescription(Contact contact,
										   RTCSessionDescription description) {
		CompletableFuture.runAsync(() -> {
			if (description.sdpType == RTCSdpType.OFFER) {
				createPeerConnection(contact);
			}

			var peerConnectionClient = getPeerConnectionClient(contact);

			peerConnectionClient.setSessionDescription(description);
		});
	}

	@Override
	public void onRemoteIceCandidate(Contact contact,
									 RTCIceCandidate candidate) {
		CompletableFuture.runAsync(() -> {
			var peerConnectionClient = getPeerConnectionClient(contact);

			peerConnectionClient.addIceCandidate(candidate);
		});
	}

	@Override
	public void onRemoteIceCandidatesRemoved(Contact contact,
											 RTCIceCandidate[] candidates) {
		CompletableFuture.runAsync(() -> {
			var peerConnectionClient = getPeerConnectionClient(contact);

			peerConnectionClient.removeIceCandidates(candidates);
		});
	}

	@Override
	public void onError(String message) {

	}

	public void dispose() {
		executor.shutdown();
	}

	public void setContactEventConsumer(BiConsumer<Contact, Boolean> consumer) {

	}

	public void setOnConnectionState(BiConsumer<Contact, RTCPeerConnectionState> consumer) {
		peerConnectionContext.onPeerConnectionState = consumer;
	}

	public void setOnRemoteFrame(BiConsumer<Contact, VideoFrame> consumer) {
		peerConnectionContext.onRemoteFrame = consumer;
	}

	public void setOnLocalFrame(Consumer<VideoFrame> consumer) {
		peerConnectionContext.onLocalFrame = consumer;
	}

	public void setOnStatsReport(Consumer<RTCStatsReport> consumer) {
		peerConnectionContext.onStatsReport = consumer;
	}

	public void setOnMessage(BiConsumer<Contact, ChatMessage> consumer) {
		peerConnectionContext.onMessage = consumer;
	}

	public void setOnRemoteVideoStream(BiConsumer<Contact, Boolean> consumer) {
		peerConnectionContext.onRemoteVideoStream = consumer;
	}

	public void setOnLocalVideoStream(Consumer<Boolean> consumer) {
		peerConnectionContext.onLocalVideoStream = consumer;
	}

	public boolean hasLocalVideoStream() {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		return nonNull(peerConnectionClient) && peerConnectionClient.hasLocalVideoStream();
	}

	public boolean hasRemoteVideoStream() {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		return nonNull(peerConnectionClient) && peerConnectionClient.hasRemoteVideoStream();
	}

	public CompletableFuture<Contacts> getContacts() {
		return CompletableFuture.supplyAsync(() -> {
			Contacts contacts = new Contacts();

			try {
				contacts.addAll(signalingClient.getContacts());
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			return contacts;
		});
	}

	public CompletableFuture<Void> login(Contact asContact, Room room) {
		return CompletableFuture.runAsync(() -> {
			try {
				signalingClient.joinRoom(asContact, room);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> logout() {
		CompletableFuture<Void> closing = closeConnections();

		return CompletableFuture.allOf(closing, CompletableFuture.runAsync(() -> {
			try {
				signalingClient.leaveRoom();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		}));
	}

	public CompletableFuture<Void> sendMessage(ChatMessage message, Contact toContact) {
		var peerConnectionClient = getPeerConnectionClient(toContact);

		return peerConnectionClient.sendMessage(message);
	}

	public CompletableFuture<Void> call(Contact contact, boolean enableVideo) {
		peerConnectionContext.videoDirection = enableVideo ?
				RTCRtpTransceiverDirection.SEND_RECV :
				RTCRtpTransceiverDirection.INACTIVE;

		return CompletableFuture.runAsync(() -> {
			var peerConnectionClient = createPeerConnection(contact);
			peerConnectionClient.initCall();

			activeContact = contact;
		});
	}

	public void setDesktopActive(boolean active) {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		if (nonNull(peerConnectionClient)) {
			peerConnectionClient.setDesktopActive(active);
		}
	}

	public void setMicrophoneActive(boolean active) {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		if (nonNull(peerConnectionClient)) {
			peerConnectionClient.setMicrophoneActive(active);
		}
	}

	public void setCameraActive(boolean active) {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		if (nonNull(peerConnectionClient)) {
			peerConnectionClient.setCameraActive(active);
		}
	}

	public void enableStats(boolean active) {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		if (nonNull(peerConnectionClient)) {
			peerConnectionClient.enableStatsEvents(active, 5000);
		}
	}

	public CompletableFuture<Void> hangup() {
		var peerConnectionClient = getPeerConnectionClient(activeContact);

		if (isNull(peerConnectionClient)) {
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> close = peerConnectionClient.close();
		close.thenRun(() -> activeContact = null);

		return CompletableFuture.allOf(close, logout());
	}

	private CompletableFuture<Void> closeConnections() {
		var clients = connections.values();
		var iter = clients.iterator();

		CompletableFuture<?>[] list = new CompletableFuture[clients.size()];

		int index = 0;

		while (iter.hasNext()) {
			var client = iter.next();
			iter.remove();

			list[index++] = client.close();
		}

		return CompletableFuture.allOf(list);
	}

	private PeerConnectionClient getPeerConnectionClient(Contact contact) {
		return connections.get(contact);
	}

	private PeerConnectionClient removePeerConnectionClient(Contact contact) {
		return connections.remove(contact);
	}

	private PeerConnectionClient createPeerConnection(Contact contact) {
		var peerConnectionClient = getPeerConnectionClient(contact);

		if (nonNull(peerConnectionClient)) {
			return peerConnectionClient;
		}

		peerConnectionClient = new PeerConnectionClient(config, contact,
				peerConnectionContext, signalingClient, executor);

		connections.put(contact, peerConnectionClient);

		return peerConnectionClient;
	}

}
