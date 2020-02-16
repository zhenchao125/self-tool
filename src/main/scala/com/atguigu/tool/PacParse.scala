package com.atguigu.tool


import java.io.PrintWriter
import java.net.{HttpURLConnection, InetAddress, InetSocketAddress}

import org.apache.commons.codec.binary.Base64

import scala.io.{BufferedSource, Source}

object PacParse {
    val hostName = InetAddress.getLocalHost.getHostName
    val localFile = s"/Users/$hostName/Library/Application Support/V2RayX/pac/pac.js"
    val pacUrl = "https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt"
    val my = List("||233v2.com",
        "||https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt")
    
    def main(args: Array[String]): Unit = {
        
        val sourcePac = readPacTextFromWeb()
        val pac: String = parsePac(sourcePac)
        val localPacJsLines: List[String] = readLocalPacjs()
        val i: Int = localPacJsLines.indexOf("var domains = [")
        val j: Int = localPacJsLines.indexOf("];")
        val pre: List[String] = localPacJsLines.slice(0, i + 1)
        val post: List[String] = localPacJsLines.slice(j, localPacJsLines.length)
        val lines = pac.split("[\r\n]").toList ::: my
        val urls = lines
            .filter(line => line.matches("""^[\.\|].*"""))
            .map(line => {
                s""" "${line.replaceAll("""^(\.|\|{1,2})""", "")}" """
            }).mkString(",\n")
        
        val result = pre.mkString("\n") + "\n" + urls + "\n" + post.mkString("\n")
        writeToPacJs(result)
    }
    
    def writeToPacJs(content: String) = {
        val writer = new PrintWriter(localFile)
        writer.write(content)
        writer.flush()
        writer.close()
    }
    
    /**
     * 从 github 读取 pac 原始文件
     * 需要使用代理去访问列表
     * 如果代理不能使用则, 直接连接
     *
     * @return
     */
    def readPacTextFromWeb(): String = {
        try {
            val proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8001))
            val connection: HttpURLConnection = new java.net.URL(pacUrl).openConnection(proxy).asInstanceOf[HttpURLConnection]
            connection.connect()
            Source.fromInputStream(connection.getInputStream).mkString.replaceAll("\r", "")
        } catch {
            case _ =>
                val connection: HttpURLConnection = new java.net.URL(pacUrl).openConnection().asInstanceOf[HttpURLConnection]
                connection.connect()
                Source.fromInputStream(connection.getInputStream).mkString.replaceAll("\r", "")
        }
        
    }
    
    /**
     * 解析原始 pac 文件, 使用的是 base64
     */
    def parsePac(sourcePac: String): String = {
        val base64 = new Base64()
        new String(base64.decode(sourcePac))
    }
    
    def readLocalPacjs() = {
        
        val file: BufferedSource = Source.fromFile(localFile)
        val pacJsLines = file.getLines().toList
        file.close()
        pacJsLines
    }
}
