package com.jfelt.koddit

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*


class KodditAuth {

    companion object {
        val TEMPORARY: String = "temporary"
        val PERMANENT: String = "permanent"

        fun generateState(customSeedLength: Int?): String? {
            var stateLength: Int? = 20
            val seed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"
            val rand = Random()
            val sb = StringBuilder()
            if (customSeedLength == null) {
                stateLength = customSeedLength
            }

            for (i in 0..stateLength!!) {
                sb.append(seed[rand.nextInt()])
            }

            return sb.toString()
        }

        fun getAuthUrl(clientId: String, state: String, redirectUri: String,
                       duration: String, scopes: String, bShouldUseCompact: Boolean): String {
            var baseUrl: String?
            if (bShouldUseCompact) {
                baseUrl = "https://www.reddit.com/api/v1/authorize.compact?"
            } else {
                baseUrl = "https://www.reddit.com/api/v1/authorize?"
            }

            baseUrl += "client_id=$clientId&response_type=code&state=$state&redirect_uri=$redirectUri&duration$duration&scope=$scopes"
            return baseUrl
        }

        fun sendPostData(code: String, redirectUrl: String) {
            val postBody = "grant_type=authorization_code&code=$code&redirect_uri=$redirectUrl"
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://www.reddit.com")
                    .addConverterFactory(GsonConverterFactory.create()).build()

            val authInterface: KodditAuthInterface = retrofit.create(KodditAuthInterface::class.java)
        }
    }


    interface KodditAuthInterface {
        @FormUrlEncoded()
        @POST("api/v1/access_token")
        fun getAccessToken(@Field("body") body: String, @Header("Authorization") authorization: String): Call<String>
    }
}