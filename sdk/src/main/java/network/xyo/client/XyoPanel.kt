package network.xyo.client

import android.content.Context
import network.xyo.client.archivist.api.PostBoundWitnessesResult
import network.xyo.client.archivist.api.XyoArchivistApiClient
import network.xyo.client.archivist.api.XyoArchivistApiConfig

data class XyoPanelReportResult(val bw: XyoBoundWitnessJson, val apiResults: List<PostBoundWitnessesResult>)

class XyoPanel(val context: Context, val archivists: List<XyoArchivistApiClient>, val witnesses: List<XyoWitness<XyoPayload>>?) {
    constructor(
        context: Context,
        archive: String? = null,
        apiDomain: String? = null,
        witnesses: List<XyoWitness<XyoPayload>>? = null,
        token: String? = null
    ) :
        this(
            context,
            listOf(
                XyoArchivistApiClient.get(
                    XyoArchivistApiConfig(
                        archive ?: XyoPanel.DefaultApiArchive,
                        apiDomain ?: XyoPanel.DefaultApiDomain
                    )
                )
            ),
            witnesses
        )

    constructor(
        context: Context,
        observe: ((context: Context, previousHash: String?) -> XyoEventPayload?)?
    ):this(
        context,
        emptyList<XyoArchivistApiClient>(),
        listOf(XyoWitness(observe)))

    suspend fun event(event: String): XyoPanelReportResult {
        val adhocWitnessList = listOf(
            XyoWitness<XyoEventPayload>({
                context, previousHash -> XyoEventPayload(event, previousHash)
            })
        )
        return this.report(adhocWitnessList)
    }

    suspend fun report(adhocWitnesses: List<XyoWitness<XyoPayload>> = emptyList()): XyoPanelReportResult {
        val witnesses: List<XyoWitness<XyoPayload>> = (this.witnesses ?: emptyList()).plus(adhocWitnesses)
        val payloads = witnesses.map { witness ->
                witness.observe(context)
        }
        val bw = XyoBoundWitnessBuilder()
            .payloads(payloads.mapNotNull { payload -> payload })
            .witnesses(witnesses)
            .build()
        val results = mutableListOf<PostBoundWitnessesResult>()
        archivists.forEach { archivist ->
            results.add(archivist.postBoundWitnessAsync(bw))
        }
        return XyoPanelReportResult(bw, results)
    }

    companion object {
        val DefaultApiArchive = "default"
        val DefaultApiDomain = "https://archivist.xyo.network"
        val defaultArchivist: XyoArchivistApiClient
            get() {
                val apiConfig = XyoArchivistApiConfig(this.DefaultApiArchive, this.DefaultApiDomain)
                return XyoArchivistApiClient.get(apiConfig)
            }
    }
}