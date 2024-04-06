package com.example.networksdk

import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object CustomNetwork {
    const val GET = "GET"
    const val POST = "POST"
    const val DELETE = "DELETE"
    const val PUT = "PUT"


    class HttpRequest(internal val httpMethod: String) {
        internal val header: MutableMap<String, String> = HashMap()
        internal var url: String? = null
        internal var body: ByteArray? = null
        private var jsonObjectRequestListener: JsonObjectListener? = null
        private var coroutineExecutor: CoroutineExecutor = CoroutineExecutor()

        fun url(url: String?): HttpRequest {
            this.url = url
            return this
        }

        fun body(jsonBody: JSONObject?): HttpRequest {
            val textBody = jsonBody?.toString()
            body = textBody?.toByteArray(Charsets.UTF_8)
            this.header["Content-type"] = "application/json"
            return this
        }

        fun header(header: Map<String, String>?): HttpRequest {
            if (header?.isEmpty() == true) return this
            header?.let { this.header.putAll(it) }
            return this
        }

        fun makeRequest(jsonObjectListener: JsonObjectListener?): HttpRequest {
            this.jsonObjectRequestListener = jsonObjectListener
            coroutineExecutor.execute {
                RequestTask(this).run()
            }
            return this
        }

        internal fun sendResponse(response: Response?, exception: Exception?) {
            if (jsonObjectRequestListener != null) {
                if (exception != null) jsonObjectRequestListener?.onFailure(exception)
                else jsonObjectRequestListener?.onResponse(response?.asJSONObject())
            }
        }
    }

    internal class RequestTask(private val httpRequest: HttpRequest) {
        fun run() {
            try {
                val connection = getHttpUrlConnection()
                val parsedResponse = parseResponse(connection)
                httpRequest.sendResponse(parsedResponse, null)
            } catch (e: IOException) {
                httpRequest.sendResponse(null, e)
            }
        }

        @Throws(IOException::class)
        private fun getHttpUrlConnection(): HttpURLConnection {
            val url = URL(httpRequest.url)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = httpRequest.httpMethod
            for ((key, value) in httpRequest.header) {
                connection.setRequestProperty(key, value)
            }
            if (httpRequest.body != null) {
                val outputStream = connection.outputStream
                outputStream.write(httpRequest.body)
            }

            connection.connect()

            return connection
        }

        @Throws(IOException::class)
        private fun parseResponse(connection: HttpURLConnection): Response {
            try {
                val byteOutputStream = ByteArrayOutputStream()
                val status = connection.responseCode
                val validStatus = status in 200..299
                val inputStream =
                    if (validStatus) connection.inputStream else connection.errorStream
                var read: Int
                var totalRead = 0
                val buf = ByteArray(1024 * 4)
                while (inputStream.read(buf).also { read = it } != -1) {
                    byteOutputStream.write(buf, 0, read)
                    totalRead += read
                }
                return Response(byteOutputStream.toByteArray())
            } finally {
                connection.disconnect()
            }
        }
    }

    class Response(private val data: ByteArray) {
        @Throws(JSONException::class)
        fun asJSONObject(): JSONObject {
            val str = String(data, StandardCharsets.UTF_8)
            return if (str.isEmpty()) JSONObject()
            else JSONObject(str)
        }
    }
}