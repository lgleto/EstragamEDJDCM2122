package ipca.example.estragame.ui

import com.google.firebase.Timestamp
import kotlin.collections.HashMap

class User {

    var name : String? = null
    var photo : String? = null
    var date : Timestamp? = null
    var online : Boolean? = null

    fun toHashMap() : HashMap<String, Comparable<Any>?> {
        val post = hashMapOf(
            "name"    to name,
            "photo"   to photo,
            "date"    to date,
            "online"  to online
        )

        return post as HashMap<String, Comparable<Any>?>
    }

    companion object {
        fun fromHashMap(hash: Map<String, Any>) : User {
            var post = User()

            post.name = hash["name"] as String?
            post.photo = hash["photo"] as String?
            post.date = hash["date"] as Timestamp?
            post.online = hash["online"] as Boolean?

            return post
        }
    }

}