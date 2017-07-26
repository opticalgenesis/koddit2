package com.jfelt.koddit.objects


data class AuthenticationObject (var access_token: String, var token_type: String,
                                var expires_in: Int, var scope: String, var refresh_token: String)