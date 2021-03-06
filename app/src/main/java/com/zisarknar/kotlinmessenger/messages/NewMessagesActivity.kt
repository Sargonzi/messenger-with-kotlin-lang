package com.zisarknar.kotlinmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.zisarknar.kotlinmessenger.R
import com.zisarknar.kotlinmessenger.registerLogin.User
import kotlinx.android.synthetic.main.activity_new_messages.*
import kotlinx.android.synthetic.main.item_view_new_user.view.*

class NewMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messages)

        supportActionBar?.title = "Select user"

        fetchUsers()
    }

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val adapter = GroupAdapter<ViewHolder>()

                    p0.children.forEach {
                        Log.d("New", it.toString())
                        val user = it.getValue(User::class.java)
                        if (user != null ) {
                            adapter.add(UserItem(user))
                        }

                        adapter.setOnItemClickListener { item, view ->
                            val userItem = item as UserItem
                            val intent = Intent(view.context, ChatLogActivity::class.java)
                            intent.putExtra(USER_KEY, userItem.user)
                            startActivity(intent)
                            finish()
                        }
                        rv_contents.adapter = adapter
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }
}


class UserItem(val user: User): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.cycImg)
    }

    override fun getLayout(): Int {
        return R.layout.item_view_new_user
    }

}
