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

object Utilities 
{
    def writeQueryResToFile(res: String, outputFile: String)
    {
        if (res != "")
        {
            // remove special characters
            var normalizedRes = java.text.Normalizer.normalize(res, java.text.Normalizer.Form.NFD)
            normalizedRes = normalizedRes.replaceAll("[^\\p{ASCII}]", "")
            // this cleans up the concatenated JSON-LD chunks and makes it one chunk
            normalizedRes = normalizedRes.replaceAll("\\]\\[", ",")
            // write to file
            val pw = new PrintWriter(outputFile) 
            pw.write(normalizedRes)
            pw.close() 
            println("Output written to " + outputFile)
        }
        else println("Nothing to write to file")
    }
    
    def encodeURL(url: String, params: String, encodingScheme: String = "UTF-8"): String =
    {
        url + URLEncoder.encode(params, encodingScheme)
    }

     def encodeURL(url: String, encodingScheme: String): String =
    {
        URLEncoder.encode(url, encodingScheme)
    }
    
    def get(url: String, connectTimeout: Int = 5000, readTimeout: Int = 100000, acceptType: String = "application/ld+json"): InputStream =
    {
        import java.net.{URL, HttpURLConnection}
        val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
        connection.setConnectTimeout(connectTimeout)
        connection.setReadTimeout(readTimeout)
        connection.setRequestMethod("GET")
        connection.setRequestProperty("Accept", acceptType);
        val inputStream = connection.getInputStream
        inputStream
    }
    
    def convertToByteArray(is: InputStream): Array[Byte] =
    {
        val baos: ByteArrayOutputStream = new ByteArrayOutputStream();
        org.apache.commons.io.IOUtils.copy(is, baos)
        baos.toByteArray()
    }  
}