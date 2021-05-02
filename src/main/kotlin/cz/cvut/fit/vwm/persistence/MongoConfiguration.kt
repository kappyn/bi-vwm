package cz.cvut.fit.vwm.persistence

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoConfiguration {
    private val clientSettings = MongoClientSettings.builder().credential(MongoCredential.createScramSha1Credential("vwm", "admin", "vwm".toCharArray())).build()

    fun newClient(): CoroutineClient {
        return KMongo.createClient(clientSettings).coroutine
    }
}
