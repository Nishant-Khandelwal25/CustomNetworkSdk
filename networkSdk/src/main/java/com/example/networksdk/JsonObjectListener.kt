package com.example.networksdk

import org.json.JSONObject
import java.lang.Exception

interface JsonObjectListener {
    fun onResponse(response: JSONObject?)
    fun onFailure(exception: Exception?)
}