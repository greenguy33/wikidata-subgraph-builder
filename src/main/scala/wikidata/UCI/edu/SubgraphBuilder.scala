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

object SubgraphBuilder 
{    
    val urlHead = "https://query.wikidata.org/bigdata/namespace/wdq/sparql?query="
    var outputFile = ""

    def main(args: Array[String]): Unit =
    {
        var res: String = ""
        if (args.size > 2)
        {
            val start = args(1)
            var includeProps = false
            var includeTypes = false
            if (args.contains("-p") || args.contains("--properties")) includeProps = true
            
            val singleEdgeQuery = 
              s"""construct {
                  ?s wdt:$start ?o .
                  wdt:$start rdfs:label ?edgeLabel .
                  ?s rdfs:label ?s_label .
                  ?o rdfs:label ?o_label .
                  } where {
                    ?s wdt:$start ?o .
                    SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                    ?edge wikibase:directClaim wdt:$start .
                    Optional {?s rdfs:label ?s_label filter (lang(?s_label) = "en") .}
                    Optional {?o rdfs:label ?o_label filter(lang(?o_label) = "en") .}
                  }"""
              
            val singleNodeWithPropertiesQuery = 
               s"""construct {
                      wd:$start ?p ?o
                    } where {
                      wd:$start ?p ?o }"""
          
            outputFile = args.last
            /*if (args(0) == "centralNode" && args.size > 3)
            {
                val hops = args(2).toInt
                res = get(encodeURL(urlHead, centralNodeQuery))
            }
            else */if (args(0) == "singleEdge") buildSingleEdgeGraph(singleEdgeQuery, start, includeProps)
            else if (args(0) == "buildFromWikipediaURLs") buildWikidataGraphFromWikipediaURLs(start, outputFile)
            else println("No valid command line argument provided")
        }
        else println("No valid command line argument provided")
    }
    
    def buildWikidataGraphFromWikipediaURLs(wikipediaURLFile: String, outputFile: String)
    {
        var res = ""
        println("Collecting Wikibase Ids")
        val idMap = WikibaseIdFinder.getWikibaseIds(wikipediaURLFile)

        val batchSize = 200
        val wdIds = idMap.values
        val batchedIds = wdIds.grouped(batchSize).toList
        println("Processing " + idMap.keys.size + " Wikidata items")
        
        for (id <- wdIds)
        {
            //println("id: " + id)
            for (batch <- batchedIds)
            {
                 val wikidataListAsString = batch.mkString("wd:"," wd:","")
                 val getNodeConnections = 
                   s"""construct {
                        ?s ?p ?o .
                        ?p rdfs:label ?edgeLabel .
                        } where {
                        values ?o {$wikidataListAsString}
                        values ?s {wd:$id}
                        ?s ?p ?o .
                        SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                        ?edge wikibase:directClaim ?p .
                        }"""

                 //println(getNodeConnections)
                 val encodedQuery = Utilities.encodeURL(urlHead, getNodeConnections, "UTF-8")
                 //println(encodedQuery)
                 val is: InputStream = Utilities.get(encodedQuery)
                 val jsonld = io.Source.fromInputStream(is, "UTF-8").mkString
                 if (jsonld.length > 3) res += jsonld
            }
        }

        for ((k,v) <- idMap) res = res.replaceAll("\"http://www.wikidata.org/entity/"+v+"\"","\""+k+"\"")
        Utilities.writeQueryResToFile(res, outputFile)
    }
    
    def buildSingleEdgeGraph(query: String, start: String, includeProps: Boolean)
    {
        println("Extracting nodes/relationships")
        val foundNodes = new HashSet[String]
        val ba: Array[Byte] = Utilities.convertToByteArray(Utilities.get(Utilities.encodeURL(urlHead, query)))
        val bais = new ByteArrayInputStream(ba)
        val nodesRes = io.Source.fromInputStream(bais, "UTF-8").mkString; bais.reset()
        val jsonRes = JsonUtils.fromInputStream(bais).asInstanceOf[java.util.ArrayList[java.util.LinkedHashMap[String, Object]]]
        for (element <- jsonRes)
        {
            foundNodes += element.get("@id").asInstanceOf[String]
            if (element.contains("http://www.wikidata.org/prop/direct/"+start))
            {
                for (map <- element.get("http://www.wikidata.org/prop/direct/"+start).asInstanceOf[java.util.ArrayList[java.util.LinkedHashMap[String, String]]])
                {
                    foundNodes += map.get("@id").asInstanceOf[String]
                }
            }
        }
        var propsRes = ""
        if (includeProps) propsRes = getPropertiesOfNodes(foundNodes)
        Utilities.writeQueryResToFile(nodesRes + propsRes, outputFile)
    }
    
    def getPropertiesOfNodes(nodes: HashSet[String], batchSize: Integer = 25): String =
    {
        println("Extracting properties")
        val chunkedNodes = nodes.grouped(batchSize).toArray
        var res = ""
        var chunkNum = 0
        for (chunk <- chunkedNodes)
        {
            chunkNum = chunkNum + 1
            var chunkedNodesAsString = ""
            for (node <- chunk) chunkedNodesAsString += "<" + node + "> "
            val propertiesQuery = s"""
              Construct {
                ?node ?property ?value .
                ?property rdfs:label ?propLabel .
              }
              Where {
                Values ?node {$chunkedNodesAsString}
                ?node ?property ?value .
                ?property a owl:DatatypeProperty .
                SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                    ?prop wikibase:directClaim ?property .
              }
              """
            println("Batch: " + chunkNum + " / " + chunkedNodes.size)
            val is: InputStream = Utilities.get(Utilities.encodeURL(urlHead, propertiesQuery))
            res += io.Source.fromInputStream(is, "UTF-8").mkString
        }
        //for (node <- nodes) if (!res.contains(node)) println("No properties of node " + node + " were included in the result")
        res
    }
}