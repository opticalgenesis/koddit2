package com.jfelt.koddit

import android.util.Log
import com.jfelt.koddit.objects.AuthenticationObject
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*


class KodditAuth {

    companion object {
        val TEMPORARY: String = "temporary"
        val PERMANENT: String = "permanent"

        fun printVersionString(): String {
            return "0.1.4 -- moved to jitpack"
        }

        fun generateState(customSeedLength: Int = 0): String? {
            var stateLength: Int? = 20
            val seed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"
            val rand = Random()
            val sb = StringBuilder()
            if (customSeedLength > 0) {
                stateLength = customSeedLength
            }

            for (i in 0..stateLength!!) {
                sb.append(seed[rand.nextInt(seed.length) + 0])
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

        fun sendPostData(code: String, redirectUrl: String, clientId: String): AuthenticationObject? {
            var obj: AuthenticationObject? = null

            val postBody = "grant_type=authorization_code&code=$code&redirect_uri=$redirectUrl"

            val okhttpClient = OkHttpClient().newBuilder().addInterceptor({ chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder().header(
                        "Authorization", Credentials.basic(clientId, ""))
                chain.proceed(builder.build())
            }).build()

            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://www.reddit.com")
                    .client(okhttpClient)
                    .addConverterFactory(GsonConverterFactory.create()).build()

            val authInterface: KodditAuthInterface = retrofit.create(KodditAuthInterface::class.java)
            val authObject: Call<AuthenticationObject> = authInterface.getAccessToken(postBody)
            authObject.enqueue(object : Callback<AuthenticationObject> {
                override fun onResponse(call: Call<AuthenticationObject>, response: Response<AuthenticationObject>) {
                    if (response.isSuccessful) {
                        Log.d("KODDIT_RAW_RESPONSE", "Raw response: ${response.body()}")
                        Log.d("KODDIT_RESPONSE_TAG", response.code().toString())
                    } else {
                        Log.e("KODDIT_ERROR", "POST failed with code ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<AuthenticationObject>?, t: Throwable?) {
                    Log.e("KODDIT_TAG", "Error thrown by Retrofit")
                    t?.printStackTrace()
                }
            })
            return obj
        }
    }


    interface KodditAuthInterface {
        @FormUrlEncoded()
        @POST("api/v1/access_token")
        fun getAccessToken(@Field("body") body: String): Call<AuthenticationObject>
    }
}