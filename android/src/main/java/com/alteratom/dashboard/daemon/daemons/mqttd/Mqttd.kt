package com.alteratom.dashboard.daemon.daemons.mqttd

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.helper_objects.Debug
import com.alteratom.dashboard.manager.StatusManager
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder
import io.netty.handler.ssl.util.SimpleTrustManagerFactory
import io.netty.util.internal.EmptyArrays
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.ManagerFactoryParameters
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    private var client: Mqtt5AsyncClient? = null

    //Current config (assigned after successful connection)
    private var currentConfig = MqttConfig()

    //List of subscribed topics
    private var topics: MutableList<Pair<String, Int>> = mutableListOf()

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    private val isConnected
        get() = client?.config?.state?.isConnected ?: false

    //Ping send on state change
    override val statePing: MutableLiveData<String?> = MutableLiveData(null)

    //Current state
    override val state: State
        get() = if (manager.isWorking) State.ATTEMPTING
        else try {
            if (!isConnected) State.DISCONNECTED
            else if (currentConfig.sslRequired && !currentConfig.sslTrustAll) State.CONNECTED_SSL
            else State.CONNECTED
        } catch (e: Exception) {
            Debug.recordException(e)
            State.FAILED
        }

    //Daemon notify response methods -------------------------------------------------------------

    override fun notifyAssigned() {
        super.notifyAssigned()
        manager.dispatch(reason = "assigned")
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (isConnected) manager.dispatch(reason = "discharged")
    }

    override fun notifyCheck() {
        super.notifyCheck()
        if (!manager.isStable()) manager.dispatch(reason = "unstable")
    }

    override fun notifyConfigChanged() {
        super.notifyConfigChanged()
        if (isConnected && isEnabled && currentConfig == d.mqtt) topicCheck()
        else {
            manager.dispatch(reason = "config")
            statePing.postValue(null)
        }
    }

    // Status manager ------------------------------------------------------------------------------

    private val manager = Manager()

    inner class Manager : StatusManager(debug = true) {
        override fun isStable(): Boolean {
            return isConnected == isEnabled && (currentConfig == d.mqtt || !isEnabled)
        }

        override fun makeStable() {
            if (isEnabled) {
                if (isConnected) {
                    Debug.log("MQTT_DISCONNECT")
                    client?.disconnect()
                } else {
                    if (currentConfig != d.mqtt || client == null) {
                        Debug.log("MQTT_BUILD_NEW_CLIENT")
                        buildClient(d.mqtt.copy())
                    }
                    Debug.log("MQTT_CONNECT")
                    client?.connect()
                }
            } else {
                Debug.log("MQTT_CLOSE")
                client?.disconnect()
            }
        }

        override fun onJobDone() = statePing.postValue("")
        override fun onJobStart() = statePing.postValue(null)
        override fun onException(e: Exception) {
            super.onException(e)
            when (e) {
                is IllegalArgumentException -> statePing.postValue(e.message)
            }
        }
    }

    // Connection methods -------------------------------------------------------------------------

    fun buildClient(config: MqttConfig) {
        var client = Mqtt5Client.builder()
            .identifier(config.clientId)
            .serverHost(config.address)
            .serverPort(config.port)
            .addConnectedListener {
                Debug.log("MQTT_ON_CONNECTED")
                topicCheck()
                statePing.postValue("")
            }
            .addDisconnectedListener {
                Debug.log("MQTT_ON_DISCONNECTED")
                topics = mutableListOf()
                manager.dispatch(reason = "connection")

                if (it.cause !is ConnectionFailedException) statePing.postValue("")
                else statePing.postValue(it.cause.cause?.message)
            }

        //Include credentials if required
        if (config.includeCred) client = client.simpleAuth()
            .username(config.username)
            .password(config.pass.toByteArray())
            .applySimpleAuth()

        //Setup WebSocket if required
        if (config.protocol == Protocol.WS || config.protocol == Protocol.WSS) {
            client = client
                .webSocketConfig()
                .queryString(config.queryString)
                .serverPath(config.serverPath)
                .applyWebSocketConfig()
        }

        //Setup SSL if required
        if (config.sslRequired) client = client.setupSSL(config)

        //Build client and update current config
        this.client = client.buildAsync()
        currentConfig = config
    }

    private fun Mqtt5ClientBuilder.setupSSL(config: MqttConfig): Mqtt5ClientBuilder {
        //---------------------------------------------------------------------------
        val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val kmfKeyPassword = UUID.randomUUID().toString().toCharArray()
        kmfStore.load(null, null)
        kmfStore.setCertificateEntry("cc", config.clientCert)
        config.clientKey?.let {
            kmfStore.setKeyEntry(
                "k",
                it.private,
                kmfKeyPassword,
                arrayOf<Certificate?>(config.clientCert)
            )
        }
        //---------------------------------------------------------------------------
        var tmfStore: KeyStore? = null
        if (config.caCert != null) {
            tmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
            tmfStore.load(null, null)
            tmfStore.setCertificateEntry("c", config.caCert)
        }

        //Decides which authentication credentials should be sent to the remote host
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(kmfStore, kmfKeyPassword)

        val alg = TrustManagerFactory.getDefaultAlgorithm()

        //Determines whether remote connection should be trusted or not
        val tmf = if (!config.sslTrustAll) TrustManagerFactory.getInstance(alg)
        else TrustAllTrustManagerFactory()

        if (!config.sslTrustAll) tmf.init(tmfStore)

        return this.sslConfig()
            .keyManagerFactory(kmf)
            .trustManagerFactory(tmf)
            .applySslConfig()
    }

    // MQTT methods ------------------------------------------------------------------------------

    fun publish(topic: String, msg: String, qos: Int = 0, retain: Boolean = false) {
        if (!isConnected) return

        client
            ?.publishWith()
            ?.topic(topic)
            ?.payload(msg.toByteArray())
            ?.qos(MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE)
            ?.retain(retain)
            ?.send()
    }

    private fun subscribe(topic: String, qos: Int) {
        if (!isConnected) return

        client?.subscribeWith()
            ?.topicFilter(topic)
            ?.qos(MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE)
            ?.callback {
                for (tile in d.tiles) tile.receive(topic, String(it.payloadAsBytes))
            }
            ?.send()
            ?.thenAccept {
                topics.add(Pair(topic, qos))
            }
    }

    private fun unsubscribe(topic: String, qos: Int) {
        if (!isConnected) return

        client?.unsubscribeWith()
            ?.topicFilter(topic)
            ?.send()
            ?.thenAccept {
                topics.remove(Pair(topic, qos))
            }
    }

    //Manage subscriptions at topic list change
    private fun topicCheck() {
        val list: MutableList<Pair<String, Int>> = mutableListOf()
        for (tile in d.tiles.filter { it.mqtt.isEnabled }) {
            for (t in tile.mqtt.subs) {
                Pair(t.value, tile.mqtt.qos).let {
                    if (!list.contains(it) && t.value.isNotBlank()) {
                        list.add(it)
                    }
                }
            }
        }

        val unsTopics = topics - list.toSet()
        val subTopics = list - topics.toSet()

        try {
            for (t in unsTopics) unsubscribe(t.first, t.second)
            for (t in subTopics) subscribe(t.first, t.second)
        } catch (_: Exception) {
        }
    }

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
    enum class Protocol { TCP, SSL, WS, WSS }

    @SuppressLint("CustomX509TrustManager")
    class TrustAllTrustManagerFactory : SimpleTrustManagerFactory() {
        override fun engineInit(keyStore: KeyStore) {}
        override fun engineInit(managerFactoryParameters: ManagerFactoryParameters) {}
        override fun engineGetTrustManagers(): Array<TrustManager> = arrayOf(tm)

        private val tm: TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, s: String) {
                chain[0].subjectDN
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, s: String) {
                chain[0].subjectDN
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return EmptyArrays.EMPTY_X509_CERTIFICATES
            }
        }
    }
}
