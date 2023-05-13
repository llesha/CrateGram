typealias Position = Int

inline fun <T : Any, R> T?.ifNotNull(action: T.() -> R) = if (this != null) action(this) else null