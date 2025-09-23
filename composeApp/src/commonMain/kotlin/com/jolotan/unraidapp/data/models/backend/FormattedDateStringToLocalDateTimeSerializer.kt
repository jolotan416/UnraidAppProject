package com.jolotan.unraidapp.data.models.backend

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class FormattedDateStringToLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    companion object {
        private const val DATE_TIME_STRING_API_FORMAT = "uuuu-MM-dd'T'HH:mm:ss.SSS'Z'"

        @OptIn(FormatStringsInDatetimeFormats::class)
        private val LOCAL_DATE_TIME_FORMAT =
            LocalDateTime.Format { byUnicodePattern(DATE_TIME_STRING_API_FORMAT) }
    }


    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("FormattedDateString") {
            element("date time", serialDescriptor<LocalDateTime>())
        }

    override fun serialize(
        encoder: Encoder,
        value: LocalDateTime
    ) {
        encoder.encodeString(value.format(LOCAL_DATE_TIME_FORMAT))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val dateTimeString = decoder.decodeString()

        return LocalDateTime.parse(
            dateTimeString,
            LOCAL_DATE_TIME_FORMAT
        )
    }
}