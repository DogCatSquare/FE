import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityBoardDetailBinding
import com.example.dogcatsquare.ui.community.CreatePostActivity

class BoardDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boardId = intent.getIntExtra("boardId", -1)  // 현재 게시판 ID 가져오기
        binding.tvBoardTitle.text = "게시판 ID: $boardId"  // 예제 코드
    }
}
