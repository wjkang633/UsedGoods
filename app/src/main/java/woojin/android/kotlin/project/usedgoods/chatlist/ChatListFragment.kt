package woojin.android.kotlin.project.usedgoods.chatlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import woojin.android.kotlin.project.usedgoods.R
import woojin.android.kotlin.project.usedgoods.databinding.FragmentChatListBinding

class ChatListFragment:Fragment(R.layout.fragment_chat_list) {

    private var binding : FragmentChatListBinding? = null
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChatListBinding = FragmentChatListBinding.bind(view)
        binding = fragmentChatListBinding

        chatListAdapter = ChatListAdapter()

        fragmentChatListBinding.chatListRecyclerView.adapter = chatListAdapter
        fragmentChatListBinding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)
    }
}