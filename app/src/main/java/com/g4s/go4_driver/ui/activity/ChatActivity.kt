package com.g4s.go4_driver.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.g4s.go4_driver.R
import com.g4s.go4_driver.adapter.ChatAdapter
import com.g4s.go4_driver.databinding.ActivityChatBinding
import com.g4s.go4_driver.model.ChatModel
import com.g4s.go4_driver.model.OrderLogModel
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    companion object {
        internal val TAG = ChatActivity::class.java.simpleName
        //image pick code
        private const val IMAGE_PICK_CODE = 1000
    }
    private var customerId: String? = null
    private var mUser: String? = null
    private lateinit var reference: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: List<ChatModel>
    var order: OrderLogModel? = null
    private var notify = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.lifecycleOwner = this
        val gson = Gson()
        order = gson.fromJson(intent.getStringExtra("order"), OrderLogModel::class.java)
        customerId = order!!.customerId
        mUser = order!!.driverId
        binding.appBar.titleTextView.text = order!!.driver!!.nama!!.toString().toUpperCase()
        binding.appBar.backButton.setOnClickListener {
            onBackPressed()
        }
        with(binding.rvChat) {
            setHasFixedSize(true)
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            linearLayoutManager.stackFromEnd = true
            layoutManager = linearLayoutManager
        }
        retrieveMessage(mUser, customerId.toString())
        img_send_message.setOnClickListener {
            val message = edt_text_message.text.toString()
            notify = true
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(
                    this,
                    "Masukkan Pesan ...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser(mUser, customerId, message)
            }
            edt_text_message.setText("")
        }
        img_attach_image_file.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), IMAGE_PICK_CODE)
        }
        seenMessage(customerId!!)
    }
    private fun retrieveMessage(senderId: String?, receiverId: String) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (chatList as ArrayList<ChatModel>).clear()
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(ChatModel::class.java)
                    if (chat?.receiver.equals(senderId) && chat?.sender.equals(receiverId)
                        || chat?.receiver.equals(receiverId) && chat?.sender.equals(senderId)
                    ) {
                        (chatList as ArrayList<ChatModel>).add(chat!!)
                    }
                    chatAdapter = ChatAdapter(order!!)
                    chatAdapter.setData(chatList as ArrayList<ChatModel>)
                    binding.rvChat.adapter = chatAdapter
                }
            }

        })
    }
    private fun sendMessageToUser(
        senderId: String?,
        receiveId: String?,
        message: String
    ) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiveId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListReference =
                        FirebaseDatabase.getInstance().reference.child("ChatList")
                            .child(senderId.toString())
                            .child(customerId!!)

                    chatListReference.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.d(TAG, p0.message)
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatListReference.child("id").setValue(customerId)
                            }

                            val chatListReceiverReference =
                                FirebaseDatabase.getInstance().reference.child("ChatList")
                                    .child(customerId!!)
                                    .child(senderId.toString())
                            chatListReceiverReference.child("id").setValue(senderId)
                        }

                    })

                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Please wait, image is sending ...")
            loadingBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")
            filePath.putFile(fileUri!!).addOnSuccessListener { it ->
//                Log.d(RegisterActivity.TAG, "Successfully uploaded image : ${it.metadata?.path}")
                filePath.downloadUrl.addOnSuccessListener {
                    val url = it.toString()
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = mUser
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = customerId
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                loadingBar.dismiss()
//                                Implement the push notification
                                val reference =
                                    FirebaseDatabase.getInstance().reference.child("users")
                                        .child(mUser.toString())

                                reference.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        Log.d(TAG, p0.message)
                                    }

                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val user = dataSnapshot.getValue(User::class.java)
                                        if (notify) {

                                        }
                                        notify = false
                                    }

                                })
                            }
                        }
                }
            }

        }
    }
    private var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, p0.message)
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                for (snapshot in datasnapshot.children) {
                    val chat = snapshot.getValue(ChatModel::class.java)
                    if (chat?.receiver.equals(mUser) && chat?.sender.equals(userId)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        snapshot.ref.updateChildren(hashMap)
                    }
                }

            }

        })
    }
    override fun onPause() {
        super.onPause()
        if (::reference.isInitialized) {
            reference.removeEventListener(seenListener!!)
        }
    }
}