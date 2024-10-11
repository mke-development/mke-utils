package team.mke.utils.ktor

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import team.mke.utils.env.Environment

/** [XML] по умолчанию */
@OptIn(ExperimentalXmlUtilApi::class)
val xml = XML {
    xmlDeclMode = XmlDeclMode.Charset
    xmlVersion = XmlVersion.XML10
    defaultPolicy {
        if (Environment.isDev()) {
            indentString = "\t"
        }
        autoPolymorphic = true
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }
}