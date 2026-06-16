package com.nino161er.rssfeed.data.model

data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val frequency: Float,
    val latitude: Double,
    val longitude: Double,
    val category: String? = null,
    val imageUrl: String? = null,
    val country: String? = null
) {
    companion object {
        val defaultStations = listOf(
            // === 87-89 MHz ===
            RadioStation("1", "France Inter", "https://icecast.radiofrance.fr/franceinter-midfi.mp3", 87.8f, 48.858, 2.350, "Talk", country = "France"),
            RadioStation("2", "Rock Antenne", "https://stream.rockantenne.de/rockantenne/stream/mp3", 87.9f, 48.135, 11.582, "Rock", country = "Germany"),
            RadioStation("3", "BBC Radio 2", "https://stream.live.vc.bbc.co.uk/bbc_radio_two", 88.1f, 51.518, -0.144, "Adult Contemporary", country = "UK"),
            RadioStation("4", "Bayern 1", "https://streams.br.de/bayern1_2.m3u", 88.8f, 48.137, 11.575, "Pop", country = "Germany"),
            RadioStation("5", "Rai Radio 1", "https://icestreaming.rai.it/1.mp3", 89.3f, 41.900, 12.480, "News", country = "Italy"),
            RadioStation("6", "Radio France Internationale", "https://icecast.rfi.fr/rfimonde", 89.5f, 48.857, 2.349, "News", country = "France"),
            RadioStation("7", "Hitradio Ö3", "https://orf-live.ors-shoutcast.at/oe3-q1a", 89.8f, 48.208, 16.373, "Pop", country = "Austria"),

            // === 90-92 MHz ===
            RadioStation("8", "FIP", "https://icecast.radiofrance.fr/fip-midfi.mp3", 90.4f, 48.856, 2.353, "Eclectic", country = "France"),
            RadioStation("9", "BBC Radio 3", "https://stream.live.vc.bbc.co.uk/bbc_radio_three", 91.3f, 51.522, -0.149, "Classical", country = "UK"),
            RadioStation("10", "Antenne Bayern", "https://stream.antenne.de/antenne/stream/mp3", 91.2f, 48.135, 11.582, "Pop", country = "Germany"),
            RadioStation("11", "Jazz Radio", "https://jazzradio.ice.infomaniak.ch/jazzradio-high.mp3", 91.5f, 48.856, 2.352, "Jazz", country = "France"),
            RadioStation("12", "SR P3", "https://http-live.sr.se/p3-mp3-192", 91.8f, 59.330, 18.070, "Pop", country = "Sweden"),
            RadioStation("13", "NPO Radio 2", "https://icecast.omroep.nl/radio2-bb-mp3", 92.2f, 52.370, 4.890, "Adult Contemporary", country = "Netherlands"),
            RadioStation("14", "Radio Nacional de España", "https://rtvelivestreamv3.akamaized.net/rtvec/1/rne1_main.mp3", 92.6f, 40.416, -3.704, "News", country = "Spain"),
            RadioStation("15", "Radio Swiss Jazz", "https://stream.srg-ssr.ch/m/rsj/mp3_128", 92.9f, 47.376, 8.543, "Jazz", country = "Switzerland"),

            // === 93-95 MHz ===
            RadioStation("16", "BBC Radio 4", "https://stream.live.vc.bbc.co.uk/bbc_radio_fourfm", 93.5f, 51.520, -0.140, "Talk", country = "UK"),
            RadioStation("17", "Bayern 3", "https://streams.br.de/bayern3_2.m3u", 93.7f, 48.140, 11.578, "Pop", country = "Germany"),
            RadioStation("18", "Los 40 Principales", "https://25683.live.streamtheworld.com/CADENA_SER.mp3", 93.9f, 40.416, -3.703, "Pop", country = "Spain"),
            RadioStation("19", "NPO 3FM", "https://icecast.omroep.nl/3fm-bb-mp3", 94.3f, 52.370, 4.892, "Pop", country = "Netherlands"),
            RadioStation("20", "Radio Wien", "https://orf-live.ors-shoutcast.at/wie-q1a", 94.7f, 48.208, 16.372, "Pop", country = "Austria"),
            RadioStation("21", "Virgin Radio France", "https://ais-live.cloud-services.paris:8080/europe2.mp3", 95.2f, 48.857, 2.351, "Rock", country = "France"),
            RadioStation("22", "Capital FM", "https://media-ice.musicradio.com/CapitalMP3", 95.8f, 51.515, -0.142, "Pop", country = "UK"),

            // === 96-98 MHz ===
            RadioStation("23", "SWR3", "https://liveradio.swr.de/sw3/", 96.5f, 48.775, 9.183, "Pop", country = "Germany"),
            RadioStation("24", "WDR 2", "https://wdr-wdr2-ruhrgebiet.icecastssl.wdr.de/wdr/wdr2/ruhrgebiet/mp3/128/stream.mp3", 96.8f, 51.430, 7.000, "Pop", country = "Germany"),
            RadioStation("25", "Deutschlandfunk", "https://st01.dlf.de/dlf/01/128/mp3/stream.mp3", 97.7f, 50.933, 6.950, "News", country = "Germany"),
            RadioStation("26", "Radio María España", "https://cdnradio.radiomaria.es/rme-live", 98.0f, 40.416, -3.702, "Religious", country = "Spain"),
            RadioStation("27", "Radio Monaco", "https://icecast.monaco.mc:8443/radio-monaco", 98.2f, 43.738, 7.424, "Pop", country = "Monaco"),
            RadioStation("28", "Cadena 100", "https://flucast-b09.80s.es/cadena100", 98.5f, 40.418, -3.700, "Pop", country = "Spain"),
            RadioStation("29", "BBC Radio 1", "https://stream.live.vc.bbc.co.uk/bbc_radio_one", 98.8f, 51.518, -0.143, "Pop", country = "UK"),

            // === 99-101 MHz ===
            RadioStation("30", "Radio Nova", "https://novazz.ice.infomaniak.ch/nova.mp3", 99.0f, 48.858, 2.350, "Pop", country = "France"),
            RadioStation("31", "hr3", "https://dispatcher.rndtech.de/s/2630/live/128/mp3", 99.4f, 50.115, 8.688, "Pop", country = "Germany"),
            RadioStation("32", "Sky Radio", "https://playerservices.streamtheworld.com/api/livestream?mount=SKYRADIO&transports=http", 99.9f, 52.370, 4.893, "Pop", country = "Netherlands"),
            RadioStation("33", "NRJ Paris", "https://scdn.nrjaudio.fm/60001", 100.3f, 48.860, 2.348, "Pop", country = "France"),
            RadioStation("34", "BBC Radio 5 Live", "https://stream.live.vc.bbc.co.uk/bbc_radio_five_live", 100.6f, 51.518, -0.145, "Talk", country = "UK"),
            RadioStation("35", "Classic FM", "https://media-ice.musicradio.com/ClassicFMMP3", 100.9f, 51.521, -0.148, "Classical", country = "UK"),
            RadioStation("36", "Smooth Radio", "https://media-ice.musicradio.com/SmoothUKMP3", 101.1f, 51.516, -0.147, "Adult Contemporary", country = "UK"),
            RadioStation("37", "Radio 24", "https://icecast.unitedradio.it/Radio24.mp3", 101.5f, 45.464, 9.192, "News", country = "Italy"),
            RadioStation("38", "Ö1", "https://orf-live.ors-shoutcast.at/oe1-q1a", 101.8f, 48.208, 16.374, "Culture", country = "Austria"),

            // === 102-104 MHz ===
            RadioStation("39", "Radio 538", "https://playerservices.streamtheworld.com/api/livestream?mount=RADIO538&transports=http", 102.1f, 52.370, 4.895, "Pop", country = "Netherlands"),
            RadioStation("40", "RTL 102.5", "https://streamingv2.rtl.it/rtlfm", 102.5f, 45.465, 9.190, "Pop", country = "Italy"),
            RadioStation("41", "Radio SRF 3", "https://stream.srg-ssr.ch/m/rsj/mp3_128", 103.5f, 47.376, 8.542, "Pop", country = "Switzerland"),
            RadioStation("42", "Europe 2", "https://ais-live.cloud-services.paris:8080/europe2.mp3", 103.9f, 48.857, 2.352, "Rock", country = "France"),
            RadioStation("43", "RTL", "https://streaming.radio.rtl.fr/rtl-1-44-128", 104.3f, 48.857, 2.351, "General", country = "France"),
            RadioStation("44", "Kiss FM", "https://media-ice.musicradio.com/KissFMMP3", 104.7f, 51.517, -0.146, "Pop", country = "UK"),

            // === 105-108 MHz ===
            RadioStation("45", "Radio Italia", "https://radioitaliashoutcasting.streamingradio.it/radioitalia", 105.0f, 45.464, 9.191, "Italian Pop", country = "Italy"),
            RadioStation("46", "Radio 105", "https://icecast.unitedradio.it/Radio105.mp3", 105.3f, 45.465, 9.191, "Pop", country = "Italy"),
            RadioStation("47", "Absolute Radio", "https://ais-cloud.absoluteradio.co.uk/absolute.mp3", 105.8f, 51.517, -0.146, "Rock", country = "UK"),
            RadioStation("48", "Heart London", "https://media-ice.musicradio.com/HeartLondonMP3", 106.2f, 51.519, -0.145, "Adult Contemporary", country = "UK"),
            RadioStation("49", "Antenne France", "https://icecast.skyrock.net/s/natio_mp3_128k", 106.5f, 48.858, 2.350, "Pop", country = "France"),
            RadioStation("50", "Radio Classique", "https://radioclassique.ice.infomaniak.ch/radioclassique-high.mp3", 106.8f, 48.856, 2.354, "Classical", country = "France"),
            RadioStation("51", "LBC", "https://media-ice.musicradio.com/LBCUKMP3", 107.1f, 51.518, -0.143, "Talk", country = "UK"),
            RadioStation("52", "Radio X", "https://media-ice.musicradio.com/RadioXUKMP3", 107.5f, 51.519, -0.144, "Rock", country = "UK"),
            RadioStation("53", "BBC Radio 1Xtra", "https://stream.live.vc.bbc.co.uk/bbc_1xtra", 107.9f, 51.520, -0.142, "Urban", country = "UK"),
        ).sortedBy { it.frequency }
    }
}
