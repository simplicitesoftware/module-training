package com.simplicite.commons.Training;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared code TrnTokenBucket
 */
public class TrnTokenBucket implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private final int capacity;
	private int tokens;
	private long lastRefillTimestamp;
	private final Lock lock = new ReentrantLock();

	public TrnTokenBucket(int capacity) {
		this.capacity = capacity;
		this.tokens = capacity;
		this.lastRefillTimestamp = System.currentTimeMillis();
	}

	public boolean getToken() {
		lock.lock();
		try {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - lastRefillTimestamp;
			// Refill 1 token per second
			int tokensToAdd = (int) (elapsedTime / 1000);
			tokens = Math.min(tokens + tokensToAdd, capacity);
			lastRefillTimestamp = currentTime;
			if (tokens > 0) {
				tokens--;
				// Grant a token
				return true;
			}
			// No tokens available
			return false;
		} finally {
			lock.unlock();
		}
	}

}
