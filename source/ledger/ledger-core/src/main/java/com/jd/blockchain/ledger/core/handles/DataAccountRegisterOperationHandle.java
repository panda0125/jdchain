package com.jd.blockchain.ledger.core.handles;

import com.jd.blockchain.ledger.BlockchainIdentity;
import com.jd.blockchain.ledger.DataAccountRegisterOperation;
import com.jd.blockchain.ledger.LedgerPermission;
import com.jd.blockchain.ledger.core.LedgerDataQuery;
import com.jd.blockchain.ledger.core.LedgerDataset;
import com.jd.blockchain.ledger.core.LedgerService;
import com.jd.blockchain.ledger.core.MultiIdsPolicy;
import com.jd.blockchain.ledger.core.OperationHandleContext;
import com.jd.blockchain.ledger.core.SecurityContext;
import com.jd.blockchain.ledger.core.SecurityPolicy;
import com.jd.blockchain.ledger.core.TransactionRequestExtension;

public class DataAccountRegisterOperationHandle extends AbstractLedgerOperationHandle<DataAccountRegisterOperation> {
	public DataAccountRegisterOperationHandle() {
		super(DataAccountRegisterOperation.class);
	}
	
	@Override
	protected void doProcess(DataAccountRegisterOperation op, LedgerDataset newBlockDataset,
			TransactionRequestExtension requestContext, LedgerDataQuery previousBlockDataset,
			OperationHandleContext handleContext, LedgerService ledgerService) {
		// TODO: 请求者应该提供数据账户的公钥签名，以更好地确保注册人对该地址和公钥具有合法使用权；

		// 权限校验；
		SecurityPolicy securityPolicy = SecurityContext.getContextUsersPolicy();
		securityPolicy.checkEndpoints(LedgerPermission.REGISTER_DATA_ACCOUNT, MultiIdsPolicy.AT_LEAST_ONE);

		// 操作账本；
		DataAccountRegisterOperation dataAccountRegOp = (DataAccountRegisterOperation) op;
		BlockchainIdentity bid = dataAccountRegOp.getAccountID();
		newBlockDataset.getDataAccountSet().register(bid.getAddress(), bid.getPubKey(), null);
	}

}
