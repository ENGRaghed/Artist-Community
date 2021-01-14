package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.Chat
import com.safcsp.android.artistcommunity.data.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatFragment : Fragment() {
    private val args by navArgs<ChatFragmentArgs>()

    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var chatList = ArrayList<Chat>()
    private lateinit var user: User
  //  private lateinit var userId: String

    private lateinit var profileImage: CircleImageView
    private lateinit var sendBtn: ImageButton
    private lateinit var messageEd: EditText
    private lateinit var userNameTv: TextView
    private lateinit var MessageRecyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user= User()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(args.userId)
        firebaseUser?.uid?.let { readMessage(it, args.userId) }
        adapter= ChatAdapter(chatList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=  inflater.inflate(R.layout.fragment_chat, container, false)
        sendBtn= view.findViewById(R.id.btnSendMessage)
        messageEd= view.findViewById(R.id.etMessage)
        userNameTv= view.findViewById(R.id.tvUserName)
        profileImage= view.findViewById(R.id.imgProfile)
        MessageRecyclerView= view.findViewById(R.id.chatRecyclerView)
        MessageRecyclerView.layoutManager= LinearLayoutManager(context)
        MessageRecyclerView.adapter= adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)
                tvUserName.text = user!!.name
                if (user.profileImage == "") {
                  profileImage.setImageResource(R.drawable.ic_baseline_account3_circle_24)
                } else {
                    Glide.with(this@ChatFragment).load(user.profileImage).into(profileImage)
                }
            }
        })

        FirebaseAuth.getInstance().currentUser?.uid?.let { readMessage(it,args.userId) }
        sendBtn.setOnClickListener {
            var message: String = messageEd.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "message is empty", Toast.LENGTH_SHORT).show()
                messageEd.setText("")
            } else {
                firebaseUser?.uid?.let { it1 -> sendMessage(it1, args.userId, message) }
                messageEd.setText("")
            }
        }

    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference!!.child("Chat").push().setValue(hashMap).addOnSuccessListener {
            Toast.makeText(requireContext(), "add chat success", Toast.LENGTH_SHORT).show()
            Log.i("CHAT","add chat success")
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
            Log.i("CHAT","$it")
        }
    }

    fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                adapter.setData(chatList)
            }
        })
    }



    class ChatAdapter(var chatList: ArrayList<Chat>) :
        RecyclerView.Adapter<ViewHolder>() {
        private val MESSAGE_TYPE_LEFT = 0
        private val MESSAGE_TYPE_RIGHT = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if (viewType == MESSAGE_TYPE_RIGHT) {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
                return ViewHolder(view)
            } else {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
                return ViewHolder(view)
            }
        }

        override fun getItemCount() = chatList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chat = chatList[position]
            holder.bind(chat)
        }

        override fun getItemViewType(position: Int): Int {
           var firebaseUser = FirebaseAuth.getInstance().currentUser
            if (chatList[position].senderId == firebaseUser!!.uid) {
                return MESSAGE_TYPE_RIGHT
            } else {
                return MESSAGE_TYPE_LEFT
            }
        }

        fun setData( newChatList : ArrayList<Chat>){
            chatList = newChatList
            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        lateinit var chat: Chat
        private val messageTv: TextView = itemView.findViewById(R.id.tvMessage)
        private val image: CircleImageView = itemView.findViewById(R.id.userImage)

        fun bind(chat: Chat) {
            messageTv.text= chat.message
            var sender= FirebaseDatabase.getInstance().getReference("Users")
                .child("${chat.senderId}")

            sender.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user!!.profileImage == "") {
                        image.setImageResource(R.drawable.ic_baseline_account3_circle_24)
                    } else {
                        Glide.with(image).load(user.profileImage).into(image)
                    }
                }
            })
        }
    }
}