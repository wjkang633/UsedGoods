package woojin.android.kotlin.project.usedgoods.chatdetail

data class ChatItem(
    val senderId: String,
    val message: String
) {
    constructor() : this("", "")
}
