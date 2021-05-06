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
    def getWikibaseIds(filename: String): (HashMap[String, String], ArrayBuffer[String]) =
    {
        val map = new HashMap[String, String]
        val list = new ArrayBuffer[String]
        val apiUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=pageprops&titles=pageTitle&format=json"
        val source = Source.fromFile(filename)
        for (line <- source.getLines())
        {
            var pageTitle = line
            if (pageTitle.contains("https://en.wikipedia.org/wiki/")) pageTitle = line.split("\\/").last
            if (!pageTitle.contains("#"))
            {
                val newUrl = apiUrl.replace("pageTitle", pageTitle)
                //println(newUrl)
                val response = io.Source.fromInputStream(Utilities.get(newUrl), "UTF-8").mkString
                val split = response.split("wikibase_item")
                if (split.size == 1) println("Could not find wikibase Id for: " + pageTitle)
                else 
                {
                    val thisId = split(1).replaceAll("\"","").replaceAll("\\}","").replaceAll("\\:","")
                    map.put(line, thisId)
                    list += "wd:" + thisId
                }
            }
        }
        source.close()
        (map, list)
    }
}