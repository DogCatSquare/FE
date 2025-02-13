import com.example.dogcatsquare.data.community.PostResponse
import com.example.dogcatsquare.data.post.PopularPostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface PostApiService {
    @Multipart
    @POST("/api/board/post/users/{userId}")  // ✅ 올바른 엔드포인트로 변경
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,  // ✅ userId 추가
        @Part("boardId") boardId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("videoUrl") videoUrl: RequestBody,
        @Part images: List<MultipartBody.Part> // ✅ 수정됨: 여러 이미지 업로드 지원
    ): Call<PostResponse>

    @GET("api/board/posts/popular")
    fun getPopularPost(@Header("Authorization") token: String): Call<PopularPostResponse>
}
