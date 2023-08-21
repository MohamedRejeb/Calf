fun main() {
    println("LaunchedEffect")
    val notif = UNUserNotificationCenter.currentNotificationCenter()
    notif.requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
        completionHandler = { granted, error ->
            if (granted) {
                scope.launch(Dispatchers.Main) {
                    val content = UNMutableNotificationContent().apply {
                        setTitle("Hello")
                        setBody("World")
                        setSound(UNNotificationSound.defaultSound())
                    }
                    notif.addNotificationRequest(
                        UNNotificationRequest.requestWithIdentifier(
                            identifier = "hello",
                            content = content,
                            trigger = null
                        ),
                        null
                    )
                }
            }
        }
    )
}