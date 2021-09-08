package woojin.android.kotlin.project.usedgoods.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.CHILD_CHAT
import woojin.android.kotlin.project.usedgoods.DBKey.Companion.DB_USERS
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.chatdetail.ChatRoomActivity
import woojin.android.kotlin.project.usedgoods.databinding.FragmentChatListBinding

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {

    private var binding: FragmentChatListBinding? = null

    private lateinit var chatListAdapter: ChatListAdapter

    private val chatRoomList = mutableListOf<ChatListItem>()
    private val auth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChatListBinding = FragmentChatListBinding.bind(view)
        binding = fragmentChatListBinding

        chatRoomList.clear()

        chatListAdapter = ChatListAdapter(onItemClicked = { chatRoom ->
            //채팅방으로 이동
            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatKey", chatRoom.key)
            startActivity(intent)
        })

        fragmentChatListBinding.chatListRecyclerView.adapter = chatListAdapter
        fragmentChatListBinding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)


        if (auth.currentUser == null) {
            return
        }

        //userDB에서 채팅방 가져오기
        val chatDB = Firebase.database.reference
            .child(DB_USERS)
            .child(auth.currentUser!!.uid)
            .child(CHILD_CHAT)

        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return

                    chatRoomList.add(model)
                }

                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()

        chatListAdapter.notifyDataSetChanged()
    }
}