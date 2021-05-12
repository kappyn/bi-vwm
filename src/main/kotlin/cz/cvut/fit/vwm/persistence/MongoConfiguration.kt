package cz.cvut.fit.vwm.persistence

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import io.ktor.config.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoConfiguration {

    private lateinit var name: String
    private lateinit var password: String
    private var srv: String? = null
    private var host: String? = null
    private lateinit var db : String
    private lateinit var clientSettings: MongoClientSettings

    fun setConfig(config: ApplicationConfig) {
        name = config.propertyOrNull("name")?.getString() ?: ""
        password = config.propertyOrNull("password")?.getString() ?: ""
        srv = config.propertyOrNull("srv")?.getString()
        host = config.propertyOrNull("host")?.getString()
        db = config.propertyOrNull("db")?.getString() ?: ""

        clientSettings = MongoClientSettings.builder()
            .credential(MongoCredential.createScramSha1Credential(name, "admin", password.toCharArray()))
            .applyToClusterSettings {
                if (host != null) {
                    it.hosts(mutableListOf(ServerAddress(host)))
                }
                if (srv != null) {
                    it.srvHost(srv)
                }
            }
            .build()
    }

    fun newClient(): CoroutineClient {
        return KMongo.createClient(clientSettings).coroutine
    }

    fun getDatabase(name: String = db): CoroutineDatabase {
        return newClient().getDatabase(name)
    }
}
