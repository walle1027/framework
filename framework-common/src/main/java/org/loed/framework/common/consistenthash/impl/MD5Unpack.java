package org.loed.framework.common.consistenthash.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.loed.framework.common.consistenthash.HashFunction;

import java.nio.ByteBuffer;

public class MD5Unpack implements HashFunction {
	@Override
	public int hash(String str) {
		byte[] bytes = DigestUtils.md5(str);
		return ByteBuffer.wrap(bytes).getInt(0);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
