package woojin.android.kotlin.project.usedgoods.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.DB_ARTICLES
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val articleList = mutableListOf<ArticleModel>()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleDB.addChildEventListener(listener)

        //리스트 초기화
        articleList.clear()

        articleAdapter = ArticleAdapter()

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter
        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            //회원만 이동할 수 있도록
            if (auth.currentUser != null) {
                startActivity(Intent(requireActivity(), AddArticleActivity::class.java))
            } else {
                Snackbar.make(
                    view,
                    "로그인 후 사용해주세요.",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        articleDB.removeEventListener(listener)
    }
}