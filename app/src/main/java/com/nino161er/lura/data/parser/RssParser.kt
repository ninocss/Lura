package com.nino161er.rssfeed.data.parser

import com.nino161er.rssfeed.data.model.RssItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

data class ParsedFeed(
    val title: String,
    val description: String?,
    val iconUrl: String?,
    val items: List<RssItem>
)

class RssParser {

    fun parse(xml: String, feedId: Long): ParsedFeed {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var feedTitle = ""
        var feedDescription: String? = null
        var feedIconUrl: String? = null
        val items = mutableListOf<RssItem>()

        var eventType = parser.eventType
        var currentTag = ""
        var inItem = false
        var inChannel = false
        var inImage = false

        var title = ""
        var link = ""
        var description: String? = null
        var pubDate: String? = null
        var content: String? = null
        var imageUrl: String? = null

        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        when (currentTag) {
                            "channel" -> inChannel = true
                            "image" -> if (!inItem) inImage = true
                            "item" -> {
                                inItem = true
                                title = ""
                                link = ""
                                description = null
                                pubDate = null
                                content = null
                                imageUrl = null
                            }
                            "enclosure" -> {
                                if (inItem && imageUrl == null) {
                                    val type = parser.getAttributeValue(null, "type")
                                    if (type?.startsWith("image/") == true) {
                                        imageUrl = parser.getAttributeValue(null, "url")
                                    }
                                }
                            }
                            "content" -> {
                                if (inItem && imageUrl == null) {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url != null) imageUrl = url
                                }
                            }
                        }
                        
                        if (inItem && imageUrl == null && (currentTag == "content" || currentTag == "thumbnail")) {
                            imageUrl = parser.getAttributeValue(null, "url")
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        if (inItem) {
                            when (currentTag) {
                                "title" -> title += text
                                "link" -> link += text
                                "description" -> description = (description ?: "") + text
                                "pubDate" -> pubDate = (pubDate ?: "") + text
                                "encoded" -> content = (content ?: "") + text
                            }
                        } else if (inImage) {
                            if (currentTag == "url") feedIconUrl = text
                        } else if (inChannel) {
                            when (currentTag) {
                                "title" -> if (feedTitle.isEmpty()) feedTitle += text
                                "description" -> if (feedDescription == null) feedDescription = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "item" -> {
                                val itemDescription = description?.let { sanitizeHtml(it) }
                                val itemContent = content?.let { sanitizeHtml(it) }
                                
                                val item = RssItem(
                                    feedId = feedId,
                                    title = title.ifEmpty { "(No title)" },
                                    link = link.ifEmpty { null },
                                    description = itemDescription,
                                    pubDate = pubDate,
                                    content = itemContent,
                                    imageUrl = imageUrl ?: extractImageUrl(description, content)
                                )
                                items.add(item)
                                inItem = false
                            }
                            "image" -> inImage = false
                            "channel" -> inChannel = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw RssParseException("Failed to parse RSS XML", e)
        } catch (e: IOException) {
            throw RssParseException("IO error during RSS parsing", e)
        }

        return ParsedFeed(
            title = feedTitle.ifEmpty { "Unknown Feed" },
            description = feedDescription,
            iconUrl = feedIconUrl ?: "https://www.google.com/s2/favicons?domain=${getHost(link)}&sz=128",
            items = items
        )
    }

    private fun getHost(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun sanitizeHtml(text: String): String {
        return text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("&nbsp;", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("&amp;", RegexOption.IGNORE_CASE), "&")
            .replace(Regex("&lt;", RegexOption.IGNORE_CASE), "<")
            .replace(Regex("&gt;", RegexOption.IGNORE_CASE), ">")
            .replace(Regex("&quot;", RegexOption.IGNORE_CASE), "\"")
            .replace(Regex("&#39;", RegexOption.IGNORE_CASE), "'")
            .replace(Regex("\\s*\n\\s*"), "\n")
            .replace(Regex(" +"), " ")
            .trim()
    }

    private fun extractImageUrl(description: String?, content: String?): String? {
        val text = content ?: description ?: return null
        val regex = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.getOrNull(1)
    }
}

class RssParseException(message: String, cause: Throwable) : Exception(message, cause)
