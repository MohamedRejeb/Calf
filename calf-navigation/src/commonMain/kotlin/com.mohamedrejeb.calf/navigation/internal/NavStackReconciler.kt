package com.mohamedrejeb.calf.navigation.internal

/**
 * Result of reconciling a back-stack snapshot against the previously
 * materialized hosts.
 *
 * @property stack the new (key, host) list, in stack order.
 * @property dropped hosts whose entry left the back-stack; the caller must
 *   tear them down (clear ViewModels, detach views).
 */
internal data class NavStackDiff<K, H>(
    val stack: List<Pair<K, H>>,
    val dropped: List<H>,
)

/**
 * Reconciles [previous] (the hosts currently materialized, in stack order)
 * against [snapshot] (the new back-stack), reusing hosts whose key is still
 * present and calling [createHost] for new keys.
 *
 * Duplicate keys are first-class: each occurrence in [snapshot] consumes one
 * unused host with an equal key from [previous], bottom-up. Pushing the same
 * key twice therefore yields two *distinct* hosts — required on iOS, where a
 * `UIViewController` instance may appear at most once in
 * `UINavigationController.viewControllers` — and popping one occurrence keeps
 * the bottom host (and its ViewModels) alive.
 *
 * Pure function — no platform types — so the duplicate/pop/middle-removal
 * semantics are verified in commonTest.
 */
internal fun <K, H> reconcileNavStack(
    previous: List<Pair<K, H>>,
    snapshot: List<K>,
    createHost: (K) -> H,
): NavStackDiff<K, H> {
    val reusable = HashMap<K, ArrayDeque<H>>()
    previous.forEach { (key, host) ->
        reusable.getOrPut(key) { ArrayDeque() }.addLast(host)
    }

    val stack = snapshot.map { key ->
        val reused = reusable[key]?.removeFirstOrNull()
        key to (reused ?: createHost(key))
    }

    val dropped = reusable.values.flatten()
    return NavStackDiff(stack = stack, dropped = dropped)
}
