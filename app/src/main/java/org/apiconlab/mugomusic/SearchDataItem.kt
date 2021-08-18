package org.apiconlab.mugomusic

data class SearchDataItem(
    val song_name: String,
    val album_name: String,
    val song_artist: String,
    val song_image: String,
    val download_links: List<String>
)