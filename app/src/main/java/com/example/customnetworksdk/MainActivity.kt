package com.example.customnetworksdk

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.example.networksdk.CustomNetwork
import com.example.networksdk.JsonObjectListener
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(key1 = true) {
                CustomNetwork.HttpRequest(CustomNetwork.GET)
                    .url("https://jsonplaceholder.typicode.com/posts/1")
                    .makeRequest(object : JsonObjectListener{
                        override fun onResponse(response: JSONObject?) {
                            Log.d("TAG", "onResponse: $response")
                        }

                        override fun onFailure(exception: Exception?) {
                            Log.d("TAG", "onFailure: $exception")
                        }

                    })

            }
        }
    }
}