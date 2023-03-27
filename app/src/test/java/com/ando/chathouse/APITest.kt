package com.ando.chathouse

class APITest {
//    private val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(OPENAI_URL)
//            .addConverterFactory(GsonConverterFactory.create(NetworkModule.getGson()))
//            .client(NetworkModule.provideHttpClient())
//            .build()
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testQuery() = runTest{
//        val api = retrofit.create(OpenAIApi::class.java)
//        val response = api.query(
//            authorization = Authorization(DEFAULT_API_KEY),
//            para = TextCompletionPara(prompt = "hello")
//        )
//        assert(response.choices[0].text.isNotBlank())
//    }
}