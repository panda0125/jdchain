package com.jd.blockchain.consensus.bftsmart.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bftsmart.reconfiguration.util.TOMConfiguration;
import com.jd.blockchain.consensus.ClientIdentification;
import com.jd.blockchain.consensus.ConsensusManageService;
import com.jd.blockchain.consensus.bftsmart.BftsmartClientIncomingConfig;
import com.jd.blockchain.consensus.bftsmart.BftsmartClientIncomingSettings;
import com.jd.blockchain.consensus.bftsmart.BftsmartTopology;
import com.jd.blockchain.crypto.Crypto;
import com.jd.blockchain.crypto.SignatureFunction;
import com.jd.blockchain.utils.serialize.binary.BinarySerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BftsmartConsensusManageService implements ConsensusManageService {

	private static Logger LOGGER = LoggerFactory.getLogger(BftsmartConsensusManageService.class);

	public static final int GATEWAY_SIZE = 100;

	public static final int CLIENT_SIZE_PER_GATEWAY = 1000;

	public static final int CLIENT_RANGE = GATEWAY_SIZE * CLIENT_SIZE_PER_GATEWAY;

	private BftsmartNodeServer nodeServer;

	private int clientId;

	private static final Lock authLock = new ReentrantLock();

	public BftsmartConsensusManageService(BftsmartNodeServer nodeServer) {
		this.nodeServer = nodeServer;
		// Assume that each peer node corresponds to up to 100 gateways
		clientId = nodeServer.getServerId() * CLIENT_RANGE;
	}

	@Override
	public BftsmartClientIncomingSettings authClientIncoming(ClientIdentification authId) {
		if (verify(authId)) {
			byte[] topology = BinarySerializeUtils.serialize(nodeServer.getTopology());
			byte[] tomConfig = BinarySerializeUtils.serialize(nodeServer.getTomConfig());

			LOGGER.info("topology:{}, tomConfig:{}", topology == null ? 0 : topology.length,
					tomConfig == null ? 0: tomConfig.length);

			BftsmartClientIncomingConfig clientIncomingSettings = new BftsmartClientIncomingConfig();

			clientIncomingSettings
					.setTopology(BinarySerializeUtils.serialize(nodeServer.getTopology()));

			clientIncomingSettings
					.setTomConfig(BinarySerializeUtils.serialize(nodeServer.getTomConfig()));

			clientIncomingSettings
					.setConsensusSettings(nodeServer.getConsensusSetting());

			clientIncomingSettings.setPubKey(authId.getPubKey());
			// compute gateway id
			authLock.lock();
			try {
				clientIncomingSettings.setClientId(clientId++);
				clientId += CLIENT_SIZE_PER_GATEWAY;
			} finally {
				authLock.unlock();
			}

			return clientIncomingSettings;
		}

		return null;
	}

	public boolean verify(ClientIdentification authId) {

		SignatureFunction signatureFunction = Crypto
				.getSignatureFunction(authId.getPubKey().getAlgorithm());

		return signatureFunction.verify(authId.getSignature(), authId.getPubKey(), authId.getIdentityInfo());
	}
}
