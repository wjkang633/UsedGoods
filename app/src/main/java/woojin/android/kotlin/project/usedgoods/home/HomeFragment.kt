package woojin.android.kotlin.project.usedgoods.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.CHILD_CHAT
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.DB_ARTICLES
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.DB_USERS
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.chatlist.ChatListItem
import woojin.android.kotlin.project.usedgoods.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference

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

        userDB = Firebase.database.reference.child(DB_USERS)

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleDB.addChildEventListener(listener)

        //????????? ?????????
        articleList.clear()

        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->
            if (auth.currentUser != null) {
                //????????? ??? ??????
                if (auth.currentUser!!.uid != articleModel.sellerId) {
                    //?????? ?????? ???????????? ?????? ???
                    //????????? ?????????
                    val chatRoom = ChatListItem(
                            buyerId = auth.currentUser!!.uid,
                            sellerId = articleModel.sellerId,
                            itemTitle = articleModel.title,
                            key = articleModel.createdAt
                    )

                    userDB.child(auth.currentUser!!.uid)
                            .child(CHILD_CHAT)
                            .push()
                            .setValue(chatRoom)

                    userDB.child(articleModel.sellerId)
                            .child(CHILD_CHAT)
                            .push()
                            .setValue(chatRoom)

                    Snackbar.make(view, "???????????? ?????????????????????. ??????????????? ??????????????????.", Snackbar.LENGTH_SHORT).show()

                } else {
                    //?????? ?????? ???????????? ???
                    Snackbar.make(view, "?????? ?????? ??????????????????.", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                //????????? ?????? ??????
                Snackbar.make(view, "????????? ??? ??????????????????.", Snackbar.LENGTH_SHORT).show()
            }
        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter
        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            //????????? ????????? ??? ?????????
            if (auth.currentUser != null) {
                startActivity(Intent(requireActivity(), AddArticleActivity::class.java))
            } else {
                Snackbar.make(view, "????????? ??? ??????????????????.", Snackbar.LENGTH_SHORT).show()
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