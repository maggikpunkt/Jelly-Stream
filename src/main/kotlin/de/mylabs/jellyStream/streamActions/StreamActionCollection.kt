package de.mylabs.jellyStream.streamActions

class StreamActionCollection(
    val transcodes: ArrayList<Transcode> = ArrayList<Transcode>(),
    val extractions: ArrayList<Extraction> = ArrayList<Extraction>(),
    val groupExtractions: ArrayList<GroupExtraction> = ArrayList<GroupExtraction>()
) {
    fun add(transcode: Transcode) = transcodes.add(transcode)
    fun add(extraction: Extraction) = extractions.add(extraction)
    fun add(groupExtraction: GroupExtraction) = groupExtractions.add(groupExtraction)
    fun addAll(streamActions: StreamActionCollection) {
        this.transcodes += streamActions.transcodes
        this.extractions += streamActions.extractions
        this.groupExtractions += streamActions.groupExtractions
    }
}