package com.mohamedrejeb.calf.io

import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Every file reopened from a bookmark of the same location shares one security-scoped access, so
 * reopening a bookmark over and over, without releasing, does not pile up access. The simulator
 * never hands out real security-scoped URLs, so start and stop are counted instead.
 */
class SecurityScopedAccessRegistryTest {
    private var started = 0
    private var stopped = 0
    private val registry = SecurityScopedAccessRegistry(
        start = { started++; true },
        stop = { stopped++ },
    )
    private val folder = NSURL.fileURLWithPath("/private/var/mobile/Calf Exports", isDirectory = true)

    @Test
    fun reopeningALocationHoldsOneAccess() {
        repeat(3) { registry.acquire(folder) }

        assertEquals(1, started)
        assertEquals(0, stopped)
    }

    @Test
    fun accessStopsWhenTheLastFileIsReleased() {
        val first = registry.acquire(folder)
        val second = registry.acquire(folder)

        first.release()
        assertEquals(0, stopped)
        second.release()
        assertEquals(1, stopped)
    }

    @Test
    fun releasingTheSameFileTwiceCountsOnce() {
        val first = registry.acquire(folder)
        val second = registry.acquire(folder)

        first.release()
        first.release()
        assertEquals(0, stopped, "the second file still holds the access")
        second.release()
        assertEquals(1, stopped)
    }

    @Test
    fun aLocationOpenedAgainAfterItsLastReleaseStartsAgain() {
        registry.acquire(folder).release()
        registry.acquire(folder)

        assertEquals(2, started)
    }

    @Test
    fun eachLocationHasItsOwnAccess() {
        val other = NSURL.fileURLWithPath("/private/var/mobile/Other", isDirectory = true)

        registry.acquire(folder)
        registry.acquire(other).release()

        assertEquals(2, started)
        assertEquals(1, stopped)
    }

    @Test
    fun aStartThatFailedIsRetriedByTheNextFile() {
        val results = ArrayDeque(listOf(false, true))
        val retrying = SecurityScopedAccessRegistry(
            start = { started++; results.removeFirst() },
            stop = { stopped++ },
        )

        val first = retrying.acquire(folder)
        val second = retrying.acquire(folder)
        first.release()
        second.release()

        assertEquals(2, started)
        assertEquals(1, stopped, "the access that did start is stopped once")
    }

    @Test
    fun aUrlThatIsNotSecurityScopedIsNeverStopped() {
        val unscoped = SecurityScopedAccessRegistry(start = { started++; false }, stop = { stopped++ })

        unscoped.acquire(folder).release()

        assertEquals(0, stopped)
    }
}
