package cz.cvut.fit.vwm.persistence

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.WriteConcern
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import io.ktor.config.*
import io.netty.channel.nio.NioEventLoopGroup
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.TimeUnit

object MongoConfiguration {

    private lateinit var name: String
    private lateinit var password: String
    private var srv: String? = null
    private var host: String? = null
    private lateinit var db: String
    private lateinit var clientSettings: MongoClientSettings
    private val eventLoopGroup = NioEventLoopGroup()
    private var env: ENVIRONMENT = ENVIRONMENT.LOCAL

    fun setConfig(config: ApplicationConfig) {
        name = config.propertyOrNull("name")?.getString() ?: ""
        password = config.propertyOrNull("password")?.getString() ?: ""
        srv = config.propertyOrNull("srv")?.getString()
        host = config.propertyOrNull("host")?.getString()
        db = config.propertyOrNull("db")?.getString() ?: ""
        if (srv != null) {
            env = ENVIRONMENT.PROD
        }

        clientSettings = MongoClientSettings.builder()
            .credential(MongoCredential.createScramSha1Credential(name, "admin", password.toCharArray()))
            .environmentTypeSettings()
            .build()
    }

    private fun MongoClientSettings.Builder.environmentTypeSettings(): MongoClientSettings.Builder {
        return when (env) {
            ENVIRONMENT.LOCAL -> this.applyToClusterSettings { it.hosts(mutableListOf(ServerAddress(host))) }
            ENVIRONMENT.PROD -> this.streamFactoryFactory(NettyStreamFactoryFactory.builder().eventLoopGroup(eventLoopGroup).build())
                .applyToSslSettings {
                    it.enabled(true)
                }.retryWrites(true)
                .writeConcern(WriteConcern.MAJORITY)
                .applyToConnectionPoolSettings {
                    it.maxConnectionIdleTime(600000, TimeUnit.MILLISECONDS)
                }
                .applyToClusterSettings {
                    it.srvHost(srv)
                }
        }
    }

    fun newClient(): CoroutineClient {
        return KMongo.createClient(clientSettings).coroutine
    }

    fun getDatabase(name: String = db): CoroutineDatabase {
        return newClient().getDatabase(name)
    }

    enum class ENVIRONMENT {
        LOCAL, PROD
    }
}
