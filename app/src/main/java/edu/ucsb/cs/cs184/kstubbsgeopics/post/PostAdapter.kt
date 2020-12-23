package edu.ucsb.cs.cs184.kstubbsgeopics.post

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import edu.ucsb.cs.cs184.kstubbsgeopics.User


class PostAdapter(private var context: Context?, private var posts: ArrayList<Post>,
                  private var users: ArrayList<User>,
                  private var distances: ArrayList<Double>?) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    lateinit var firebaseUser: FirebaseUser
    var index = 0

    var pics = arrayOf(R.drawable._573537538000, R.drawable._573537538001,
        R.drawable._573537538002, R.drawable._573537538003,R.drawable._573537538004,
        R.drawable._573537538005, R.drawable._573537538006,R.drawable._573537538007,
        R.drawable._573537538008, R.drawable._573537538009)
    //could not figure out how to get arrays.xml for the life of me

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        var image: ImageView
        var username: TextView
        var caption: TextView
        var heartImage: ImageView
        var displayLikes: TextView
        var longitude: TextView
        var latitude: TextView
        var distance: TextView

        init {
            v.setOnClickListener(this)
            image = v.findViewById(R.id.image_box)
            username = v.findViewById(R.id.username_box)
            caption = v.findViewById(R.id.caption_box)
            heartImage = v.findViewById(R.id.like_button)
            displayLikes = v.findViewById(R.id.display_likes)
            longitude = v.findViewById(R.id.longitude_box)
            latitude = v.findViewById(R.id.latitude_box)
            distance = v.findViewById(R.id.distance_box)
        }

        override fun onClick(v: View?) {
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_post, parent, false)
        return PostAdapter.ViewHolder(view)
    }

    lateinit var database: FirebaseDatabase

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        index = position
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance()
        var tempUID = posts[position].userUID

        var username = ""
        for (user in users) {
            if (tempUID == user.UID) {
                username = user.Username
                break
            }
        }
        if (posts[position].userUID == firebaseUser.uid) {
            holder.username.text = username + " (You)"
            holder.username.setTextColor(Color.BLUE)
        } else {
            holder.username.text = username
            holder.username.setTextColor(Color.BLACK)
        }
        if (tempUID == firebaseUser.uid) {
            holder.image.setImageDrawable(Drawable.createFromPath(posts[position].photoPath))
        } else {
            var tempIndex = index % 10
            holder.image.setImageResource(pics[tempIndex])
            index++
        }

        var ref = FirebaseDatabase.getInstance().getReference()
            .child("Likes")
            .child(posts[position].postUID)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser.uid).exists()) {
                    holder.heartImage.setImageResource(R.drawable.ic_like)
                    holder.heartImage.contentDescription = "filled"
                } else {
                    holder.heartImage.setImageResource(R.drawable.ic_like_hollow)
                    holder.heartImage.contentDescription = "empty"
                }
                holder.displayLikes.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        holder.heartImage.setOnClickListener {
            if (holder.heartImage.contentDescription == "empty") {
                ref.child(firebaseUser.uid).setValue("exists")
            } else {
                ref.child(firebaseUser.uid).removeValue()
            }
        }

        holder.caption.text = posts[position].caption

        holder.latitude.text = "Lat: " + posts[position].latitude

        holder.longitude.text = "Long: " + posts[position].longitude

        holder.distance.text = "Distance from center: " + 60 * distances!![position] + " miles"
        //distance in miles is 1 unit times 60
    }

    override fun getItemCount(): Int {
        return posts.size
    }

}