package com.zisarknar.kotlinmessenger.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.zisarknar.kotlinmessenger.R
import com.zisarknar.kotlinmessenger.registerLogin.User
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    private var toUser : User? = null
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        toUser = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        rv_chatlog.adapter = adapter

//        setupDummyData()
        listenForMessages()
        send_button.setOnClickListener {
            Log.d(TAG, "Message sending...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text, LatestMessagesActivity.currentUser!!))
                    }else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                rv_chatlog.scrollToPosition(adapter.getItemCount() -1)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }

    private fun performSendMessage() {
        val text = editText_chatlog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        val toId = user.uid
        if (fromId == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis())
        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our ref key ${ref.key}")
            editText_chatlog.text.clear()
            rv_chatlog.scrollToPosition(adapter.itemCount - 1)
        }
        toRef.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

}

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
    constructor() : this("", "", "", "", -1)
}

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txtChatFrom.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imgChatFrom)
    }

}


class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_chat_to)
    }

}
