package network.xyo.client.witness.system.info

import android.content.Context
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class XyoSystemInfoNetwork (
    val cellular: XyoSystemInfoNetworkCellular?,
    val wifi: XyoSystemInfoNetworkWifi?,
    val wired: XyoSystemInfoNetworkWired?
) {
    companion object {
        fun detect(context: Context): XyoSystemInfoNetwork? {
            val result = XyoSystemInfoNetwork(
                XyoSystemInfoNetworkCellular.detect(context),
                XyoSystemInfoNetworkWifi.detect(context),
                XyoSystemInfoNetworkWired.detect(context)
            )
            if (result.cellular != null || result.wifi != null || result.wired != null) {
                return result
            }
            return null
        }
    }
}