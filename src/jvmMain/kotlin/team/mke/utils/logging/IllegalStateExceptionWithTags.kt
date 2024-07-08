package team.mke.utils.logging

class IllegalStateExceptionWithTags(message: String, vararg val tags: ErrorTag) : IllegalStateException(message)