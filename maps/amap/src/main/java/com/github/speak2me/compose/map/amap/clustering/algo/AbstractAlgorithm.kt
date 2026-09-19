package com.github.speak2me.compose.map.amap.clustering.algo

import com.github.speak2me.compose.map.amap.clustering.ClusterItem
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Base Algorithm class that implements lock/unlock functionality.
 */
public abstract class AbstractAlgorithm<T : ClusterItem> : Algorithm<T> {
    private val mLock: ReadWriteLock = ReentrantReadWriteLock()

    override fun lock() {
        mLock.writeLock().lock()
    }

    override fun unlock() {
        mLock.writeLock().unlock()
    }
}