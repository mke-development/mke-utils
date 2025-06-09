package team.mke.utils.mail.term

import javax.mail.Message
import javax.mail.search.SearchTerm

class RegexSubjectTerm(private val regex: Regex) : SearchTerm() {
    override fun match(msg: Message?): Boolean {
        return msg?.subject?.matches(regex) ?: false
    }
}