package wikidata.UCI.edu

import java.net.URLEncoder
import java.io.PrintWriter
import com.github.jsonldjava.utils._
import com.github.jsonldjava.core._
import java.io.InputStream
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import scala.io.Source

object WikibaseIdFinder 
{
    def getWikibaseIds(filename: String): HashMap[String, String] =
    {
        val map = new HashMap[String, String]
        val apiUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=pageprops&titles=pageTitle&format=json"
        val source = Source.fromFile(filename)

        var unresolved = 0
        var completed = 0
        var lines = 0

        for (line <- source.getLines())
        {
            lines = lines + 1
            var pageTitle = line
            if (pageTitle.contains("https://en.wikipedia.org/wiki/")) pageTitle = line.split("\\/").last
            if (!pageTitle.contains("#") && pageTitle != "")
            {
                val encodedPageTitle = Utilities.encodeURL(pageTitle, "UTF-8")
                val newUrl = apiUrl.replace("pageTitle", encodedPageTitle).replaceAll(" ", "_")
                //println(newUrl)
                val response = io.Source.fromInputStream(Utilities.get(newUrl), "UTF-8").mkString
                val split = response.split("wikibase_item")
                if (split.size == 1) 
                {
                    println("Could not find wikibase Id for: " + pageTitle)
                    unresolved = unresolved + 1
                }
                else 
                {
                    val thisId = split(1).replaceAll("\"","").replaceAll("\\}","").replaceAll("\\:","")
                    map.put(line, thisId)
                    completed = completed + 1
                }
            }
        }
        println("Resolved " + completed + " links and left " + unresolved + " unresolved out of " + lines + " lines of input")
        source.close()
        map
    }
}