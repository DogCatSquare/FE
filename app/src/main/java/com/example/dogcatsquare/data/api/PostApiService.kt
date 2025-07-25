import com.example.dogcatsquare.data.model.community.BoardPost
import com.example.dogcatsquare.data.model.community.GetAllPostResponse
import com.example.dogcatsquare.data.model.community.LikeResponse
import com.example.dogcatsquare.data.model.community.PostDetail
import com.example.dogcatsquare.data.model.community.PostResponse
import com.example.dogcatsquare.data.model.post.PopularPostResponse
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
    ): Call<com.example.dogcatsquare.data.model.community.PostResponse>

    @GET("api/board/post/{postId}")
    fun getPost(@Header("Authorization") token: String, @Path("postId") postId: Int): Call<com.example.dogcatsquare.data.model.community.PostDetail>

    @GET("api/board/posts/popular")
    fun getPopularPost(@Header("Authorization") token: String): Call<PopularPostResponse>

    @GET("api/board/{boardId}/posts")
    fun getBoardPost(@Header("Authorization") token: String, @Path("boardId") boardId: Int): Call<com.example.dogcatsquare.data.model.community.BoardPost>

    @POST("api/board/post/{postId}/like")
    fun fetchLike(@Header("Authorization") token: String, @Path("postId") postId: Int, @Query("userId") userId: Int): Call<com.example.dogcatsquare.data.model.community.LikeResponse>

    @GET("api/board/posts/all")
    fun getAllPosts(@Header("Authorization") token: String): Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>
}
