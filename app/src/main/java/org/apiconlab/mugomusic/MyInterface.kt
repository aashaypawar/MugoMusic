package org.apiconlab.mugomusic

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MyInterface {
    @GET("search?")
    fun getMyData(@Query("song") songName: String): Call<List<SearchDataItem>>
}