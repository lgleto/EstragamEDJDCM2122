package ipca.example.estragame.ui

import com.google.firebase.Timestamp
import kotlin.collections.HashMap

class Post {

    var description : String? = null
    var photo : String? = null
    var date : Timestamp? = null
    var user : String? = null


    fun toHashMap() : HashMap<String, Comparable<Any>?> {
        val post = hashMapOf(
            "description" to description,
            "photo"       to photo,
            "date"        to date,
            "user"        to user
        )

        return post as HashMap<String, Comparable<Any>?>
    }

    companion object {
        fun fromHashMap(hash: Map<String, Any>) : Post {
            var post = Post()

            post.description = hash["description"] as String?
            post.photo = hash["photo"] as String?
            post.date = hash["date"] as Timestamp?
            post.user = hash["user"] as String?

            return post
        }
    }

}