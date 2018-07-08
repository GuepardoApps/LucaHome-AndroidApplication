package guepardoapps.lucahome.common.services.change

import android.annotation.SuppressLint
import android.content.Context
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.converter.change.JsonDataToChangeConverter
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.models.change.Change
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.utils.Logger
import java.util.*
import kotlin.collections.ArrayList

class ChangeService private constructor() : IChangeService {
    private val tag = ChangeService::class.java.simpleName

    private val loadTimeoutMs: Int = 2 * 60 * 1000

    private var converter: JsonDataToChangeConverter = JsonDataToChangeConverter()

    private var downloadAdapter: DownloadAdapter? = null

    private var changeList: ArrayList<Change> = arrayListOf()
    private var lastUpdate: Calendar = Calendar.getInstance()

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: ChangeService = ChangeService()
    }

    companion object {
        val instance: ChangeService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null
    override var context: Context? = null
    override var onChangeService: OnChangeService? = null

    @Deprecated("Do not use serviceSettings in ChangeService")
    override var serviceSettings: ServiceSettings = ServiceSettings(-1, false, 0, false)
    @Deprecated("Do not use receiverActivity in ChangeService")
    override var receiverActivity: Class<*>? = null

    override fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context
        this.downloadAdapter = DownloadAdapter(this.context!!)
    }

    override fun dispose() {
        this.context = null
    }

    @Throws(NotImplementedError::class)
    override fun get(): MutableList<Change> {
        throw NotImplementedError("Get for change not available")
    }

    override fun get(type: String): Change? {
        return changeList.find { value -> value.type == type }
    }

    override fun get(uuid: UUID): Change? {
        return changeList.find { value -> value.uuid == uuid }
    }

    @Throws(NotImplementedError::class)
    override fun search(searchValue: String): MutableList<Change> {
        throw NotImplementedError("Search for change not available")
    }

    override fun load() {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        if (changeList.isNotEmpty() && Calendar.getInstance().timeInMillis - lastUpdate.timeInMillis <= loadTimeoutMs) {
            onChangeService!!.loadFinished(true, "")
            return
        }

        this.downloadAdapter?.send(
                ServerAction.ChangeGet.command,
                ServerAction.ChangeGet,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.ChangeGet) {
                            val successGet = state == DownloadState.Success
                            if (!successGet) {
                                onChangeService!!.loadFinished(false, "Loading failed!")
                                return
                            }

                            val loadedList = converter.parse(message)
                            if (!loadedList.isEmpty()) {
                                changeList = loadedList
                            }
                            onChangeService!!.loadFinished(true, "")
                        }
                    }
                }
        )
    }

    @Throws(NotImplementedError::class)
    override fun add(entry: Change, reload: Boolean) {
        throw NotImplementedError("Add for change not available")
    }

    @Throws(NotImplementedError::class)
    override fun update(entry: Change, reload: Boolean) {
        throw NotImplementedError("Update for change not available")
    }

    @Throws(NotImplementedError::class)
    override fun delete(entry: Change, reload: Boolean) {
        throw NotImplementedError("Delete for change not available")
    }
}